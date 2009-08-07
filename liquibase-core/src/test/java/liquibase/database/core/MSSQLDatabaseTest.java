package liquibase.database.core;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Assert;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.AbstractDatabaseTest;
import liquibase.database.DataType;
import liquibase.database.Database;

/**
 * Tests for {@link MSSQLDatabase}
 */
public class MSSQLDatabaseTest extends AbstractDatabaseTest {

    public MSSQLDatabaseTest() throws Exception {
        super(new MSSQLDatabase());
    }

    @Override
    protected String getProductNameString() {
        return "Microsoft SQL Server";
    }

    @Override
    @Test
    public void getBlobType() {
        Assert.assertEquals(new DataType("IMAGE", true), getDatabase().getBlobType());
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Override
    @Test
    public void getBooleanType() {
        Assert.assertEquals(new DataType("BIT", false), getDatabase().getBooleanType());
    }

    @Override
    @Test
    public void getCurrencyType() {
        Assert.assertEquals(new DataType("MONEY", false), getDatabase().getCurrencyType());
    }

    @Override
    @Test
    public void getUUIDType() {
        Assert.assertEquals(new DataType("UNIQUEIDENTIFIER", false), getDatabase().getUUIDType());
    }

    @Override
    @Test
    public void getClobType() {
        Assert.assertEquals(new DataType("TEXT", true), getDatabase().getClobType());
    }

    @Override
    @Test
    public void getDateType() {
        Assert.assertEquals(new DataType("SMALLDATETIME", false), getDatabase().getDateType());
    }

    @Override
    @Test
    public void getDateTimeType() {
        Assert.assertEquals(new DataType("DATETIME", false), getDatabase().getDateTimeType());
    }

    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        Assert.assertEquals("GETDATE()", getDatabase().getCurrentDateTimeFunction());
    }

    @Test
    public void getDefaultDriver() {
        Database database = new MSSQLDatabase();

        assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", database.getDefaultDriver("jdbc:sqlserver://localhost;databaseName=liquibase"));

        assertNull(database.getDefaultDriver("jdbc:oracle:thin://localhost;databaseName=liquibase"));
    }

    @Override
    @Test
    public void escapeTableName_noSchema() {
        Database database = new MSSQLDatabase();
        assertEquals("[dbo].[tableName]", database.escapeTableName(null, "tableName"));
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = new MSSQLDatabase();
        assertEquals("[schemaName].[tableName]", database.escapeTableName("schemaName", "tableName"));
    }
}
