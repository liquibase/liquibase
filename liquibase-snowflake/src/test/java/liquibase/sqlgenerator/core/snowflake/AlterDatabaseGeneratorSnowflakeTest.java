package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AlterDatabaseStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AlterDatabaseGeneratorSnowflake
 */
@DisplayName("AlterDatabaseGeneratorSnowflake")
public class AlterDatabaseGeneratorSnowflakeTest {
    
    private AlterDatabaseGeneratorSnowflake generator;
    private AlterDatabaseStatement statement;
    
    @Mock
    private SnowflakeDatabase database;
    
    @Mock
    private SqlGeneratorChain sqlGeneratorChain;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new AlterDatabaseGeneratorSnowflake();
        statement = new AlterDatabaseStatement();
        
        // Setup database mock
        when(database.escapeObjectName("TEST_DB", liquibase.structure.core.Table.class)).thenReturn("TEST_DB");
        when(database.escapeObjectName("NEW_DB", liquibase.structure.core.Table.class)).thenReturn("NEW_DB");
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(generator.supports(statement, database));
    }
    
    @Test
    @DisplayName("Should validate database name is required")
    void shouldValidateDatabaseNameRequired() {
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("databaseName is required"));
    }
    
    @Test
    @DisplayName("Should validate at least one change is required")
    void shouldValidateAtLeastOneChangeRequired() {
        statement.setDatabaseName("TEST_DB");
        
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().get(0).contains("At least one database property must be changed"));
    }
    
    @Test
    @DisplayName("Should generate rename SQL")
    void shouldGenerateRenameSql() {
        statement.setDatabaseName("TEST_DB");
        statement.setNewName("NEW_DB");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER DATABASE TEST_DB RENAME TO NEW_DB", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate rename SQL with IF EXISTS")
    void shouldGenerateRenameSqlWithIfExists() {
        statement.setDatabaseName("TEST_DB");
        statement.setNewName("NEW_DB");
        statement.setIfExists(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER DATABASE IF EXISTS TEST_DB RENAME TO NEW_DB", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SET SQL for single property")
    void shouldGenerateSetSqlForSingleProperty() {
        statement.setDatabaseName("TEST_DB");
        statement.setNewDataRetentionTimeInDays("7");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER DATABASE TEST_DB SET DATA_RETENTION_TIME_IN_DAYS = 7", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate SET SQL for multiple properties")
    void shouldGenerateSetSqlForMultipleProperties() {
        statement.setDatabaseName("TEST_DB");
        statement.setNewDataRetentionTimeInDays("7");
        statement.setNewMaxDataExtensionTimeInDays("30");
        statement.setNewDefaultDdlCollation("en-ci");
        statement.setNewComment("Test database");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("ALTER DATABASE TEST_DB SET"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS = 7"));
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS = 30"));
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION = 'en-ci'"));
        assertTrue(sql.contains("COMMENT = 'Test database'"));
    }
    
    @Test
    @DisplayName("Should handle comment with quotes")
    void shouldHandleCommentWithQuotes() {
        statement.setDatabaseName("TEST_DB");
        statement.setNewComment("Test's database");
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER DATABASE TEST_DB SET COMMENT = 'Test''s database'", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate UNSET SQL for single property")
    void shouldGenerateUnsetSqlForSingleProperty() {
        statement.setDatabaseName("TEST_DB");
        statement.setUnsetComment(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER DATABASE TEST_DB UNSET COMMENT", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate UNSET SQL for multiple properties")
    void shouldGenerateUnsetSqlForMultipleProperties() {
        statement.setDatabaseName("TEST_DB");
        statement.setUnsetDataRetentionTimeInDays(true);
        statement.setUnsetMaxDataExtensionTimeInDays(true);
        statement.setUnsetDefaultDdlCollation(true);
        statement.setUnsetComment(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.startsWith("ALTER DATABASE TEST_DB UNSET"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS"));
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS"));
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION"));
        assertTrue(sql.contains("COMMENT"));
    }
    
    @Test
    @DisplayName("Should handle dropComment as UNSET")
    void shouldHandleDropCommentAsUnset() {
        statement.setDatabaseName("TEST_DB");
        statement.setDropComment(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(1, sqls.length);
        assertEquals("ALTER DATABASE TEST_DB SET COMMENT = ''", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate multiple SQL statements for complex operations")
    void shouldGenerateMultipleSqlForComplexOperations() {
        statement.setDatabaseName("TEST_DB");
        statement.setNewName("NEW_DB");
        statement.setNewDataRetentionTimeInDays("7");
        statement.setUnsetComment(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(3, sqls.length);
        // First: rename
        assertEquals("ALTER DATABASE TEST_DB RENAME TO NEW_DB", sqls[0].toSql());
        // Second: SET operations (using new name)
        assertEquals("ALTER DATABASE NEW_DB SET DATA_RETENTION_TIME_IN_DAYS = 7", sqls[1].toSql());
        // Third: UNSET operations (using new name)
        assertEquals("ALTER DATABASE NEW_DB UNSET COMMENT", sqls[2].toSql());
    }
    
    @Test
    @DisplayName("Should use IF EXISTS for all statements")
    void shouldUseIfExistsForAllStatements() {
        statement.setDatabaseName("TEST_DB");
        statement.setIfExists(true);
        statement.setNewName("NEW_DB");
        statement.setNewDataRetentionTimeInDays("7");
        statement.setUnsetComment(true);
        
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        assertEquals(3, sqls.length);
        assertTrue(sqls[0].toSql().contains("IF EXISTS"));
        assertTrue(sqls[1].toSql().contains("IF EXISTS"));
        assertTrue(sqls[2].toSql().contains("IF EXISTS"));
    }
}