package liquibase.database.core;

import liquibase.database.AbstractDatabaseTest;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

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
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
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
        assertEquals("[dbo].[tableName]", database.escapeTableName(null, null, "tableName"));
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = new MSSQLDatabase();
        assertEquals("[catalogName].[schemaName].[tableName]", database.escapeTableName("catalogName", "schemaName", "tableName"));
    }

    @Test
    public void changeDefaultSchema() throws DatabaseException {
        Database database = new MSSQLDatabase();
        assertEquals("dbo", database.getDefaultSchemaName());

        database.setDefaultSchemaName("myschema");
        assertEquals("myschema", database.getDefaultSchemaName());
    }
}
