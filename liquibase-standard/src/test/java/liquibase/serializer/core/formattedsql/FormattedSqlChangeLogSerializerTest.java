package liquibase.serializer.core.formattedsql;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.change.core.AddAutoIncrementChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.AddColumnConfig;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FormattedSqlChangeLogSerializerTest {

    private FormattedSqlChangeLogSerializer serializer;
    private ChangeSet changeSet;
    private Database database;

    @Before
    public void setUp() {
        serializer = new FormattedSqlChangeLogSerializer();
        changeSet = new ChangeSet("1", "testAuthor", false, false, "path/to/changeLogFile.h2.sql", null, null, null);
        database = new H2Database();
    }

    @Test
    public void serialize_changeSetWithNoChanges() {
        String serialized = serializer.serialize(changeSet, true);
        assertEquals(serialized, ("-- changeset testAuthor:1 splitStatements:false\n"));
    }

    @Test
    public void serialize_changeSetWithContextAndLabels() {
        ChangeSet changeSetWithContextAndLabels = new ChangeSet("1", "testAuthor", false, false, "path/to/changeLogFile.h2.sql", null, null, null);
        changeSetWithContextAndLabels.setLabels(new Labels("label1"));
        changeSetWithContextAndLabels.setContextFilter(new ContextExpression("context1"));
        String serialized = serializer.serialize(changeSetWithContextAndLabels, true);
        assertEquals(serialized, ("-- changeset testAuthor:1 labels:\"label1\" contextFilter:\"context1\" splitStatements:false\n"));
    }

    @Test
    public void getValidFileExtensions_returnsSqlExtension() {
        String[] expectedExtensions = new String[] {"sql"};
        assertArrayEquals(expectedExtensions, serializer.getValidFileExtensions());
    }

    @Test
    public void serialize_changeSetWithChanges() {
        AddAutoIncrementChange statement = new AddAutoIncrementChange();
        statement.setTableName("table_name");
        statement.setColumnName("column_name");
        statement.setColumnDataType("int");
        changeSet.addChange(statement);
        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        String expectedSql = "-- changeset testAuthor:1 splitStatements:false\n" + sqls[0].toSql() + ";\n";

        String serialized = serializer.serialize(changeSet, true);
        assertEquals(expectedSql, serialized);
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void serialize_changeSetWithNoFilePath() {
        changeSet.setFilePath(null);
        serializer.serialize(changeSet, true);
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void serialize_changeSetWithInvalidFilePath() {
        changeSet.setFilePath("invalidFilePath");
        serializer.serialize(changeSet, true);
    }

    @Test(expected = UnexpectedLiquibaseException.class)
    public void serialize_changeSetWithNoDatabase() {
        ChangeSet changeSetWithInvalidDb = new ChangeSet("1", "testAuthor",
                false, false, "path/to/changeLogFile.LALALA.sql", null, null, null);
        serializer.serialize(changeSetWithInvalidDb, true);
    }

    @Test
    public void serialize_changeSetWithLogicalFilePath() {
        changeSet.setLogicalFilePath("foo/bar/baz.sql");

        AddAutoIncrementChange statement = new AddAutoIncrementChange();
        statement.setTableName("table_name");
        statement.setColumnName("column_name");
        statement.setColumnDataType("int");
        changeSet.addChange(statement);
        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        String expectedSql = "-- changeset testAuthor:1 logicalFilePath:\"foo/bar/baz.sql\" splitStatements:false\n" + sqls[0].toSql() + ";\n";

        String serialized = serializer.serialize(changeSet, true);
        assertEquals(expectedSql, serialized);
    }

    @Test
    public void cleanSqlServerQuoting_removesBracketsFromQuotedIdentifiers() {
        // Test the private method via reflection to verify regex works correctly
        String inputSql = "CREATE TABLE \"[MyTable]\" (\"[Column1]\" INT, \"[Column2]\" VARCHAR(50));";
        String expectedSql = "CREATE TABLE [MyTable] ([Column1] INT, [Column2] VARCHAR(50));";
        
        // Use a SQL Server database and changeset to trigger the cleaning logic
        ChangeSet sqlServerChangeSet = new ChangeSet("1", "testAuthor", false, false, "path/to/changeLogFile.mssql.sql", null, null, null);
        MSSQLDatabase sqlServerDatabase = new MSSQLDatabase();
        
        CreateTableChange change = new CreateTableChange();
        change.setTableName("[TestTable]");
        AddColumnConfig column = new AddColumnConfig();
        column.setName("[TestColumn]");
        column.setType("int");
        change.addColumn(column);
        sqlServerChangeSet.addChange(change);
        
        String serialized = serializer.serialize(sqlServerChangeSet, true);
        
        // Verify that the output doesn't contain quoted brackets
        assertFalse("SQL should not contain quoted brackets", serialized.contains("\"["));
        assertFalse("SQL should not contain quoted brackets", serialized.contains("]\""));
    }

    @Test
    public void serialize_sqlServerChangeSetWithBracketedNames() {
        ChangeSet sqlServerChangeSet = new ChangeSet("1", "testAuthor", false, false, "path/to/changeLogFile.mssql.sql", null, null, null);
        
        CreateTableChange change = new CreateTableChange();
        change.setTableName("[BracketedTable]");
        AddColumnConfig column = new AddColumnConfig();
        column.setName("[BracketedColumn]");
        column.setType("int");
        change.addColumn(column);
        sqlServerChangeSet.addChange(change);
        
        String serialized = serializer.serialize(sqlServerChangeSet, true);
        
        // Verify the changeset header is correct
        assertTrue("Should contain changeset header", serialized.startsWith("-- changeset testAuthor:1"));
        
        // Verify that brackets are preserved without additional quotes
        assertTrue("Should contain bracketed table name", serialized.contains("[BracketedTable]"));
        assertTrue("Should contain bracketed column name", serialized.contains("[BracketedColumn]"));
        
        // Verify no double quoting
        assertFalse("Should not contain quoted brackets", serialized.contains("\"[BracketedTable]\""));
        assertFalse("Should not contain quoted brackets", serialized.contains("\"[BracketedColumn]\""));
    }

    @Test
    public void serialize_nonSqlServerDatabaseUnaffected() {
        // Test that non-SQL Server databases are not affected by the bracket cleaning
        ChangeSet h2ChangeSet = new ChangeSet("1", "testAuthor", false, false, "path/to/changeLogFile.h2.sql", null, null, null);
        
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        change.setTableName("RegularTable");
        change.setColumnName("RegularColumn");
        change.setColumnDataType("int");
        h2ChangeSet.addChange(change);
        
        String serialized = serializer.serialize(h2ChangeSet, true);
        
        // Verify normal behavior for non-SQL Server databases
        assertTrue("Should contain changeset header", serialized.startsWith("-- changeset testAuthor:1"));
        assertTrue("Should contain table name", serialized.contains("RegularTable"));
        assertTrue("Should contain column name", serialized.contains("RegularColumn"));
    }

}
