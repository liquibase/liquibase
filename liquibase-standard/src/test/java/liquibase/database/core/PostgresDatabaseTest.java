package liquibase.database.core;

import liquibase.GlobalConfiguration;
import liquibase.changelog.column.LiquibaseColumn;
import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.DatabaseException;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    public void testGetDefaultDriver() throws DatabaseException {
        try (Database database = new PostgresDatabase()) {
            assertEquals("org.postgresql.Driver", database.getDefaultDriver("jdbc:postgresql://localhost/liquibase"));

            assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
        } catch (final DatabaseException e) {
            throw e;
        }
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

    @Test
    public void testIfEscapeLogicNotImpactOnChangeLog() {
        PostgresDatabase database = (PostgresDatabase) getDatabase();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);

        final String COLUMN_AUTHOR = "AUTHOR"; //one column from changeLog table should be enough for test

        String result = database.escapeObjectName(COLUMN_AUTHOR, LiquibaseColumn.class);
        assertEquals(COLUMN_AUTHOR, result);
    }

    @Override
    @Test
    public void test_escapeObjectName() {
        String tableName = database.escapeObjectName("My Table  ", Table.class);
        assertTrue(tableName.matches("[\\[\\\"`]?My Table  [\\]\\\"`]?"));

        tableName = database.escapeObjectName("MyTable", Table.class);
        assertEquals("\"MyTable\"", tableName);

        tableName = database.escapeObjectName("My Table", Table.class);
        assertTrue(tableName.matches("[\\[\\\"`]?My Table[\\]\\\"`]?"));
    }

    @Test
    public void test_getConcatSql() {
        assertEquals("", database.getConcatSql());
        assertEquals("foo", database.getConcatSql("foo"));
        assertEquals("foo || bar", database.getConcatSql("foo", "bar"));
        assertEquals("one || two || | three", database.getConcatSql("one", "two", "| three"));
    }

    @Test
    public void generatePrimaryKeyName_tableSizeNameLessThan63Bytes_nameIsBuiltCorrectly() {
        final String tableName = "name";
        final String expectedPrimaryKeyName = "name_pkey";

        assertPrimaryKeyName(expectedPrimaryKeyName, this.database.generatePrimaryKeyName(tableName));
    }

    @Test
    public void generatePrimaryKeyName_tableSizeNameMoreThan63Bytes_nameIsBuiltCorrectly() {
        final String tableName = "name_" + StringUtil.repeat("_", 100);
        final String expectedPrimaryKeyName = "name______________________________________________________pkey";

        assertPrimaryKeyName(expectedPrimaryKeyName, this.database.generatePrimaryKeyName(tableName));
    }

    @Test
    public void generatePrimaryKeyName_tableSizeNameLessThan63BytesAndNonASCIISymbols_nameIsBuiltCorrectly() {
        final String nameWith15NonAsciiSymbols = "name_" + StringUtil.repeat("\u03A9", 15);
        final String expectedPrimaryKeyName = "name_" + StringUtil.repeat("\u03A9", 15) + "_pkey";

        assertPrimaryKeyName(expectedPrimaryKeyName, this.database.generatePrimaryKeyName(nameWith15NonAsciiSymbols));
    }

//    @Test
//    public void generatePrimaryKeyName_tableSizeNameMoreThan63BytesAndNonASCIISymbols_nameIsBuiltCorrectly() {
//        final String nameWith100NonAsciiSymbols = "name_" + StringUtil.repeat("\u03A9", 100);
//        final String expectedPrimaryKeyName = "name_" + StringUtil.repeat("\u03A9", 26) + "_pkey";
//
//        assertPrimaryKeyName(expectedPrimaryKeyName, this.database.generatePrimaryKeyName(nameWith100NonAsciiSymbols));
//    }

    private void assertPrimaryKeyName(String expected, String actual) {
        assertTrue(expected.getBytes(GlobalConfiguration.FILE_ENCODING.getCurrentValue()).length <= PostgresDatabase.PGSQL_PK_BYTES_LIMIT);
        assert expected.equals(actual) : "Invalid " + actual + " vs expected " + expected;
    }
}
