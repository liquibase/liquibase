package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.OfflineConnection;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.core.TimestampType;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.resource.ResourceAccessor;
import liquibase.sdk.executor.MockExecutor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.test.JUnitResourceAccessor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.ResourceBundle;
import liquibase.Scope;

import static java.util.ResourceBundle.getBundle;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        final Database database = getDatabase();
        assertEquals("tableName", database.escapeTableName(null, null, "tableName"), "table name without schema is correctly escaped as simply tableName");
    }

    @Test
    public void saveNlsEnvironment() throws Exception {
        final Database database = getDatabase();
        final ResourceAccessor junitResourceAccessor = new JUnitResourceAccessor();
        final OfflineConnection offlineConnection = new OfflineConnection("offline:oracle", junitResourceAccessor);
        database.setConnection(offlineConnection);
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        final Database database = getDatabase();
        assertEquals("catalogName.tableName",database.escapeTableName("catalogName", "schemaName", "tableName"), "table name without schema but with catalog is correctly escaped as catalogName.tableName");
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertTrue(getDatabase().supportsInitiallyDeferrableColumns(), "Oracle Database is correctly reported as being able to do INITIALLY DEFERRED column constraints.");
    }

    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        assertEquals("SYSTIMESTAMP", getDatabase().getCurrentDateTimeFunction(), "Oracle Database's 'give me the current timestamp' function is correctly reported.");
    }

    @Test
    public void verifyTimestampDataTypeWhenWithoutClauseIsPresent() {
        final TimestampType ts = new TimestampType();
        ts.setAdditionalInformation("WITHOUT TIME ZONE");
        final DatabaseDataType oracleDataType = ts.toDatabaseDataType(getDatabase());
        assertThat(oracleDataType.getType(), is("TIMESTAMP"));
    }

    public void testGetDefaultDriver() throws DatabaseException {
        try (Database database = new OracleDatabase()) {
            assertEquals("The correct JDBC driver class name is reported if the URL is a Oracle JDBC URL", "oracle.jdbc.OracleDriver", database.getDefaultDriver("jdbc:oracle:thin:@localhost/XE"));

            assertNull("No JDBC driver class is returned if the URL is NOT an Oracle Database JDBC URL.", database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
        } catch (final DatabaseException e) {
            throw e;
        }
    }

    @Test
    public void validateCore2953WrongSqlOnValueSequenceNext() throws LiquibaseException {
        final Database database = getDatabase();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
        database.setDefaultSchemaName("sampleschema");

        final MockExecutor mockExecutor = new MockExecutor();
        mockExecutor.setDatabase(database);

        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, mockExecutor);

        final UpdateStatement updateStatement = new UpdateStatement(null, null, "test_table");
        updateStatement.addNewColumnValue("id", new SequenceNextValueFunction("test_table_id_seq"));

        database.execute(new SqlStatement[]{updateStatement}, new ArrayList<SqlVisitor>());

        assertEquals("UPDATE \"SAMPLESCHEMA\".\"test_table\" SET \"id\" = \"SAMPLESCHEMA\".\"test_table_id_seq\".nextval;", mockExecutor.getRanSql().trim());
    }

    @Test
    public void getDateLiteral_date() {
        // DATE in Oracle can have hours/minutes/seconds.
        assertEquals("TO_DATE('2017-08-16 16:32:55', 'YYYY-MM-DD HH24:MI:SS')", database.getDateLiteral("2017-08-16T16:32:55"));
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

