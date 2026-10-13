package liquibase.snapshot.jvm;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Grant;
import liquibase.structure.core.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Snapshot generator for table-level GRANT privileges, used to detect permission drift that happened outside of
 * Liquibase (a manual {@code GRANT}/{@code REVOKE}). Currently supports PostgreSQL (reading {@code pg_class.relacl}
 * directly) and MySQL/MariaDB (reading {@code information_schema.table_privileges}).
 * <p>
 * Because {@link Grant#snapshotByDefault()} is {@code false}, this generator only runs when Grant is explicitly
 * requested, e.g. {@code liquibase diff --diff-types=grants}.
 */
public class GrantSnapshotGenerator extends JdbcSnapshotGenerator {

    public GrantSnapshotGenerator() {
        super(Grant.class, new Class[]{Schema.class});
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (!isSupported(database)) {
            return PRIORITY_NONE;
        }
        return super.getPriority(objectType, database);
    }

    protected boolean isSupported(Database database) {
        return (database instanceof PostgresDatabase) || (database instanceof MySQLDatabase);
    }

    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        if (!(foundObject instanceof Schema) || !snapshot.getSnapshotControl().shouldInclude(Grant.class) ||
                !isSupported(snapshot.getDatabase())) {
            return;
        }

        Schema schema = (Schema) foundObject;
        for (Grant grant : queryGrants(schema, snapshot.getDatabase())) {
            schema.addDatabaseObject(grant);
        }
    }

    @Override
    protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException, InvalidExampleException {
        Grant exampleGrant = (Grant) example;
        Schema schema = exampleGrant.getSchema();
        if ((schema == null) || !isSupported(snapshot.getDatabase())) {
            return null;
        }

        for (Grant grant : queryGrants(schema, snapshot.getDatabase())) {
            if (grant.equals(exampleGrant)) {
                return grant;
            }
        }
        return null;
    }

    protected List<Grant> queryGrants(Schema schema, Database database) throws DatabaseException {
        SqlStatement statement = getSelectGrantsStatement(schema, database);

        List<Map<String, ?>> rows = Scope.getCurrentScope().getSingleton(ExecutorService.class)
                .getExecutor("jdbc", database)
                .queryForList(statement);

        List<Grant> grants = new ArrayList<>();
        if (rows != null) {
            for (Map<String, ?> row : rows) {
                grants.add(mapToGrant(row, schema));
            }
        }
        return grants;
    }

    protected Grant mapToGrant(Map<String, ?> row, Schema schema) {
        String isGrantable = String.valueOf(row.get("IS_GRANTABLE"));

        return new Grant(
                schema.getCatalogName(),
                schema.getName(),
                "TABLE",
                (String) row.get("OBJECT_NAME"),
                (String) row.get("PRIVILEGE_TYPE"),
                (String) row.get("GRANTEE"),
                "YES".equalsIgnoreCase(isGrantable),
                (String) row.get("GRANTOR")
        );
    }

    protected SqlStatement getSelectGrantsStatement(Schema schema, Database database) {
        String schemaName = schema.getName();
        if (schemaName == null) {
            schemaName = database.getDefaultSchemaName();
        }

        if (database instanceof PostgresDatabase) {
            //information_schema.table_privileges only shows rows where the *connected* role is the grantor,
            //the grantee, or a member of one of those roles, so it silently hides grants between two other
            //roles. Reading pg_class.relacl directly via aclexplode() returns every privilege in the catalog
            //regardless of which role is connected. acldefault('r', relowner) fills in the implicit
            //full-privilege row for tables that have never had an explicit GRANT/REVOKE run (relacl IS NULL).
            //
            //grantee/grantor are LEFT JOINed rather than INNER JOINed: a grant TO PUBLIC has grantee OID 0,
            //which matches no row in pg_roles, so an inner join would silently drop every PUBLIC grant from
            //the results - the opposite of what a drift-detection query should ever do. OID 0 is mapped to
            //the literal 'PUBLIC' explicitly since that's what it means; grantor is defensively left-joined
            //the same way even though PUBLIC can't itself grant something, so a join mismatch here still
            //surfaces the row (with a null grantor) instead of dropping it.
            String sql = "SELECT CASE WHEN acl.grantee = 0 THEN 'PUBLIC' ELSE grantee_role.rolname END AS GRANTEE, " +
                    "acl.privilege_type AS PRIVILEGE_TYPE, " +
                    "c.relname AS OBJECT_NAME, " +
                    "CASE WHEN acl.is_grantable THEN 'YES' ELSE 'NO' END AS IS_GRANTABLE, " +
                    "grantor_role.rolname AS GRANTOR " +
                    "FROM pg_class c " +
                    "JOIN pg_namespace n ON n.oid = c.relnamespace " +
                    "CROSS JOIN LATERAL aclexplode(COALESCE(c.relacl, acldefault('r', c.relowner))) AS acl " +
                    "LEFT JOIN pg_roles grantee_role ON grantee_role.oid = acl.grantee " +
                    "LEFT JOIN pg_roles grantor_role ON grantor_role.oid = acl.grantor " +
                    "WHERE n.nspname = ? " +
                    "AND c.relkind IN ('r', 'v', 'm', 'f', 'p') " +
                    "ORDER BY c.relname, GRANTEE, acl.privilege_type";
            return new RawParameterizedSqlStatement(sql, schemaName);
        } else if (database instanceof MySQLDatabase) {
            //MySQL's information_schema.table_privileges has no grantor column
            String sql = "SELECT grantee AS GRANTEE, privilege_type AS PRIVILEGE_TYPE, table_name AS OBJECT_NAME, " +
                    "is_grantable AS IS_GRANTABLE, NULL AS GRANTOR " +
                    "FROM information_schema.table_privileges " +
                    "WHERE table_schema = ? " +
                    "ORDER BY table_name, grantee, privilege_type";
            return new RawParameterizedSqlStatement(sql, schemaName);
        }

        throw new UnexpectedLiquibaseException("Don't know how to query for grants on " + database.getShortName());
    }
}
