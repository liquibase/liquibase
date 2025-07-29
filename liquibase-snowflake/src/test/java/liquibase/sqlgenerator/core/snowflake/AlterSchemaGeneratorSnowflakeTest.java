package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AlterSchemaStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AlterSchemaGeneratorSnowflake
 */
public class AlterSchemaGeneratorSnowflakeTest {

    private AlterSchemaGeneratorSnowflake generator;
    private SnowflakeDatabase database;

    @BeforeEach
    public void setUp() {
        generator = new AlterSchemaGeneratorSnowflake();
        database = new SnowflakeDatabase();
    }

    @Test
    public void testSupportsSnowflake() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        assertTrue(generator.supports(statement, database), 
                   "Generator should support Snowflake database");
    }

    @Test
    public void testValidationSchemaNameRequired() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        ValidationErrors errors = generator.validate(statement, database, null);
        
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("schemaName is required")), 
                "Should require schemaName");
    }

    @Test
    public void testValidationAtLeastOneChangeRequired() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        
        ValidationErrors errors = generator.validate(statement, database, null);
        assertTrue(errors.hasErrors(), "Should have validation errors");
        assertTrue(errors.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("At least one schema property must be changed")), 
                "Should require at least one change");
    }

    @Test
    public void testGenerateRenameSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("old_schema");
        statement.setNewName("new_schema");

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        assertEquals(1, sqlArray.length, "Should generate one SQL statement");
        
        String sql = sqlArray[0].toSql();
        assertEquals("ALTER SCHEMA old_schema RENAME TO new_schema", sql);
    }

    @Test
    public void testGenerateRenameWithIfExistsSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("old_schema");
        statement.setNewName("new_schema");
        statement.setIfExists(true);

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        assertEquals("ALTER SCHEMA IF EXISTS old_schema RENAME TO new_schema", sql);
    }

    @Test
    public void testGenerateEnableManagedAccessSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setEnableManagedAccess(true);

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        assertEquals("ALTER SCHEMA test_schema ENABLE MANAGED ACCESS", sql);
    }

    @Test
    public void testGenerateDisableManagedAccessSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setDisableManagedAccess(true);

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        assertEquals("ALTER SCHEMA test_schema DISABLE MANAGED ACCESS", sql);
    }

    @Test
    public void testGenerateManagedAccessWithIfExistsSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setIfExists(true);
        statement.setEnableManagedAccess(true);

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        assertEquals("ALTER SCHEMA IF EXISTS test_schema ENABLE MANAGED ACCESS", sql);
    }

    @Test
    public void testGenerateSetSinglePropertySQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setNewDataRetentionTimeInDays("7");

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        assertEquals("ALTER SCHEMA test_schema SET DATA_RETENTION_TIME_IN_DAYS = 7", sql);
    }

    @Test
    public void testGenerateSetMultiplePropertiesSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setNewDataRetentionTimeInDays("7");
        statement.setNewMaxDataExtensionTimeInDays("14");
        statement.setNewComment("Test comment");

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        
        // Should contain all SET options separated by spaces
        assertTrue(sql.contains("ALTER SCHEMA test_schema SET"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS = 7"));
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS = 14"));
        assertTrue(sql.contains("COMMENT = 'Test comment'"));
    }

    @Test
    public void testGenerateSetWithIfExistsSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setIfExists(true);
        statement.setNewDataRetentionTimeInDays("7");

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        assertEquals("ALTER SCHEMA IF EXISTS test_schema SET DATA_RETENTION_TIME_IN_DAYS = 7", sql);
    }

    @Test
    public void testGenerateSetCommentWithQuotesSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setNewComment("Comment with 'quotes'");

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("COMMENT = 'Comment with ''quotes'''"));
    }

    @Test
    public void testGenerateDropCommentSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setDropComment(true);

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        assertEquals("ALTER SCHEMA test_schema SET COMMENT = ''", sql);
    }

    @Test
    public void testGenerateSetPipeExecutionPausedSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setNewPipeExecutionPaused("TRUE");

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        assertEquals("ALTER SCHEMA test_schema SET PIPE_EXECUTION_PAUSED = TRUE", sql);
    }

    @Test
    public void testGenerateSetDefaultDdlCollationSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setNewDefaultDdlCollation("utf8");

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        assertEquals("ALTER SCHEMA test_schema SET DEFAULT_DDL_COLLATION = 'utf8'", sql);
    }

    @Test
    public void testGenerateUnsetSinglePropertySQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setUnsetDataRetentionTimeInDays(true);

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        assertEquals("ALTER SCHEMA test_schema UNSET DATA_RETENTION_TIME_IN_DAYS", sql);
    }

    @Test
    public void testGenerateUnsetMultiplePropertiesSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setUnsetDataRetentionTimeInDays(true);
        statement.setUnsetMaxDataExtensionTimeInDays(true);
        statement.setUnsetComment(true);

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        
        assertTrue(sql.contains("ALTER SCHEMA test_schema UNSET"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS"));
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS"));
        assertTrue(sql.contains("COMMENT"));
    }

    @Test
    public void testGenerateUnsetWithIfExistsSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setIfExists(true);
        statement.setUnsetDataRetentionTimeInDays(true);

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        String sql = sqlArray[0].toSql();
        assertEquals("ALTER SCHEMA IF EXISTS test_schema UNSET DATA_RETENTION_TIME_IN_DAYS", sql);
    }

    @Test
    public void testGenerateMultipleOperationsSQL() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setNewDataRetentionTimeInDays("7");
        statement.setUnsetComment(true);
        statement.setEnableManagedAccess(true);

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Should generate multiple SQL statements for different operation types
        assertTrue(sqlArray.length >= 2, "Should generate multiple statements");
        
        // Check that each operation type is present
        String allSql = String.join(" ", java.util.Arrays.stream(sqlArray)
                .map(sql -> sql.toSql()).toArray(String[]::new));
        
        assertTrue(allSql.contains("ENABLE MANAGED ACCESS"));
        assertTrue(allSql.contains("SET DATA_RETENTION_TIME_IN_DAYS = 7"));
        assertTrue(allSql.contains("UNSET COMMENT"));
    }

    @Test
    public void testRenameWithManagedAccessUsesNewName() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("old_schema");
        statement.setNewName("new_schema");
        statement.setEnableManagedAccess(true);

        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Should generate two statements: rename and managed access
        assertEquals(2, sqlArray.length);
        
        String renameSql = sqlArray[0].toSql();
        String managedSql = sqlArray[1].toSql();
        
        assertEquals("ALTER SCHEMA old_schema RENAME TO new_schema", renameSql);
        assertEquals("ALTER SCHEMA new_schema ENABLE MANAGED ACCESS", managedSql);
    }

    @Test
    public void testValidRenameOperation() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setNewName("renamed_schema");

        ValidationErrors errors = generator.validate(statement, database, null);
        assertFalse(errors.hasErrors(), "Rename operation should be valid");
    }

    @Test
    public void testValidSetOperation() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setNewDataRetentionTimeInDays("7");

        ValidationErrors errors = generator.validate(statement, database, null);
        assertFalse(errors.hasErrors(), "Set operation should be valid");
    }

    @Test
    public void testValidUnsetOperation() {
        AlterSchemaStatement statement = new AlterSchemaStatement();
        statement.setSchemaName("test_schema");
        statement.setUnsetDataRetentionTimeInDays(true);

        ValidationErrors errors = generator.validate(statement, database, null);
        assertFalse(errors.hasErrors(), "Unset operation should be valid");
    }
}