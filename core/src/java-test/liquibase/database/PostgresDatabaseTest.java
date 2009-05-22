package liquibase.database;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link PostgresDatabase}
 */
public class PostgresDatabaseTest extends AbstractDatabaseTest {

    public PostgresDatabaseTest() throws Exception {
        super(new PostgresDatabase());
    }

    @Override
    protected String getProductNameString() {
        return "PostgreSQL";
    }

    @Override
    @Test
    public void getBlobType() {
        assertEquals(new DataType("BYTEA", false), getDatabase().getBlobType());
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertTrue(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Override
    @Test
    public void getBooleanType() {
        assertEquals(new DataType("BOOLEAN", false), getDatabase().getBooleanType());
    }

    @Override
    @Test
    public void getCurrencyType() {
        assertEquals(new DataType("DECIMAL", true), getDatabase().getCurrencyType());
    }

    @Override
    @Test
    public void getUUIDType() {
        assertEquals(new DataType("CHAR(36)", false), getDatabase().getUUIDType());
    }

    @Override
    @Test
    public void getClobType() {
        assertEquals(new DataType("TEXT", true), getDatabase().getClobType());
    }

    @Override
    @Test
    public void getDateType() {
        assertEquals(new DataType("DATE", false), getDatabase().getDateType());
    }

    @Override
    @Test
    public void getDateTimeType() {
        assertEquals(new DataType("TIMESTAMP WITH TIME ZONE", false), getDatabase().getDateTimeType());
    }

    @Override
    @Test
    public void getCurrentDateTimeFunction() {
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

    public void testGetDefaultDriver() {
        Database database = new PostgresDatabase();

        assertEquals("org.postgresql.Driver", database.getDefaultDriver("jdbc:postgresql://localhost/liquibase"));

        assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

    @Test
    public void getColumnType_BigSerial_AutoIncrement() {
        assertEquals("bigserial", getDatabase().getColumnType("bigserial", Boolean.TRUE));
    }

    @Test
    public void getColumnType_BigInt_AutoIncrement() {
        assertEquals("bigserial", getDatabase().getColumnType("bigint", Boolean.TRUE));
    }

    @Override
    @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("\"tableName\"", database.escapeTableName(null, "tableName"));
    }

    @Test
     public void escapeTableName_reservedWord() {
         Database database = getDatabase();
         assertEquals("\"user\"", database.escapeTableName(null, "user"));
     }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("\"schemaName\".\"tableName\"", database.escapeTableName("schemaName", "tableName"));
    }

}
