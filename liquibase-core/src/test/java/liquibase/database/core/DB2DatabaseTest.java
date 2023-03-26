package liquibase.database.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import org.junit.jupiter.api.Test;

public class DB2DatabaseTest extends AbstractDb2DatabaseTest<DB2Database> {

    public DB2DatabaseTest() throws Exception {
        super(new DB2Database());
    }

    @Test
    public void testGetDefaultDriver() throws DatabaseException {
        try (Database database = getDatabase()) {
            assertEquals("com.ibm.db2.jcc.DB2Driver", database.getDefaultDriver("jdbc:db2://localhost:50000/liquibas"));

            assertNull(database.getDefaultDriver("jdbc:oracle://localhost;databaseName=liquibase"));
        }
    }

    @Test
    public void testMaxFractionDigits() {
        Database database = getDatabase();
        assertEquals(12, database.getMaxFractionalDigitsForTimestamp());
    }
}
