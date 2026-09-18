package liquibase.snapshot.jvm;

import liquibase.database.core.H2Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.core.Grant;
import liquibase.structure.core.Schema;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GrantSnapshotGeneratorTest {

    private final GrantSnapshotGenerator generator = new GrantSnapshotGenerator();

    @Test
    public void getPriority_postgres_isSupported() {
        assertTrue(generator.getPriority(Grant.class, new PostgresDatabase()) > 0);
    }

    @Test
    public void getPriority_mysql_isSupported() {
        assertTrue(generator.getPriority(Grant.class, new MySQLDatabase()) > 0);
    }

    @Test
    public void getPriority_unsupportedDatabase_isNone() {
        assertEquals(GrantSnapshotGenerator.PRIORITY_NONE, generator.getPriority(Grant.class, new H2Database()));
    }

    @Test
    public void getSelectGrantsStatement_postgres_filtersBySchema() {
        Schema schema = new Schema("mydb", "public");
        SqlStatement statement = generator.getSelectGrantsStatement(schema, new PostgresDatabase());

        RawParameterizedSqlStatement rawStatement = (RawParameterizedSqlStatement) statement;
        assertTrue(rawStatement.getSql().contains("information_schema.table_privileges"));
        assertEquals(1, rawStatement.getParameters().size());
        assertEquals("public", rawStatement.getParameters().get(0));
    }

    @Test
    public void mapToGrant_grantableYes_setsGrantableTrue() {
        Schema schema = new Schema("mydb", "public");
        Map<String, Object> row = new HashMap<>();
        row.put("GRANTEE", "app_user");
        row.put("PRIVILEGE_TYPE", "SELECT");
        row.put("OBJECT_NAME", "orders");
        row.put("IS_GRANTABLE", "YES");

        Grant grant = generator.mapToGrant(row, schema);

        assertEquals("app_user", grant.getGranteeName());
        assertEquals("SELECT", grant.getPrivilege());
        assertEquals("orders", grant.getObjectName());
        assertEquals("TABLE", grant.getObjectType());
        assertTrue(grant.getGrantable());
    }

    @Test
    public void mapToGrant_grantableNo_setsGrantableFalse() {
        Schema schema = new Schema("mydb", "public");
        Map<String, Object> row = new HashMap<>();
        row.put("GRANTEE", "app_user");
        row.put("PRIVILEGE_TYPE", "SELECT");
        row.put("OBJECT_NAME", "orders");
        row.put("IS_GRANTABLE", "NO");

        Grant grant = generator.mapToGrant(row, schema);

        assertFalse(grant.getGrantable());
    }
}
