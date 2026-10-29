package liquibase.database.core;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.structure.core.Table;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DB2DatabaseTest {

    @Test
    public void testGetDefaultDriver() throws DatabaseException {
        try (Database database = new DB2Database()) {
            assertEquals("com.ibm.db2.jcc.DB2Driver", database.getDefaultDriver("jdbc:db2://localhost:50000/liquibas"));

            assertNull(database.getDefaultDriver("jdbc:oracle://localhost;databaseName=liquibase"));
        } catch (final DatabaseException e) {
            throw e;
        }
    }

    @Test
    public void testSupportsCreateIfNotExist() throws DatabaseException {
        try (DB2Database db2Database = new DB2Database()) {
            assertTrue(db2Database.supportsCreateIfNotExists(Table.class));
        }

    }

    @Test
    public void testMaxFractionDigits() {
        Database database = new DB2Database();
        assertEquals(12, database.getMaxFractionalDigitsForTimestamp());
    }
}
