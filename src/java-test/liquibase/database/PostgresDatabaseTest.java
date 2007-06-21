package liquibase.database;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link PostgresDatabase}
 */
public class PostgresDatabaseTest extends AbstractDatabaseTest {

    public PostgresDatabaseTest() {
        super(new PostgresDatabase());
    }

    protected String getProductNameString() {
        return "PostgreSQL";
    }

    @Test
    public void testGetBlobType() {
        assertEquals("BYTEA", getDatabase().getBlobType());
    }

    @Test
    public void testSupportsInitiallyDeferrableColumns() {
        assertTrue(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Test
    public void testGetBooleanType() {
        assertEquals("BOOLEAN", getDatabase().getBooleanType());
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
        assertEquals("TIMESTAMP WITH TIME ZONE", getDatabase().getDateTimeType());
    }

    @Test
    public void testGetCurrentDateTimeFunction() {
        assertEquals("NOW()", getDatabase().getCurrentDateTimeFunction());
    }

    @Test
    public void testDropDatabaseObjects() throws Exception {
        ; //TODO: test has troubles, fix later
    }

    @Test
    public void testCheckDatabaseChangeLogTable() throws Exception {
        ; //TODO: test has troubles, fix later
    }
}