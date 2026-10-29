package liquibase.database.core;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.structure.core.Table;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DB2zDatabaseTest {

    @Test
    public void testGetDefaultDriver() throws DatabaseException {
        try (Database database = new Db2zDatabase()) {
            assertEquals("com.ibm.db2.jcc.DB2Driver", database.getDefaultDriver("jdbc:db2://localhost:50000/liquibas"));

            assertNull(database.getDefaultDriver("jdbc:oracle://localhost;databaseName=liquibase"));
        } catch (final DatabaseException e) {
            throw e;
        }
    }

    @Test
    public void testSupportsCreateIfNotExist() throws DatabaseException {
        try (Db2zDatabase db2zDatabase = new Db2zDatabase()) {
            assertFalse(db2zDatabase.supportsCreateIfNotExists(Table.class));
        }

    }

    @Test
    public void testMaxFractionDigits() {
        Database database = new Db2zDatabase();
        assertEquals(12, database.getMaxFractionalDigitsForTimestamp());
    }
}
