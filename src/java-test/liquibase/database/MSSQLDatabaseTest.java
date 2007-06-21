package liquibase.database;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for {@link MSSQLDatabase}
 */
public class MSSQLDatabaseTest extends AbstractDatabaseTest {

    public MSSQLDatabaseTest() {
        super(new MSSQLDatabase());
    }

    protected String getProductNameString() {
        return "Microsoft SQL Server";
    }

    @Test
    public void testGetBlobType() {
        assertEquals("IMAGE", getDatabase().getBlobType());
    }

    @Test
    public void testSupportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Test
    public void testGetBooleanType() {
        assertEquals("BIT", getDatabase().getBooleanType());
    }

    @Test
    public void testGetCurrencyType() {
        assertEquals("MONEY", getDatabase().getCurrencyType());
    }

    @Test
    public void testGetUUIDType() {
        assertEquals("UNIQUEIDENTIFIER", getDatabase().getUUIDType());
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
        assertEquals("GETDATE()", getDatabase().getCurrentDateTimeFunction());
    }
}