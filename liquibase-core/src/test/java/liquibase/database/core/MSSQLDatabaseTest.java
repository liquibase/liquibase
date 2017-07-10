package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link MSSQLDatabase}
 */
public class MSSQLDatabaseTest extends AbstractJdbcDatabaseTest {

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
        assertEquals("GETDATE()", getDatabase().getCurrentDateTimeFunction());
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
        assertEquals("tableName", database.escapeTableName(null, null, "tableName"));
        assertEquals("[tableName€]", database.escapeTableName(null, null, "tableName€"));
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = new MSSQLDatabase();
        assertEquals("catalogName.schemaName.tableName", database.escapeTableName("catalogName", "schemaName",
            "tableName"));
        assertEquals("[catalogName€].[schemaName€].[tableName€]", database.escapeTableName("catalogName€",
            "schemaName€", "tableName€"));
    }

    @Test
    public void changeDefaultSchema() throws DatabaseException {
        Database database = new MSSQLDatabase();
        assertNull(database.getDefaultSchemaName());

        database.setDefaultSchemaName("myschema");
        assertEquals("myschema", database.getDefaultSchemaName());
    }

    @Test
    public void testEscapeDataTypeName() {
        Database database = getDatabase();
        assertEquals("MySchema.MyUDT", database.escapeDataTypeName("MySchema.MyUDT"));
        assertEquals("[MySchema€].[MyUDT€]", database.escapeDataTypeName("MySchema€.MyUDT€"));
        assertEquals("MySchema.[MyUDT]", database.escapeDataTypeName("MySchema.[MyUDT]"));
        assertEquals("[MySchema].MyUDT", database.escapeDataTypeName("[MySchema].MyUDT"));
        assertEquals("[MySchema].[MyUDT]", database.escapeDataTypeName("[MySchema].[MyUDT]"));
    }

    @Test
    public void testUnescapeDataTypeName() {
        Database database = getDatabase();
        assertEquals("MySchema.MyUDT", database.unescapeDataTypeName("MySchema.MyUDT"));
        assertEquals("MySchema.MyUDT", database.unescapeDataTypeName("MySchema.[MyUDT]"));
        assertEquals("MySchema.MyUDT", database.unescapeDataTypeName("[MySchema].MyUDT"));
        assertEquals("MySchema.MyUDT", database.unescapeDataTypeName("[MySchema].[MyUDT]"));
    }

    @Test
    public void testUnescapeDataTypeString() {
        Database database = getDatabase();
        assertEquals("int", database.unescapeDataTypeString("int"));
        assertEquals("int", database.unescapeDataTypeString("[int]"));
        assertEquals("decimal(19, 2)", database.unescapeDataTypeString("decimal(19, 2)"));
        assertEquals("decimal(19, 2)", database.unescapeDataTypeString("[decimal](19, 2)"));
    }
}
