package liquibase.database.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;

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
    public void getDefaultDriver() throws DatabaseException {
        try (Database database = new MSSQLDatabase()) {
            assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", database.getDefaultDriver("jdbc:sqlserver://localhost;databaseName=liquibase"));

            assertNull(database.getDefaultDriver("jdbc:oracle:thin://localhost;databaseName=liquibase"));
        } catch (final DatabaseException e) {
            throw e;
        }
    }

    @Override
    @Test
    public void escapeTableName_noSchema() throws DatabaseException {
        try (Database database = new MSSQLDatabase()) {
            assertEquals("tableName", database.escapeTableName(null, null, "tableName"));
            assertEquals("[tableName€]", database.escapeTableName(null, null, "tableName€"));
        } catch (final DatabaseException e) {
            throw e;
        }
    }

    @Override
    @Test
    public void escapeTableName_withSchema() throws DatabaseException {
        try (Database database = new MSSQLDatabase()) {
            assertEquals("catalogName.schemaName.tableName", database.escapeTableName("catalogName", "schemaName", "tableName"));
            assertEquals("[catalogName€].[schemaName€].[tableName€]", database.escapeTableName("catalogName€", "schemaName€", "tableName€"));
        } catch (final DatabaseException e) {
            throw e;
        }
    }

    private Database createOfflineDatabase(String url) throws Exception {
        return DatabaseFactory.getInstance().openDatabase(url, null, null, null, null);
    }

    @Test
    public void setDefaultSchemaName() throws Exception {
        //
        // No exception should be thrown by call to setDefaultSchemaName
        //
        final Database database = createOfflineDatabase("offline:mssql");
        database.setDefaultSchemaName("MySchema");
    }

    @Test
    public void isUnmodifiable() throws Exception {
        final Database database = createOfflineDatabase("offline:mssql");
        assertTrue(database instanceof MSSQLDatabase);
        final MSSQLDatabase mssqlDatabase = (MSSQLDatabase) database;
        assertTrue(mssqlDatabase.dataTypeIsNotModifiable("datetime"));
    }

//    @Test
//    public void changeDefaultSchemaToAllowedValue() throws Exception {
//        Database database = new MSSQLDatabase();
//        Database dbSpy = PowerMockito.spy(database);
//        when(dbSpy, method(MSSQLDatabase.class, "getConnectionSchemaName", null)).withNoArguments().thenReturn
//            ("myschema");
//        assertNull(dbSpy.getDefaultSchemaName());
//
//        dbSpy.setDefaultSchemaName("myschema");
//        assertEquals("myschema", dbSpy.getDefaultSchemaName());
//    }
//
//    @Test
//    public void changeDefaultSchemaToNull() throws Exception {
//        Database database = new MSSQLDatabase();
//        Database dbSpy = PowerMockito.spy(database);
//        when(dbSpy, method(MSSQLDatabase.class, "getConnectionSchemaName", null)).withNoArguments().thenReturn
//            ("myschema");
//        assertNull(dbSpy.getDefaultSchemaName());
//
//        dbSpy.setDefaultSchemaName(null);
//        assertNull("Changing the default schema to null should be successful.", dbSpy.getDefaultSchemaName());
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void changeDefaultSchemaToForbiddenValue() throws Exception {
//        Database database = new MSSQLDatabase();
//        Database dbSpy = PowerMockito.spy(database);
//        when(dbSpy, method(MSSQLDatabase.class, "getConnectionSchemaName", null)).withNoArguments().thenReturn
//            ("myschema");
//        assertNull(dbSpy.getDefaultSchemaName());
//
//        dbSpy.setDefaultSchemaName("some_other_schema");
//    }

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
