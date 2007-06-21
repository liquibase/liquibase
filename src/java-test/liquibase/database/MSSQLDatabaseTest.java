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
    public void getBlobType() {
        assertEquals("IMAGE", getDatabase().getBlobType());
    }

    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Test
    public void getBooleanType() {
        assertEquals("BIT", getDatabase().getBooleanType());
    }

    @Test
    public void getCurrencyType() {
        assertEquals("MONEY", getDatabase().getCurrencyType());
    }

    @Test
    public void getUUIDType() {
        assertEquals("UNIQUEIDENTIFIER", getDatabase().getUUIDType());
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
        assertEquals("GETDATE()", getDatabase().getCurrentDateTimeFunction());
    }
}