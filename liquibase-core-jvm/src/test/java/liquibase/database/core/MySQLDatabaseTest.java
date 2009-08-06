package liquibase.database.core;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Assert;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.AbstractDatabaseTest;
import liquibase.database.DataType;
import liquibase.database.Database;

/**
 * Tests for {@link MySQLDatabase}
 */
public class MySQLDatabaseTest extends AbstractDatabaseTest {

    public MySQLDatabaseTest() throws Exception {
        super(new MySQLDatabase());
    }

    @Override
    protected String getProductNameString() {
      return "MySQL";
    }

    @Override
    @Test
    public void getBlobType() {
        Assert.assertEquals(new DataType("BLOB", true), getDatabase().getBlobType());
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Override
    @Test
    public void getBooleanType() {
        assertEquals(new DataType("TINYINT(1)", false), getDatabase().getBooleanType());
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
        assertEquals(new DataType("DATETIME", false), getDatabase().getDateTimeType());
    }


    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        assertEquals("NOW()", getDatabase().getCurrentDateTimeFunction());
    }

    public void testGetDefaultDriver() {
        Database database = new MySQLDatabase();

        assertEquals("com.mysql.jdbc.Driver", database.getDefaultDriver("jdbc:mysql://localhost/liquibase"));

        assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

    @Override
    @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("`tableName`", database.escapeTableName(null, "tableName"));
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("`schemaName`.`tableName`", database.escapeTableName("schemaName", "tableName"));
    }

}
