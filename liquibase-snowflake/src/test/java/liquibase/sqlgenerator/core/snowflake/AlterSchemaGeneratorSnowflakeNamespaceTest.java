package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.AlterSchemaStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Comprehensive unit tests for AlterSchemaGeneratorSnowflake namespace attribute support
 */
@DisplayName("AlterSchemaGeneratorSnowflake Namespace Attributes")
public class AlterSchemaGeneratorSnowflakeNamespaceTest {
    
    private AlterSchemaGeneratorSnowflake generator;
    private AlterSchemaStatement statement;
    
    @Mock
    private SnowflakeDatabase database;
    
    @Mock
    private SqlGeneratorChain sqlGeneratorChain;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new AlterSchemaGeneratorSnowflake();
        statement = new AlterSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        
        // Setup database mock for schema name escaping
        when(database.escapeObjectName("TEST_SCHEMA", liquibase.structure.core.Table.class))
            .thenReturn("TEST_SCHEMA");
        when(database.escapeObjectName("NEW_SCHEMA", liquibase.structure.core.Table.class))
            .thenReturn("NEW_SCHEMA");
        
        // Clear storage
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    @AfterEach
    void tearDown() {
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    // RENAME Operation Tests
    
    @Test
    @DisplayName("Should generate RENAME TO operation from namespace attribute")
    void shouldGenerateRenameToFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newName", "NEW_SCHEMA");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA RENAME TO NEW_SCHEMA"));
        
        // Verify attributes were cleaned up
        assertNull(SnowflakeNamespaceAttributeStorage.getAttributes("TEST_SCHEMA"));
    }
    
    @Test
    @DisplayName("Should generate RENAME TO with IF EXISTS from namespace attribute")
    void shouldGenerateRenameToWithIfExistsFromNamespaceAttribute() {
        // Given
        statement.setIfExists(true);
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newName", "NEW_SCHEMA");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA IF EXISTS TEST_SCHEMA RENAME TO NEW_SCHEMA"));
    }
    
    // SET Operations Tests
    
    @Test
    @DisplayName("Should generate SET DATA_RETENTION_TIME_IN_DAYS from namespace attribute")
    void shouldGenerateSetDataRetentionFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newDataRetentionTimeInDays", "30");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA SET DATA_RETENTION_TIME_IN_DAYS = 30"));
    }
    
    @Test
    @DisplayName("Should generate SET MAX_DATA_EXTENSION_TIME_IN_DAYS from namespace attribute")
    void shouldGenerateSetMaxDataExtensionFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newMaxDataExtensionTimeInDays", "60");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA SET MAX_DATA_EXTENSION_TIME_IN_DAYS = 60"));
    }
    
    @Test
    @DisplayName("Should generate SET DEFAULT_DDL_COLLATION from namespace attribute")
    void shouldGenerateSetDefaultDdlCollationFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newDefaultDdlCollation", "en-ci");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA SET DEFAULT_DDL_COLLATION = 'en-ci'"));
    }
    
    @Test
    @DisplayName("Should generate SET PIPE_EXECUTION_PAUSED from namespace attribute")
    void shouldGenerateSetPipeExecutionPausedFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newPipeExecutionPaused", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA SET PIPE_EXECUTION_PAUSED = TRUE"));
    }
    
    @Test
    @DisplayName("Should generate SET COMMENT from namespace attribute")
    void shouldGenerateSetCommentFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newComment", "Updated schema comment");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA SET COMMENT = 'Updated schema comment'"));
    }
    
    @Test
    @DisplayName("Should generate SET with multiple properties from namespace attributes")  
    void shouldGenerateSetWithMultiplePropertiesFromNamespaceAttributes() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newDataRetentionTimeInDays", "15");
        attrs.put("newMaxDataExtensionTimeInDays", "45");
        attrs.put("newComment", "Multi-property update");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA SET"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS = 15"));
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS = 45"));
        assertTrue(sql.contains("COMMENT = 'Multi-property update'"));
    }
    
    @Test
    @DisplayName("Should generate DROP COMMENT from namespace attribute")
    void shouldGenerateDropCommentFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("dropComment", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA SET COMMENT = ''"));
    }
    
    // UNSET Operations Tests
    
    @Test
    @DisplayName("Should generate UNSET DATA_RETENTION_TIME_IN_DAYS from namespace attribute")
    void shouldGenerateUnsetDataRetentionFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("unsetDataRetentionTimeInDays", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA UNSET DATA_RETENTION_TIME_IN_DAYS"));
    }
    
    @Test
    @DisplayName("Should generate UNSET MAX_DATA_EXTENSION_TIME_IN_DAYS from namespace attribute")
    void shouldGenerateUnsetMaxDataExtensionFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("unsetMaxDataExtensionTimeInDays", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA UNSET MAX_DATA_EXTENSION_TIME_IN_DAYS"));
    }
    
    @Test
    @DisplayName("Should generate UNSET DEFAULT_DDL_COLLATION from namespace attribute")
    void shouldGenerateUnsetDefaultDdlCollationFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("unsetDefaultDdlCollation", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA UNSET DEFAULT_DDL_COLLATION"));
    }
    
    @Test
    @DisplayName("Should generate UNSET PIPE_EXECUTION_PAUSED from namespace attribute")
    void shouldGenerateUnsetPipeExecutionPausedFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("unsetPipeExecutionPaused", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA UNSET PIPE_EXECUTION_PAUSED"));
    }
    
    @Test
    @DisplayName("Should generate UNSET COMMENT from namespace attribute")
    void shouldGenerateUnsetCommentFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("unsetComment", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA UNSET COMMENT"));
    }
    
    @Test
    @DisplayName("Should generate UNSET with multiple properties from namespace attributes")
    void shouldGenerateUnsetWithMultiplePropertiesFromNamespaceAttributes() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("unsetDataRetentionTimeInDays", "true");
        attrs.put("unsetMaxDataExtensionTimeInDays", "true");
        attrs.put("unsetComment", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA UNSET"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS"));
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS"));
        assertTrue(sql.contains("COMMENT"));
    }
    
    // MANAGED ACCESS Operations Tests
    
    @Test
    @DisplayName("Should generate ENABLE MANAGED ACCESS from namespace attribute")
    void shouldGenerateEnableManagedAccessFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("enableManagedAccess", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA ENABLE MANAGED ACCESS"));
    }
    
    @Test
    @DisplayName("Should generate DISABLE MANAGED ACCESS from namespace attribute")
    void shouldGenerateDisableManagedAccessFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("disableManagedAccess", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("ALTER SCHEMA TEST_SCHEMA DISABLE MANAGED ACCESS"));
    }
    
    // Complex Operations Tests
    
    @Test
    @DisplayName("Should generate multiple SQL statements for complex operations")
    void shouldGenerateMultipleSqlStatementsForComplexOperations() {
        // Given - RENAME + ENABLE MANAGED ACCESS should generate 2 statements
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newName", "RENAMED_SCHEMA");
        attrs.put("enableManagedAccess", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        when(database.escapeObjectName("RENAMED_SCHEMA", liquibase.structure.core.Table.class))
            .thenReturn("RENAMED_SCHEMA");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(2, sqls.length);
        
        String sql1 = sqls[0].toSql();
        assertTrue(sql1.contains("ALTER SCHEMA TEST_SCHEMA RENAME TO RENAMED_SCHEMA"));
        
        String sql2 = sqls[1].toSql();
        assertTrue(sql2.contains("ALTER SCHEMA RENAMED_SCHEMA ENABLE MANAGED ACCESS"));
    }
    
    @Test
    @DisplayName("Should use renamed schema name in subsequent operations")
    void shouldUseRenamedSchemaNameInSubsequentOperations() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newName", "NEW_SCHEMA");
        attrs.put("newComment", "Updated after rename");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        when(database.escapeObjectName("NEW_SCHEMA", liquibase.structure.core.Table.class))
            .thenReturn("NEW_SCHEMA");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(2, sqls.length);
        
        String sql1 = sqls[0].toSql();
        assertTrue(sql1.contains("ALTER SCHEMA TEST_SCHEMA RENAME TO NEW_SCHEMA"));
        
        String sql2 = sqls[1].toSql();
        assertTrue(sql2.contains("ALTER SCHEMA NEW_SCHEMA SET COMMENT = 'Updated after rename'"));
    }
    
    // Validation Tests
    
    @Test
    @DisplayName("Should validate data retention constraints")
    void shouldValidateDataRetentionConstraints() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newDataRetentionTimeInDays", "60");
        attrs.put("newMaxDataExtensionTimeInDays", "30"); // Invalid: max < retention
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS must be >= DATA_RETENTION_TIME_IN_DAYS")));
    }
    
    @Test
    @DisplayName("Should validate comment length constraint")
    void shouldValidateCommentLengthConstraint() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) sb.append("A"); // > 256 chars
        String longComment = sb.toString();
        attrs.put("newComment", longComment);
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Schema comment cannot exceed 256 characters")));
    }
    
    @Test
    @DisplayName("Should validate comment mutual exclusivity")
    void shouldValidateCommentMutualExclusivity() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newComment", "New comment");
        attrs.put("unsetComment", "true"); // Conflicting operations
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot SET and UNSET comment operations simultaneously")));
    }
    
    @Test
    @DisplayName("Should validate managed access mutual exclusivity")
    void shouldValidateManagedAccessMutualExclusivity() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("enableManagedAccess", "true");
        attrs.put("disableManagedAccess", "true"); // Conflicting operations
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot ENABLE and DISABLE managed access simultaneously")));
    }
    
    @Test
    @DisplayName("Should validate data retention value ranges")
    void shouldValidateDataRetentionValueRanges() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newDataRetentionTimeInDays", "100"); // > 90, invalid
        attrs.put("newMaxDataExtensionTimeInDays", "110"); // > 90, invalid
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("DATA_RETENTION_TIME_IN_DAYS must be between 0 and 90")));
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS must be between 0 and 90")));
    }
    
    @Test
    @DisplayName("Should require at least one change operation")
    void shouldRequireAtLeastOneChangeOperation() {
        // Given - no changes specified
        Map<String, String> attrs = new HashMap<>();
        // Empty attributes map
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("At least one schema property must be changed")));
    }
    
    @Test
    @DisplayName("Should prefer namespace attributes over statement attributes")
    void shouldPreferNamespaceAttributesOverStatementAttributes() {
        // Given - both namespace and statement attributes
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newComment", "Namespace comment");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        statement.setNewComment("Statement comment");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then - namespace attribute should win
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("COMMENT = 'Namespace comment'"));
        assertFalse(sql.contains("Statement comment"));
    }
    
    @Test
    @DisplayName("Should handle special characters in comment")
    void shouldHandleSpecialCharactersInComment() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("newComment", "Comment with 'single quotes' and \"double quotes\"");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("COMMENT = 'Comment with ''single quotes'' and \"double quotes\"'"));
    }
}