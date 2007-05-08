package liquibase.database;

public class OracleDatabaseTest extends AbstractDatabaseTest {

    public OracleDatabaseTest() {
        super(new OracleDatabase());
    }

    public void testGetBlobType() {
        assertEquals("BLOB", getDatabase().getBlobType());
    }

    public void testSupportsInitiallyDeferrableColumns() {
        assertTrue(getDatabase().supportsInitiallyDeferrableColumns());
    }

    public void testGetBooleanType() {
        assertEquals("NUMBER(1)", getDatabase().getBooleanType());
    }

    public void testGetCurrencyType() {
        assertEquals("NUMBER(15, 2)", getDatabase().getCurrencyType());
    }

    public void testGetUUIDType() {
        assertEquals("RAW(16)", getDatabase().getUUIDType());
    }

    public void testGetClobType() {
        assertEquals("CLOB", getDatabase().getClobType());
    }

    public void testGetDateType() {
        assertEquals("DATE", getDatabase().getDateType());
    }

    public void testGetDateTimeType() {
        assertEquals("TIMESTAMP", getDatabase().getDateTimeType());
    }

    protected String getProductNameString() {
        return "Oracle";
    }

    public void testGetCurrentDateTimeFunction() {
        assertEquals("SYSDATE", getDatabase().getCurrentDateTimeFunction());
    }
}
