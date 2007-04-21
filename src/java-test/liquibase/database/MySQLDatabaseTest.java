package liquibase.database;

public class MySQLDatabaseTest  extends AbstractDatabaseTest {
    public MySQLDatabaseTest() {
        super(new MySQLDatabase());
    }

    public void testGetBlobType() {
        assertEquals("BLOB", getDatabase().getBlobType());
    }

    public void testSupportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    public void testGetBooleanType() {
        assertEquals("TINYINT(1)", getDatabase().getBooleanType());
    }

    public void testGetCurrencyType() {
        assertEquals("DECIMAL", getDatabase().getCurrencyType());
    }

    public void testGetUUIDType() {
        assertNull(getDatabase().getUUIDType());
    }

    public void testGetClobType() {
        assertEquals("TEXT", getDatabase().getClobType());
    }

    public void testGetDateType() {
        assertEquals("DATE", getDatabase().getDateType());
    }

    public void testGetDateTimeType() {
        assertEquals("DATETIME", getDatabase().getDateTimeType());
    }

    protected String getProductNameString() {
        return "MySQL";
    }

    public void testGetCurrentDateTimeFunction() {
        assertEquals("NOW()", getDatabase().getCurrentDateTimeFunction());
    }
}
