package liquibase.database.core;

import junit.framework.TestCase;
import liquibase.exception.DatabaseException;

public class AbstractDb2DatabaseTest extends TestCase {
    public void testGetDateLiteral() {
        try (AbstractDb2Database database = new DB2Database()) {
          assertEquals("DATE('2018-12-31')", database.getDateLiteral("2018-12-31"));
          assertEquals("TIME('23:58:59')", database.getDateLiteral("23:58:59"));
          assertEquals("TIMESTAMP('2018-12-31 23:58:59')", database.getDateLiteral("2018-12-31 23:58:59"));
          assertEquals("UNSUPPORTED:foo", database.getDateLiteral("foo"));
        } catch (DatabaseException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
    }
}
