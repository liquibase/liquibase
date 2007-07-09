package liquibase.database;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link MySQLDatabase}
 */
public class MySQLDatabaseTest extends AbstractDatabaseTest {

    public MySQLDatabaseTest() {
        super(new MySQLDatabase());
    }

    protected String getProductNameString() {
      return "MySQL";
    }

    @Test
    public void getBlobType() {
        assertEquals("BLOB", getDatabase().getBlobType());
    }

    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Test
    public void getBooleanType() {
        assertEquals("TINYINT(1)", getDatabase().getBooleanType());
    }

    @Test
    public void getCurrencyType() {
        assertEquals("DECIMAL", getDatabase().getCurrencyType());
    }

    @Test
    public void getUUIDType() {
        assertNull(getDatabase().getUUIDType());
    }

    @Test
    public void getClobType() {
        assertEquals("TEXT", getDatabase().getClobType());
    }

    @Test
    public void getDateType() {
        assertEquals("DATE", getDatabase().getDateType());
    }

    @Test
    public void getDateTimeType() {
        assertEquals("DATETIME", getDatabase().getDateTimeType());
    }


    @Test
    public void getCurrentDateTimeFunction() {
        assertEquals("NOW()", getDatabase().getCurrentDateTimeFunction());
    }

    public void testGetDefaultDriver() {
        Database database = new MySQLDatabase();

        assertEquals("com.mysql.jdbc.Driver", database.getDefaultDriver("jdbc:mysql://localhost/liquibase"));

        assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

}
