package liquibase.database.core;

import static java.util.ResourceBundle.getBundle;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.ResourceBundle;
import liquibase.Scope;
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
import liquibase.statement.SequenceNextValueFunction;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.test.JUnitResourceAccessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for {@link liquibase.database.core.OracleDatabase}.
 */
public class OracleDatabaseTest extends AbstractJdbcDatabaseTest<OracleDatabase> {

    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    public OracleDatabaseTest() throws Exception {
        super(new OracleDatabase());
    }

    @Override
    protected String getProductNameString() {
        return "Oracle";
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        " tableName | tableName ",
    })
    public void escapeTableName_noSchema(String tableName, String expected) throws DatabaseException {
        final Database database = getDatabase();
        assertEquals(expected, database.escapeTableName(null, null, tableName), "table name without schema is correctly escaped as simply tableName");
    }

    @Test
    public void saveNlsEnvironment() throws Exception {
        final Database database = getDatabase();
        final ResourceAccessor junitResourceAccessor = new JUnitResourceAccessor();
        final OfflineConnection offlineConnection = new OfflineConnection("offline:oracle", junitResourceAccessor);
        database.setConnection(offlineConnection);
    }

    @Override
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        " catalogName | schemaName | tableName | catalogName.tableName ",
    })
    public void escapeTableName_withSchema(String catalogName, String schemaName, String tableName, String expected) {
        final Database database = getDatabase();
        assertEquals(expected, database.escapeTableName(catalogName, schemaName, tableName), "table name without schema but with catalog is correctly escaped as catalogName.tableName");
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

    @Test
    public void testGetDefaultDriver() throws DatabaseException {
        try (Database database = new OracleDatabase()) {
            assertEquals("oracle.jdbc.OracleDriver", database.getDefaultDriver("jdbc:oracle:thin:@localhost/XE"), "The correct JDBC driver class name is reported if the URL is a Oracle JDBC URL");

            assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"), "No JDBC driver class is returned if the URL is NOT an Oracle Database JDBC URL.");
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

        database.execute(new SqlStatement[]{updateStatement}, new ArrayList<>());

        assertEquals("UPDATE \"SAMPLESCHEMA\".\"test_table\" SET \"id\" = \"SAMPLESCHEMA\".\"test_table_id_seq\".nextval;", mockExecutor.getRanSql().trim());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        // DATE in Oracle can have hours/minutes/seconds.
        " 2017-08-16T16:32:55     | TO_DATE('2017-08-16 16:32:55', 'YYYY-MM-DD HH24:MI:SS')             ",
        " 2017-08-16              | TO_DATE('2017-08-16', 'YYYY-MM-DD')                                 ",
        " 16:32:55                | TO_DATE('16:32:55', 'HH24:MI:SS')                                   ",
        " 2017-08-16T16:32:55.125 | TO_TIMESTAMP('2017-08-16 16:32:55.125', 'YYYY-MM-DD HH24:MI:SS.FF') ",
        " 2017-08-16T16:32:55.3   | TO_TIMESTAMP('2017-08-16 16:32:55.3', 'YYYY-MM-DD HH24:MI:SS.FF')   ",
        " 2017-08-16T16:32:55_3   | UNSUPPORTED:2017-08-16T16:32:55_3                                   ",
        " 123                     | UNSUPPORTED:123                                                     ",
    })
    public void getDateLiteral(String isoDate, String expected) {
        assertEquals(expected, database.getDateLiteral(isoDate));
    }
}

