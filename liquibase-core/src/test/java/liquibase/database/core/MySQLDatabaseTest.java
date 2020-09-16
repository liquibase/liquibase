package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.statement.DatabaseFunction;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link MySQLDatabase}
 */
public class MySQLDatabaseTest extends AbstractJdbcDatabaseTest {

    public MySQLDatabaseTest() throws Exception {
        super(new MySQLDatabase());
    }

    @Override
    protected String getProductNameString() {
      return "MySQL";
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
    public void getCurrentDateTimeFunctionWithPrecision() {
        MySQLDatabase mySQLDatabase = (MySQLDatabase) getDatabase();
        Assert.assertEquals("NOW(1)", mySQLDatabase.getCurrentDateTimeFunction(1));
        Assert.assertEquals("NOW(2)", mySQLDatabase.getCurrentDateTimeFunction(2));
        Assert.assertEquals("NOW(5)", mySQLDatabase.getCurrentDateTimeFunction(5));
    }

    @Test
    public void generateDatabaseFunctionValue() {
        MySQLDatabase mySQLDatabase = (MySQLDatabase) getDatabase();
        assertEquals("NOW()", mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction("CURRENT_TIMESTAMP()")));
        assertNull(mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction(null)));
    }

    @Test
    public void generateDatabaseFunctionValueWithPrecision() {
        MySQLDatabase mySQLDatabase = (MySQLDatabase) getDatabase();
        assertEquals("NOW(2)", mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction("CURRENT_TIMESTAMP(2)")));
        assertEquals("NOW(3)", mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction("CURRENT_TIMESTAMP(3)")));
    }

    @Test
    public void generateDatabaseFunctionValueWithIncorrectPrecision() {
        MySQLDatabase mySQLDatabase = (MySQLDatabase) getDatabase();
        assertEquals("NOW()", mySQLDatabase.generateDatabaseFunctionValue(new DatabaseFunction("CURRENT_TIMESTAMP(string)")));
    }

    public void testGetDefaultDriver() {
        Database database = new MySQLDatabase();

        assertEquals("com.mysql.cj.jdbc.Driver", database.getDefaultDriver("jdbc:mysql://localhost/liquibase"));

        assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
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
