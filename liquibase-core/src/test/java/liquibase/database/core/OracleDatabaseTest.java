package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.resource.ResourceAccessor;
import liquibase.test.JUnitResourceAccessor;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;
import static org.junit.Assert.*;

/**
 * Tests for {@link liquibase.database.core.OracleDatabase}.
 */
public class OracleDatabaseTest extends AbstractJdbcDatabaseTest {
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");


    public OracleDatabaseTest() throws Exception {
        super(new OracleDatabase());
    }

    @Override
    protected String getProductNameString() {
        return "Oracle";
    }

    @Override
    @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("table name without schema is correctly escaped as simply tableName",
                "tableName", database.escapeTableName(null, null, "tableName"));
    }

    @Test
    public void saveNlsEnvironment() throws Exception {
        Database database = getDatabase();
        ResourceAccessor junitResourceAccessor = new JUnitResourceAccessor();
        OfflineConnection offlineConnection = new OfflineConnection("offline:oracle", junitResourceAccessor);
        database.setConnection(offlineConnection);
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("table name without schema but with catalog is correctly escaped as catalogName.tableName",
                "catalogName.tableName", database.escapeTableName("catalogName", "schemaName", "tableName"));
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertTrue("Oracle Database is correctly reported as being able to do INITIALLY DEFERRED column constraints.",
                getDatabase().supportsInitiallyDeferrableColumns());
    }


    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        Assert.assertEquals("Oracle Database's 'give me the current timestamp' function is correctly reported.",
                "SYSTIMESTAMP", getDatabase().getCurrentDateTimeFunction());
    }

    public void testGetDefaultDriver() {
        Database database = new OracleDatabase();

        assertEquals("The correct JDBC driver class name is reported if the URL is a Oracle JDBC URL",
                "oracle.jdbc.OracleDriver", database.getDefaultDriver("jdbc:oracle:thin:@localhost/XE"));

        assertNull("No JDBC driver class is returned if the URL is NOT an Oracle Database JDBC URL.",
                database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

}

