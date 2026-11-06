package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.database.MockDatabaseConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link MariaDBDatabase}
 */
public class MariaDBDatabaseTest extends AbstractJdbcDatabaseTest {

    public MariaDBDatabaseTest() throws Exception {
        super(new MariaDBDatabase());
    }

    @Override
    protected String getProductNameString() {
      return "MariaDB";
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
    public void testGetDefaultDriver() {
        assertEquals("org.mariadb.jdbc.Driver", this.database.getDefaultDriver("jdbc:mariadb://localhost/liquibase"));

        assertNull(this.database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

    @Override
    @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("tableName", database.escapeTableName(null, null, "tableName"));
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("catalogName.tableName", database.escapeTableName("catalogName", "schemaName", "tableName"));
    }

    @Test
    public void escapeStringForDatabase_withBackslashes() {
        assertEquals("\\\\0", database.escapeStringForDatabase("\\0"));
    }

    @ParameterizedTest(name = "MariaDB {0}.{1}: {2} reserved={3}")
    @MethodSource("reservedWordMatrix")
    public void verifyMariaDBReservedWords(int major, int minor, String word, boolean expectedReserved) {
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
        List<String> alwaysReserved = List.of(
                // MariaDB-specific reserved words
                "CURRENT_ROLE", "DELETE_DOMAIN_ID", "DO_DOMAIN_IDS", "GENERAL", "IGNORE_DOMAIN_IDS",
                "IGNORE_SERVER_IDS", "MASTER_HEARTBEAT_PERIOD", "OFFSET", "PAGE_CHECKSUM", "PARSE_VCOL_EXPR",
                "REF_SYSTEM_ID", "RETURNING", "ROW_NUMBER", "SLOW", "STATS_AUTO_RECALC", "STATS_PERSISTENT",
                "STATS_SAMPLE_PAGES", "VECTOR", "PERIOD",
                // re-added by MariaDBDatabase
                "MASTER_SSL_VERIFY_SERVER_CERT",
                // inherited MySQL 5.7 base reserved words
                "SELECT", "TABLE", "ACCESSIBLE",
                // inherited MySQL version-specific reserved words (registered during construction)
                "FUNCTION", "ROW", "ROWS", "LIBRARY", "EXTERNAL", "SETS"
        );
        List<String> neverReserved = List.of(
                // dropped from the MySQL keyword set and not re-added by MariaDB
                "MASTER_BIND",
                // not a keyword at all
                "SOMETHING_ELSE"
        );
        return Stream.of(
                forVersion(5, 5, alwaysReserved, neverReserved),
                forVersion(10, 6, alwaysReserved, neverReserved),
                forVersion(11, 4, alwaysReserved, neverReserved)
        ).flatMap(s -> s);
    }

    private static Stream<Arguments> forVersion(int major, int minor, List<String> reserved, List<String> notReserved) {
        return Stream.concat(
                reserved.stream().map(word -> Arguments.of(major, minor, word, true)),
                notReserved.stream().map(word -> Arguments.of(major, minor, word, false))
        );
    }

    @Test
    public void isReservedWordIsCaseInsensitive() {
        Database database = getDatabase();
        assertTrue(database.isReservedWord("RETURNING"));
        assertTrue(database.isReservedWord("returning"));
        assertTrue(database.isReservedWord("ReTuRnInG"));
        assertTrue(database.isReservedWord("master_ssl_verify_server_cert"));
    }
}
