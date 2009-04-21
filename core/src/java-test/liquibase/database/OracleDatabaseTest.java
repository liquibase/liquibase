package liquibase.database;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link OracleDatabase}.
 */
public class OracleDatabaseTest extends AbstractDatabaseTest {

    public OracleDatabaseTest() throws Exception {
        super(new OracleDatabase());
    }

    protected String getProductNameString() {
        return "Oracle";
    }

    @Test
    public void getBlobType() {
        assertEquals(new DataType("BLOB", false), getDatabase().getBlobType());
    }

    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertTrue(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Test
    public void getBooleanType() {
        assertEquals(new DataType("NUMBER(1)", false), getDatabase().getBooleanType());
    }

    @Test
    public void getCurrencyType() {
        assertEquals(new DataType("NUMBER(15, 2)", false), getDatabase().getCurrencyType());
    }

    @Test
    public void getUUIDType() {
        assertEquals(new DataType("RAW(16)", false), getDatabase().getUUIDType());
    }

    @Test
    public void getClobType() {
        assertEquals(new DataType("CLOB", false), getDatabase().getClobType());
    }

    @Test
    public void getDateType() {
        assertEquals(new DataType("DATE", false), getDatabase().getDateType());
    }

    @Test
    public void getDateTimeType() {
        assertEquals(new DataType("TIMESTAMP", true), getDatabase().getDateTimeType());
    }

    @Test
    public void getCurrentDateTimeFunction() {
        assertEquals("SYSDATE", getDatabase().getCurrentDateTimeFunction());
    }

    public void testGetDefaultDriver() {
        Database database = new OracleDatabase();

        assertEquals("oracle.jdbc.OracleDriver", database.getDefaultDriver("jdbc:oracle:thin:@localhost/XE"));

        assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

}

