package liquibase.database;

import static org.junit.Assert.*;
import org.junit.Test;

public class H2DatabaseTest extends AbstractDatabaseTest {

    public H2DatabaseTest() throws Exception {
        super(new H2Database());
    }

    protected String getProductNameString() {
        return "H2";
    }

    @Test
    public void getBlobType() {
        assertEquals("LONGVARBINARY", getDatabase().getBlobType());
    }

    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Test
    public void getBooleanType() {
        assertEquals("BOOLEAN", getDatabase().getBooleanType());
    }

    @Test
    public void getCurrencyType() {
        assertEquals("DECIMAL", getDatabase().getCurrencyType());
    }

    @Test
    public void getUUIDType() {
        assertEquals("VARCHAR(36)", getDatabase().getUUIDType());
    }

    @Test
    public void getClobType() {
        assertEquals("LONGVARCHAR", getDatabase().getClobType());
    }

    @Test
    public void getDateType() {
        assertEquals("DATE", getDatabase().getDateType());
    }

    @Test
    public void getDateTimeType() {
        assertEquals("TIMESTAMP", getDatabase().getDateTimeType());
    }

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