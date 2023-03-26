package liquibase.database.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import org.junit.jupiter.api.Test;

public class DB2zDatabaseTest extends AbstractDb2DatabaseTest<Db2zDatabase> {

    public DB2zDatabaseTest() throws Exception {
        super(new Db2zDatabase());
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
        Database database = new Db2zDatabase();
        assertEquals(12, database.getMaxFractionalDigitsForTimestamp());
    }
}
