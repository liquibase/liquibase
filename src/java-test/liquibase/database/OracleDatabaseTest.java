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
    public void getBlobType() {
        assertEquals("BLOB", getDatabase().getBlobType());
    }

    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertTrue(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Test
    public void getBooleanType() {
        assertEquals("NUMBER(1)", getDatabase().getBooleanType());
    }

    @Test
    public void getCurrencyType() {
        assertEquals("NUMBER(15, 2)", getDatabase().getCurrencyType());
    }

    @Test
    public void getUUIDType() {
        assertEquals("RAW(16)", getDatabase().getUUIDType());
    }

    @Test
    public void getClobType() {
        assertEquals("CLOB", getDatabase().getClobType());
    }

    @Test
    public void getDateType() {
        assertEquals("DATE", getDatabase().getDateType());
    }

    @Test
    public void getDateTimeType() {
        assertEquals("TIMESTAMP", getDatabase().getDateTimeType());
    }

    @Test
    public void getCurrentDateTimeFunction() {
        assertEquals("SYSDATE", getDatabase().getCurrentDateTimeFunction());
    }
}
