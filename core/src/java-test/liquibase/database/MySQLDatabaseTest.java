package liquibase.database;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link MySQLDatabase}
 */
public class MySQLDatabaseTest extends AbstractDatabaseTest {

    public MySQLDatabaseTest() throws Exception {
        super(new MySQLDatabase());
    }

    protected String getProductNameString() {
      return "MySQL";
    }

    @Test
    public void getBlobType() {
        assertEquals(new DataType("BLOB", true), getDatabase().getBlobType());
    }

    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Test
    public void getBooleanType() {
        assertEquals(new DataType("TINYINT(1)", false), getDatabase().getBooleanType());
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
        assertEquals(new DataType("DATETIME", false), getDatabase().getDateTimeType());
    }


    @Test
    public void getCurrentDateTimeFunction() {
        assertEquals("NOW()", getDatabase().getCurrentDateTimeFunction());
    }

    public void testGetDefaultDriver() {
        Database database = new MySQLDatabase();

        assertEquals("com.mysql.jdbc.Driver", database.getDefaultDriver("jdbc:mysql://localhost/liquibase"));

        assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

    @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("`tableName`", database.escapeTableName(null, "tableName"));
    }

    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("`schemaName`.`tableName`", database.escapeTableName("schemaName", "tableName"));
    }

}
