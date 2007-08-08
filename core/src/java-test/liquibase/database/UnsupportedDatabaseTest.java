package liquibase.database;

import junit.framework.TestCase;

public class UnsupportedDatabaseTest extends TestCase {
    public void testGetDefaultDriver() {
        Database database = new UnsupportedDatabase();

        assertNull(database.getDefaultDriver("jdbc:oracle://localhost;databaseName=liquibase"));
        assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
        assertNull(database.getDefaultDriver("jdbc:hsqldb://localhost;databaseName=liquibase"));
        assertNull(database.getDefaultDriver("jdbc:derby://localhost;databaseName=liquibase"));
        assertNull(database.getDefaultDriver("jdbc:sqlserver://localhost;databaseName=liquibase"));
        assertNull(database.getDefaultDriver("jdbc:postgresql://localhost;databaseName=liquibase"));
    }

}
