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
 * Liquibase (a manual {@code GRANT}/{@code REVOKE}). Currently supports PostgreSQL and MySQL/MariaDB by reading
 * the standard {@code information_schema.table_privileges} view.
 * <p>
 * Because {@link Grant#snapshotByDefault()} is {@code false}, this generator only runs when Grant is explicitly
 * requested, e.g. {@code liquibase diff --snapshot-types=grants} or {@code liquibase snapshot --snapshot-types=grants}.
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
                "YES".equalsIgnoreCase(isGrantable)
        );
    }

    protected SqlStatement getSelectGrantsStatement(Schema schema, Database database) {
        String schemaName = schema.getName();
        if (schemaName == null) {
            schemaName = database.getDefaultSchemaName();
        }

        if ((database instanceof PostgresDatabase) || (database instanceof MySQLDatabase)) {
            String sql = "SELECT grantee AS GRANTEE, privilege_type AS PRIVILEGE_TYPE, table_name AS OBJECT_NAME, is_grantable AS IS_GRANTABLE " +
                    "FROM information_schema.table_privileges " +
                    "WHERE table_schema = ? " +
                    "ORDER BY table_name, grantee, privilege_type";
            return new RawParameterizedSqlStatement(sql, schemaName);
        }

        throw new UnexpectedLiquibaseException("Don't know how to query for grants on " + database.getShortName());
    }
}
