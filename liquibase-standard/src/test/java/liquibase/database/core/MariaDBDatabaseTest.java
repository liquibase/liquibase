package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link MariaDBDatabase}
 */
public class MariaDBDatabaseTest extends AbstractJdbcDatabaseTest {

    public MariaDBDatabaseTest() throws Exception {
        super(new MariaDBDatabase());
    }

    @Override
    protected String getProductNameString() {
      return "MariaDB";
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        Assert.assertEquals("NOW()", getDatabase().getCurrentDateTimeFunction());
    }

    @Test
    public void testGetDefaultDriver() {
        assertEquals("org.mariadb.jdbc.Driver", this.database.getDefaultDriver("jdbc:mariadb://localhost/liquibase"));

        assertNull(this.database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

    @Override
    @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("tableName", database.escapeTableName(null, null, "tableName"));
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("catalogName.tableName", database.escapeTableName("catalogName", "schemaName", "tableName"));
    }

    @Test
    public void escapeStringForDatabase_withBackslashes() {
        Assert.assertEquals("\\\\0", database.escapeStringForDatabase("\\0"));
    }

}
