package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.database.MockDatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.statement.DatabaseFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

/**
 * Tests for {@link MySQLDatabase}
 */
public class MySQLDatabaseTest extends AbstractJdbcDatabaseTest {

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
        final MySQLDatabase mySQLDatabase = (MySQLDatabase) getDatabase();
        assertEquals("NOW(1)", mySQLDatabase.getCurrentDateTimeFunction(1));
        assertEquals("NOW(2)", mySQLDatabase.getCurrentDateTimeFunction(2));
        assertEquals("NOW(5)", mySQLDatabase.getCurrentDateTimeFunction(5));
    }

    @Test
    public void generateDatabaseFunctionValue() {
        final MySQLDatabase mySQLDatabase = (MySQLDatabase) getDatabase();
        assertEquals("NOW()", mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction("CURRENT_TIMESTAMP()")));
        assertNull(mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction(null)));
    }

    @Test
    public void generateDatabaseFunctionValueWithPrecision() {
        final MySQLDatabase mySQLDatabase = (MySQLDatabase) getDatabase();
        assertEquals("NOW(2)", mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction("CURRENT_TIMESTAMP(2)")));
        assertEquals("NOW(3)", mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction("CURRENT_TIMESTAMP(3)")));
    }

    @Test
    public void generateDatabaseFunctionValueWithIncorrectPrecision() {
        final MySQLDatabase mySQLDatabase = (MySQLDatabase) getDatabase();
        assertEquals("NOW()", mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction("CURRENT_TIMESTAMP(string)")));
    }

    public void testGetDefaultDriver() throws DatabaseException {
        try (Database database = new MySQLDatabase()) {
            assertEquals("com.mysql.cj.jdbc.Driver", database.getDefaultDriver("jdbc:mysql://localhost/liquibase"));

            assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
        } catch (final DatabaseException e) {
            throw e;
        }
    }

    @Override
    @Test
    public void escapeTableName_noSchema() {
        final Database database = getDatabase();
        assertEquals("tableName", database.escapeTableName(null, null, "tableName"));
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        final Database database = getDatabase();
        assertEquals("catalogName.tableName", database.escapeTableName("catalogName", "schemaName", "tableName"));
    }

    @Test
    public void escapeStringForDatabase_withBackslashes() {
        assertEquals("\\\\0", database.escapeStringForDatabase("\\0"));
    }

    /**
     * Tests whether reserved keywords are added for MySQL 8.0.
     */
    @Test
    public void verifyMySQL8ReservedWordsAreNotPresent() {
        Database database = getDatabase();
        MockDatabaseConnection connection = new MockDatabaseConnection();
        connection.setDatabaseMajorVersion(5);
        connection.setDatabaseMinorVersion(7);
        database.setConnection(connection);
        // attaching the connection should trigger calling "addReservedWords" - as the
        // MockDatabaseConnection does not do that, we trigger it manually
        database.addReservedWords(Arrays.asList());

        // in 5.7, the words were not reserved yet
        List<String> reservedForMySQL8 = Arrays.asList("FUNCTION", "ROW", "ROWS");
        for (String reservedWord : reservedForMySQL8) {
            String message = String.format("Expected %s to be non-reserved in MySQL < 8", reservedWord);
            assertFalse(database.isReservedWord(reservedWord), message);
        }
    }

    @Test
    public void verifyMySQL8ReservedWordsArePresent() {
        MySQLDatabase database = new MySQLDatabase();
        MockDatabaseConnection connection = new MockDatabaseConnection();
        connection.setDatabaseMajorVersion(8);
        connection.setDatabaseMinorVersion(0);
        database.setConnection(connection);
        database.addReservedWords(Arrays.asList());

        List<String> reservedForMySQL8 = Arrays.asList("FUNCTION", "ROW", "ROWS");
        // starting with 8.0, they should be reserved
        for (String reservedWord : reservedForMySQL8) {
            String message = String.format("Expected %s to be reserved in MySQL >= 8", reservedWord);
            assertTrue(database.isReservedWord(reservedWord), message);
        }
    }
}
