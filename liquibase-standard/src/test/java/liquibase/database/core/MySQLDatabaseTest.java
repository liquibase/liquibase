package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.database.MockDatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.statement.DatabaseFunction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.stream.Stream;

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

    @ParameterizedTest(name = "MySQL {0}.{1}: {2} reserved={3}")
    @MethodSource("reservedWordMatrix")
    public void verifyMySQLReservedWords(int major, int minor, String word, boolean expectedReserved) {
        Database database = getDatabase();
        MockDatabaseConnection connection = new MockDatabaseConnection();
        connection.setDatabaseMajorVersion(major);
        connection.setDatabaseMinorVersion(minor);
        database.setConnection(connection);
        // attaching the connection should trigger calling "addReservedWords" - as the
        // MockDatabaseConnection does not do that, we trigger it manually
        database.addReservedWords(List.of());

        assertEquals(expectedReserved, database.isReservedWord(word));
    }

    static Stream<Arguments> reservedWordMatrix() {
        return Stream.of(
                forVersion(5, 7, List.of("MASTER_SSL_VERIFY_SERVER_CERT"), List.of("FUNCTION", "ROW", "ROWS", "PERIOD", "CURRENT_ROLE")),
                forVersion(8, 0, List.of("FUNCTION", "ROW", "ROWS", "MASTER_BIND"), List.of()),
                forVersion(8, 4, List.of("FUNCTION", "ROW", "ROWS", "MANUAL"), List.of("MASTER_SSL_VERIFY_SERVER_CERT")),
                // no changes in 9.0
                forVersion(9, 0, List.of("FUNCTION", "ROW", "ROWS", "MANUAL"), List.of("MASTER_SSL_VERIFY_SERVER_CERT", "LIBRARY")),
                forVersion(9, 3, List.of("LIBRARY", "MANUAL", "SYSTEM"), List.of("MASTER_BIND", "EXTERNAL")),
                forVersion(9, 4, List.of("EXTERNAL", "LIBRARY", "SYSTEM"), List.of("SETS", "MASTER_BIND", "PERIOD")),
                forVersion(9, 6, List.of("SETS", "EXTERNAL", "LIBRARY", "CUBE", "ACCESSIBLE"), List.of("SOMETHING_ELSE", "MASTER_BIND")),
                forVersion(10, 0, List.of("SETS", "EXTERNAL", "LIBRARY", "CUBE", "ACCESSIBLE"), List.of("SOMETHING_ELSE", "MASTER_BIND"))
        ).flatMap(s -> s);
    }

    private static Stream<Arguments> forVersion(int major, int minor, List<String> reserved, List<String> notReserved) {
        return Stream.concat(
                reserved.stream().map(word -> Arguments.of(major, minor, word, true)),
                notReserved.stream().map(word -> Arguments.of(major, minor, word, false))
        );
    }
}
