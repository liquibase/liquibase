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

    protected String getProductNameString() {
        return "PostgreSQL";
    }

    @Test
    public void getBlobType() {
        assertEquals(new DataType("BYTEA", false), getDatabase().getBlobType());
    }

    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertTrue(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Test
    public void getBooleanType() {
        assertEquals(new DataType("BOOLEAN", false), getDatabase().getBooleanType());
    }

    @Test
    public void getCurrencyType() {
        assertEquals(new DataType("DECIMAL", true), getDatabase().getCurrencyType());
    }

    @Test
    public void getUUIDType() {
        assertEquals(new DataType("CHAR(36)", false), getDatabase().getUUIDType());
    }

    @Test
    public void getClobType() {
        assertEquals(new DataType("TEXT", true), getDatabase().getClobType());
    }

    @Test
    public void getDateType() {
        assertEquals(new DataType("DATE", false), getDatabase().getDateType());
    }

    @Test
    public void getDateTimeType() {
        assertEquals(new DataType("TIMESTAMP WITH TIME ZONE", false), getDatabase().getDateTimeType());
    }

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

    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("schemaName.\"tableName\"", database.escapeTableName("schemaName", "tableName"));
    }

}
