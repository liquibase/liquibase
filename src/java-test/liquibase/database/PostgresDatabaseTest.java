package liquibase.database;

import junit.framework.*;
import static org.easymock.classextension.EasyMock.*;
import liquibase.database.PostgresDatabase;

import java.sql.Connection;
import java.sql.SQLException;

public class PostgresDatabaseTest extends AbstractDatabaseTest {
    public PostgresDatabaseTest() {
        super(new PostgresDatabase());
    }

    public void testGetBlobType() {
        assertEquals("BYTEA", getDatabase().getBlobType());
    }

    public void testSupportsInitiallyDeferrableColumns() {
        assertTrue(getDatabase().supportsInitiallyDeferrableColumns());
    }

    public void testGetBooleanType() {
        assertEquals("BOOLEAN", getDatabase().getBooleanType());
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
        assertEquals("TIMESTAMP", getDatabase().getDateTimeType());
    }

    protected String getProductNameString() {
        return "PostgreSQL";
    }

   public void testGetCurrentDateTimeFunction() {
        assertEquals("CURRENT_DATE", getDatabase().getCurrentDateTimeFunction());
    }

    public void testDropDatabaseObjects() throws Exception {
        ; //TODO: test has troubles, fix later
    }

    public void testCheckDatabaseChangeLogTable() throws Exception {
        ; //TODO: test has troubles, fix later
    }
}