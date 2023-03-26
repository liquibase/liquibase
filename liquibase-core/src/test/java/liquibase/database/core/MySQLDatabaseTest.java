package liquibase.database.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.statement.DatabaseFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for {@link MySQLDatabase}
 */
public class MySQLDatabaseTest extends AbstractJdbcDatabaseTest<MySQLDatabase> {

    public MySQLDatabaseTest() throws Exception {
        super(new MySQLDatabase());
    }

    @Override
    protected String getProductNameString() {
        return "MySQL";
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        assertEquals("NOW()", getDatabase().getCurrentDateTimeFunction());
    }

    @Test
    public void getCurrentDateTimeFunctionWithPrecision() {
        final MySQLDatabase mySQLDatabase = getDatabase();
        assertEquals("NOW(1)", mySQLDatabase.getCurrentDateTimeFunction(1));
        assertEquals("NOW(2)", mySQLDatabase.getCurrentDateTimeFunction(2));
        assertEquals("NOW(5)", mySQLDatabase.getCurrentDateTimeFunction(5));
    }

    @Test
    public void generateDatabaseFunctionValue() {
        final MySQLDatabase mySQLDatabase = getDatabase();
        assertEquals("NOW()", mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction("CURRENT_TIMESTAMP()")));
        assertNull(mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction(null)));
    }

    @Test
    public void generateDatabaseFunctionValueWithPrecision() {
        final MySQLDatabase mySQLDatabase = getDatabase();
        assertEquals("NOW(2)", mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction("CURRENT_TIMESTAMP(2)")));
        assertEquals("NOW(3)", mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction("CURRENT_TIMESTAMP(3)")));
    }

    @Test
    public void generateDatabaseFunctionValueWithIncorrectPrecision() {
        final MySQLDatabase mySQLDatabase = getDatabase();
        assertEquals("NOW()", mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction("CURRENT_TIMESTAMP(string)")));
    }

    @Test
    public void testGetDefaultDriver() throws DatabaseException {
        try (Database database = new MySQLDatabase()) {
            assertEquals("com.mysql.cj.jdbc.Driver", database.getDefaultDriver("jdbc:mysql://localhost/liquibase"));

            assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
        }
    }

    @Override
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        " catalogName | schemaName | tableName | catalogName.tableName ",
    })
    public void escapeTableName_withSchema(String catalogName, String schemaName, String tableName, String expected) {
        final Database database = getDatabase();
        assertEquals(expected, database.escapeTableName(catalogName, schemaName, tableName));
    }

    @Test
    public void escapeStringForDatabase_withBackslashes() {
        assertEquals("\\\\0", database.escapeStringForDatabase("\\0"));
    }
}
