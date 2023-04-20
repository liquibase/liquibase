package liquibase.database.core;

import junit.framework.TestCase;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;

public class UnsupportedDatabaseTest extends TestCase {

    public void testGetDefaultDriver() throws DatabaseException {
        try (Database database = new UnsupportedDatabase()) {
            assertNull(database.getDefaultDriver("jdbc:oracle://localhost;databaseName=liquibase"));
            assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
            assertNull(database.getDefaultDriver("jdbc:hsqldb://localhost;databaseName=liquibase"));
            assertNull(database.getDefaultDriver("jdbc:derby://localhost;databaseName=liquibase"));
            assertNull(database.getDefaultDriver("jdbc:sqlserver://localhost;databaseName=liquibase"));
            assertNull(database.getDefaultDriver("jdbc:postgresql://localhost;databaseName=liquibase"));
        } catch (final DatabaseException e) {
            throw e;
        }
    }

}
