package liquibase.database.core;

import junit.framework.TestCase;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;

public class DB2DatabaseTest extends TestCase {
    public void testGetDefaultDriver() {
        try (Database database = new DB2Database()) {
          assertEquals("com.ibm.db2.jcc.DB2Driver", database.getDefaultDriver("jdbc:db2://localhost:50000/liquibas"));

          assertNull(database.getDefaultDriver("jdbc:oracle://localhost;databaseName=liquibase"));
        } catch (DatabaseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
    }


}
