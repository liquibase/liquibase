package liquibase.database;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link MSSQLDatabase}
 */
public class MSSQLDatabaseTest extends AbstractDatabaseTest {

    public MSSQLDatabaseTest() throws Exception {
        super(new MSSQLDatabase());
    }

    protected String getProductNameString() {
        return "Microsoft SQL Server";
    }

    @Test
    public void getBlobType() {
        assertEquals(new DataType("IMAGE", true), getDatabase().getBlobType());
    }

    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Test
    public void getBooleanType() {
        assertEquals(new DataType("BIT", false), getDatabase().getBooleanType());
    }

    @Test
    public void getCurrencyType() {
        assertEquals(new DataType("MONEY", false), getDatabase().getCurrencyType());
    }

    @Test
    public void getUUIDType() {
        assertEquals(new DataType("UNIQUEIDENTIFIER", false), getDatabase().getUUIDType());
    }

    @Test
    public void getClobType() {
        assertEquals(new DataType("TEXT", true), getDatabase().getClobType());
    }

    @Test
    public void getDateType() {
        assertEquals(new DataType("SMALLDATETIME", false), getDatabase().getDateType());
    }

    @Test
    public void getDateTimeType() {
        assertEquals(new DataType("DATETIME", false), getDatabase().getDateTimeType());
    }

    @Test
    public void getCurrentDateTimeFunction() {
        assertEquals("GETDATE()", getDatabase().getCurrentDateTimeFunction());
    }

    @Test
    public void getDefaultDriver() {
        Database database = new MSSQLDatabase();

        assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", database.getDefaultDriver("jdbc:sqlserver://localhost;databaseName=liquibase"));

        assertNull(database.getDefaultDriver("jdbc:oracle:thin://localhost;databaseName=liquibase"));
    }

    @Test
    public void escapeTableName_noSchema() {
        Database database = new MSSQLDatabase();
        assertEquals("[dbo].[tableName]", database.escapeTableName(null, "tableName"));
    }

    @Test
    public void escapeTableName_withSchema() {
        Database database = new MSSQLDatabase();
        assertEquals("[schemaName].[tableName]", database.escapeTableName("schemaName", "tableName"));
    }
}
