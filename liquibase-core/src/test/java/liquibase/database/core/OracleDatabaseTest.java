package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Tests for {@link liquibase.database.core.OracleDatabase}.
 */
public class OracleDatabaseTest extends AbstractJdbcDatabaseTest {

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
        assertEquals("tableName", database.escapeTableName(null, null, "tableName"));
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("catalogName.tableName", database.escapeTableName("catalogName", "schemaName", "tableName"));
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertTrue(getDatabase().supportsInitiallyDeferrableColumns());
    }


    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        Assert.assertEquals("SYSTIMESTAMP", getDatabase().getCurrentDateTimeFunction());
    }

    @Test
    public void testGetDefaultDriver() {
        Database database = new OracleDatabase();

        assertEquals("oracle.jdbc.OracleDriver", database.getDefaultDriver("jdbc:oracle:thin:@localhost/XE"));

        assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

    @Test
    public void getDateLiteral_dateOnly() {
    	assertEquals("to_date('2017-08-16', 'YYYY-MM-DD')", database.getDateLiteral("2017-08-16"));
    }

    @Test
    public void getDateLiteral_timeOnly() {
    	assertEquals("to_date('16-32-55', 'HH24:MI:SS')", database.getDateLiteral("16-32-55"));
    }

    @Test
    public void getDateLiteral_timestamp() {
    	assertEquals("to_timestamp('2017-08-16 16:32:55.125', 'YYYY-MM-DD HH24:MI:SS.FF')", database.getDateLiteral("2017-08-16T16:32:55.125"));
    }

    @Test
    public void getDateLiteral_datetime() {
    	assertEquals("to_date('2017-08-16 16:32:55', 'YYYY-MM-DD HH24:MI:SS')", database.getDateLiteral("2017-08-16T16:32:55.3"));
    }

    @Test
    public void getDateLiteral_datetime_invalid() {
    	assertEquals("UNSUPPORTED:2017-08-16T16:32:55_3", database.getDateLiteral("2017-08-16T16:32:55_3"));
    }
    
    @Test
    public void getDateLiteral_unsupported() {
    	assertEquals("UNSUPPORTED:123", database.getDateLiteral("123"));
    }
    
    
}

