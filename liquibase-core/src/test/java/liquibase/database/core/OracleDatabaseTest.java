package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.core.TimestampType;

import org.junit.Assert;
import static org.junit.Assert.*;

import org.hamcrest.CoreMatchers;
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
    public void verifyTimestampDataTypeWhenWithoutClauseIsPresent() {
        TimestampType ts = new TimestampType();
        ts.setAdditionalInformation("WITHOUT TIME ZONE");
        DatabaseDataType oracleDataType = ts.toDatabaseDataType(getDatabase());
        assertThat(oracleDataType.getType(), CoreMatchers.is("TIMESTAMP"));
    }

    public void testGetDefaultDriver() {
        Database database = new OracleDatabase();

        assertEquals("oracle.jdbc.OracleDriver", database.getDefaultDriver("jdbc:oracle:thin:@localhost/XE"));

        assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

}

