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
    public void testGetBlobType() {
        assertEquals("BLOB", getDatabase().getBlobType());
    }

    @Test
    public void testSupportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Test
    public void testGetBooleanType() {
        assertEquals("TINYINT(1)", getDatabase().getBooleanType());
    }

    @Test
    public void testGetCurrencyType() {
        assertEquals("DECIMAL", getDatabase().getCurrencyType());
    }

    @Test
    public void testGetUUIDType() {
        assertNull(getDatabase().getUUIDType());
    }

    @Test
    public void testGetClobType() {
        assertEquals("TEXT", getDatabase().getClobType());
    }

    @Test
    public void testGetDateType() {
        assertEquals("DATE", getDatabase().getDateType());
    }

    @Test
    public void testGetDateTimeType() {
        assertEquals("DATETIME", getDatabase().getDateTimeType());
    }


    @Test
    public void testGetCurrentDateTimeFunction() {
        assertEquals("NOW()", getDatabase().getCurrentDateTimeFunction());
    }
}
