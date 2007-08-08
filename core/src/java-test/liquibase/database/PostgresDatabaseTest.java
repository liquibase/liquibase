package liquibase.database;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link PostgresDatabase}
 */
public class PostgresDatabaseTest extends AbstractDatabaseTest {

    public PostgresDatabaseTest() {
        super(new PostgresDatabase());
    }

    protected String getProductNameString() {
        return "PostgreSQL";
    }

    @Test
    public void getBlobType() {
        assertEquals("BYTEA", getDatabase().getBlobType());
    }

    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertTrue(getDatabase().supportsInitiallyDeferrableColumns());
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
        assertNull(getDatabase().getUUIDType());
    }

    @Test
    public void getClobType() {
        assertEquals("TEXT", getDatabase().getClobType());
    }

    @Test
    public void getDateType() {
        assertEquals("DATE", getDatabase().getDateType());
    }

    @Test
    public void getDateTimeType() {
        assertEquals("TIMESTAMP WITH TIME ZONE", getDatabase().getDateTimeType());
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

}