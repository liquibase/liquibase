package liquibase.database;

import junit.framework.TestCase;

public class DerbyDatabaseTest extends TestCase {
    public void testGetDefaultDriver() {
        Database database = new DerbyDatabase();

        assertEquals("org.apache.derby.jdbc.EmbeddedDriver", database.getDefaultDriver("jdbc:derby:liquibase;create=true"));

        assertNull(database.getDefaultDriver("jdbc:oracle://localhost;databaseName=liquibase"));
    }

}
