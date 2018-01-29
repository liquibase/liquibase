package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.OfflineConnection;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.resource.ResourceAccessor;
import liquibase.sdk.executor.MockExecutor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.test.JUnitResourceAccessor;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;
import static org.junit.Assert.*;

/**
 * Tests for {@link liquibase.database.core.OracleDatabase}.
 */
public class OracleDatabaseTest extends AbstractJdbcDatabaseTest {
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");


    public OracleDatabaseTest() throws Exception {
        super(new OracleDatabase());
    }

    @Override
    protected String getProductNameString() {
        return "Oracle";
    }

    @Override
    @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("table name without schema is correctly escaped as simply tableName",
                "tableName", database.escapeTableName(null, null, "tableName"));
    }

    @Test
    public void saveNlsEnvironment() throws Exception {
        Database database = getDatabase();
        ResourceAccessor junitResourceAccessor = new JUnitResourceAccessor();
        OfflineConnection offlineConnection = new OfflineConnection("offline:oracle", junitResourceAccessor);
        database.setConnection(offlineConnection);
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("table name without schema but with catalog is correctly escaped as catalogName.tableName",
                "catalogName.tableName", database.escapeTableName("catalogName", "schemaName", "tableName"));
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertTrue("Oracle Database is correctly reported as being able to do INITIALLY DEFERRED column constraints.",
                getDatabase().supportsInitiallyDeferrableColumns());
    }


    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        Assert.assertEquals("Oracle Database's 'give me the current timestamp' function is correctly reported.",
                "SYSTIMESTAMP", getDatabase().getCurrentDateTimeFunction());
    }

    @Test
    public void testGetDefaultDriver() {
        Database database = new OracleDatabase();

        assertEquals("The correct JDBC driver class name is reported if the URL is a Oracle JDBC URL",
                "oracle.jdbc.OracleDriver", database.getDefaultDriver("jdbc:oracle:thin:@localhost/XE"));

        assertNull("No JDBC driver class is returned if the URL is NOT an Oracle Database JDBC URL.",
                database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

    @Test
    public void validateCore2953WrongSqlOnValueSequenceNext() throws LiquibaseException {
        Database database = getDatabase();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
        database.setDefaultSchemaName("sampleschema");

        MockExecutor mockExecutor = new MockExecutor();
        mockExecutor.setDatabase(database);

        ExecutorService.getInstance().setExecutor(database, mockExecutor);

        UpdateStatement updateStatement = new UpdateStatement(null, null, "test_table");
        updateStatement.addNewColumnValue("id", new SequenceNextValueFunction("test_table_id_seq"));

        database.execute(new SqlStatement[]{updateStatement}, new ArrayList<SqlVisitor>());

        assertEquals("UPDATE \"SAMPLESCHEMA\".\"test_table\" SET \"id\" = \"SAMPLESCHEMA\".\"test_table_id_seq\".nextval;", mockExecutor.getRanSql().trim());
    }

    @Test
    public void getDateLiteral_dateOnly() {
        assertEquals("TO_DATE('2017-08-16', 'YYYY-MM-DD')", database.getDateLiteral("2017-08-16"));
    }

    @Test
    public void getDateLiteral_timeOnly() {
        assertEquals("TO_DATE('16:32:55', 'HH24:MI:SS')", database.getDateLiteral("16:32:55"));
    }

    @Test
    public void getDateLiteral_timestamp() {
        assertEquals("TO_TIMESTAMP('2017-08-16 16:32:55.125', 'YYYY-MM-DD HH24:MI:SS.FF')", database.getDateLiteral("2017-08-16T16:32:55.125"));
    }

    @Test
    public void getDateLiteral_datetime() {
        assertEquals("TO_TIMESTAMP('2017-08-16 16:32:55.3', 'YYYY-MM-DD HH24:MI:SS.FF')", database.getDateLiteral("2017-08-16T16:32:55.3"));
    }

    @Test
    public void getDateLiteral_datetime_invalid() {
        assertEquals("UNSUPPORTED:2017-08-16T16:32:55_3", database.getDateLiteral("2017-08-16T16:32:55_3"));
    }

    @Test
    public void getDateLiteral_unsupported() {
        assertEquals("UNSUPPORTED:123", database.getDateLiteral("123"));
    }
}

