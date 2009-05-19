package liquibase.database;

import static org.junit.Assert.*;
import org.junit.Test;

public class H2DatabaseTest extends AbstractDatabaseTest {

    public H2DatabaseTest() throws Exception {
        super(new H2Database());
    }

    @Override
    protected String getProductNameString() {
        return "H2";
    }

    @Override
    @Test
    public void getBlobType() {
        assertEquals(new DataType("LONGVARBINARY", true), getDatabase().getBlobType());
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
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
        assertEquals(new DataType("VARCHAR(36)", false), getDatabase().getUUIDType());
    }

    @Override
    @Test
    public void getClobType() {
        assertEquals(new DataType("LONGVARCHAR", true), getDatabase().getClobType());
    }

    @Override
    @Test
    public void getDateType() {
        assertEquals(new DataType("DATE", false), getDatabase().getDateType());
    }

    @Override
    @Test
    public void getDateTimeType() {
        assertEquals(new DataType("TIMESTAMP", false), getDatabase().getDateTimeType());
    }

    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        assertEquals("NOW()", getDatabase().getCurrentDateTimeFunction());
    }

    @Test
    public void testGetDefaultDriver() {
        Database database = getDatabase();

        assertEquals("org.h2.Driver", database.getDefaultDriver("jdbc:h2:mem:liquibase"));

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
