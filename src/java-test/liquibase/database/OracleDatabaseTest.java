package liquibase.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for {@link OracleDatabase}
 */
public class OracleDatabaseTest extends AbstractDatabaseTest {

    public OracleDatabaseTest() {
        super(new OracleDatabase());
    }

    protected String getProductNameString() {
        return "Oracle";
    }

    @Test
    public void testGetBlobType() {
        assertEquals("BLOB", getDatabase().getBlobType());
    }

    @Test
    public void testSupportsInitiallyDeferrableColumns() {
        assertTrue(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Test
    public void testGetBooleanType() {
        assertEquals("NUMBER(1)", getDatabase().getBooleanType());
    }

    @Test
    public void testGetCurrencyType() {
        assertEquals("NUMBER(15, 2)", getDatabase().getCurrencyType());
    }

    @Test
    public void testGetUUIDType() {
        assertEquals("RAW(16)", getDatabase().getUUIDType());
    }

    @Test
    public void testGetClobType() {
        assertEquals("CLOB", getDatabase().getClobType());
    }

    @Test
    public void testGetDateType() {
        assertEquals("DATE", getDatabase().getDateType());
    }

    @Test
    public void testGetDateTimeType() {
        assertEquals("TIMESTAMP", getDatabase().getDateTimeType());
    }

    @Test
    public void testGetCurrentDateTimeFunction() {
        assertEquals("SYSDATE", getDatabase().getCurrentDateTimeFunction());
    }
}
