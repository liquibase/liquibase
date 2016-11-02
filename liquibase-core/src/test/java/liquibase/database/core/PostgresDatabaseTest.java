package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import org.junit.Assert;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests for {@link PostgresDatabase}
 */
public class PostgresDatabaseTest extends AbstractJdbcDatabaseTest {

    public PostgresDatabaseTest() throws Exception {
        super(new PostgresDatabase());
    }

    @Override
    protected String getProductNameString() {
        return "PostgreSQL";
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertTrue(getDatabase().supportsInitiallyDeferrableColumns());
    }


    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        Assert.assertEquals("NOW()", getDatabase().getCurrentDateTimeFunction());
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


    @Override
    @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("\"tableName\"", database.escapeTableName(null, null, "tableName"));
        assertEquals("tbl", database.escapeTableName(null, null, "tbl"));
    }

    @Test
    public void escapeTableName_reservedWord() {
        Database database = getDatabase();
        assertEquals("\"user\"", database.escapeTableName(null, null, "user"));
    }

    @Override
    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("\"schemaName\".\"tableName\"", database.escapeTableName("catalogName", "schemaName", "tableName"));
    }

    @Test
    public void escapeTableName_reservedWordOnly() {
        Database database = getDatabase();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ONLY_RESERVED_WORDS);
        assertEquals("\"user\"", database.escapeTableName(null, null, "user"));
        assertEquals("tableName", database.escapeTableName(null, null, "tableName"));
    }

    @Test
    public void escapeTableName_all() {
        Database database = getDatabase();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
        assertEquals("\"tbl\"", database.escapeTableName(null, null, "tbl"));
        assertEquals("\"user\"", database.escapeTableName(null, null, "user"));
    }
}
