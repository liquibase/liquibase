package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateSchemaStatement;
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
 * Comprehensive unit tests for CreateSchemaGeneratorSnowflake namespace attribute support
 */
@DisplayName("CreateSchemaGeneratorSnowflake Namespace Attributes")
public class CreateSchemaGeneratorSnowflakeNamespaceTest {
    
    private CreateSchemaGeneratorSnowflake generator;
    private CreateSchemaStatement statement;
    
    @Mock
    private SnowflakeDatabase database;
    
    @Mock
    private SqlGeneratorChain sqlGeneratorChain;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        generator = new CreateSchemaGeneratorSnowflake();
        statement = new CreateSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        
        // Setup database mock for schema name escaping
        when(database.escapeObjectName("TEST_SCHEMA", liquibase.structure.core.Schema.class))
            .thenReturn("TEST_SCHEMA");
        
        // Clear storage
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    @AfterEach
    void tearDown() {
        SnowflakeNamespaceAttributeStorage.clear();
    }
    
    // Core Namespace Attributes Tests
    
    @Test
    @DisplayName("Should create OR REPLACE schema from namespace attribute")
    void shouldCreateOrReplaceSchemaFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("orReplace", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE OR REPLACE SCHEMA"));
        
        // Verify attributes were cleaned up
        assertNull(SnowflakeNamespaceAttributeStorage.getAttributes("TEST_SCHEMA"));
    }
    
    @Test
    @DisplayName("Should create IF NOT EXISTS schema from namespace attribute")
    void shouldCreateIfNotExistsSchemaFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("ifNotExists", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE SCHEMA IF NOT EXISTS"));
    }
    
    @Test
    @DisplayName("Should create TRANSIENT schema from namespace attribute")
    void shouldCreateTransientSchemaFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("transient", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE TRANSIENT SCHEMA"));
    }
    
    @Test
    @DisplayName("Should create schema WITH MANAGED ACCESS from namespace attribute")
    void shouldCreateManagedAccessSchemaFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("managedAccess", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("WITH MANAGED ACCESS"));
    }
    
    @Test
    @DisplayName("Should create schema with CLONE from namespace attribute")
    void shouldCreateCloneSchemaFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("cloneFrom", "SOURCE_SCHEMA");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        when(database.escapeObjectName("SOURCE_SCHEMA", liquibase.structure.core.Schema.class))
            .thenReturn("SOURCE_SCHEMA");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CLONE SOURCE_SCHEMA"));
    }
    
    // Time Travel and Storage Attributes Tests
    
    @Test
    @DisplayName("Should add data retention from namespace attribute")
    void shouldAddDataRetentionFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("dataRetentionTimeInDays", "30");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS = 30"));
    }
    
    @Test
    @DisplayName("Should add max data extension from namespace attribute")
    void shouldAddMaxDataExtensionFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("maxDataExtensionTimeInDays", "60");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS = 60"));
    }
    
    @Test
    @DisplayName("Should add default DDL collation from namespace attribute")
    void shouldAddDefaultDdlCollationFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("defaultDdlCollation", "en-ci");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION = 'en-ci'"));
    }
    
    @Test
    @DisplayName("Should add pipe execution paused from namespace attribute")
    void shouldAddPipeExecutionPausedFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("pipeExecutionPaused", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("PIPE_EXECUTION_PAUSED = TRUE"));
    }
    
    // Advanced Snowflake Attributes Tests
    
    @Test
    @DisplayName("Should add external volume from namespace attribute")
    void shouldAddExternalVolumeFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("externalVolume", "external_vol");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("EXTERNAL_VOLUME = 'external_vol'"));
    }
    
    @Test
    @DisplayName("Should add catalog from namespace attribute")
    void shouldAddCatalogFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("catalog", "test_catalog");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CATALOG = 'test_catalog'"));
    }
    
    @Test
    @DisplayName("Should add classification profile from namespace attribute")
    void shouldAddClassificationProfileFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("classificationProfile", "sensitive_profile");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CLASSIFICATION_PROFILE = 'sensitive_profile'"));
    }
    
    @Test
    @DisplayName("Should add replace invalid characters from namespace attribute")
    void shouldAddReplaceInvalidCharactersFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("replaceInvalidCharacters", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("REPLACE_INVALID_CHARACTERS = TRUE"));
    }
    
    @Test
    @DisplayName("Should add storage serialization policy from namespace attribute")
    void shouldAddStorageSerializationPolicyFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("storageSerializationPolicy", "optimized");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("STORAGE_SERIALIZATION_POLICY = optimized"));
    }
    
    @Test
    @DisplayName("Should add tag from namespace attribute")
    void shouldAddTagFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("tag", "production");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("TAG (production)"));
    }
    
    @Test
    @DisplayName("Should add comment from namespace attribute")
    void shouldAddCommentFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("comment", "Production schema with enhanced features");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("COMMENT = 'Production schema with enhanced features'"));
    }
    
    // Combined Operations Tests
    
    @Test
    @DisplayName("Should combine multiple namespace attributes correctly")
    void shouldCombineMultipleNamespaceAttributes() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("orReplace", "true");
        attrs.put("transient", "true");
        attrs.put("managedAccess", "true");
        attrs.put("dataRetentionTimeInDays", "0"); // Must be 0 for transient
        attrs.put("comment", "Complex schema configuration");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CREATE OR REPLACE TRANSIENT SCHEMA"));
        assertTrue(sql.contains("WITH MANAGED ACCESS"));
        assertTrue(sql.contains("DATA_RETENTION_TIME_IN_DAYS = 0"));
        assertTrue(sql.contains("COMMENT = 'Complex schema configuration'"));
    }
    
    @Test
    @DisplayName("Should handle clone with additional properties")
    void shouldHandleCloneWithAdditionalProperties() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("cloneFrom", "PROD_SCHEMA");
        attrs.put("managedAccess", "true");
        attrs.put("comment", "Development clone of production");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        when(database.escapeObjectName("PROD_SCHEMA", liquibase.structure.core.Schema.class))
            .thenReturn("PROD_SCHEMA");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("CLONE PROD_SCHEMA"));
        assertTrue(sql.contains("WITH MANAGED ACCESS"));
        assertTrue(sql.contains("COMMENT = 'Development clone of production'"));
    }
    
    // Validation Tests
    
    @Test
    @DisplayName("Should validate mutual exclusivity of OR REPLACE and IF NOT EXISTS")
    void shouldValidateMutualExclusityOfOrReplaceAndIfNotExists() {
        // Given - set conflicting statement attributes (not namespace attributes)
        statement.setOrReplace(true);
        statement.setIfNotExists(true);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("OR REPLACE and IF NOT EXISTS are mutually exclusive")));
    }
    
    @Test
    @DisplayName("Should validate transient schema constraint")
    void shouldValidateTransientSchemaConstraint() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("transient", "true");
        attrs.put("dataRetentionTimeInDays", "7"); // Invalid for transient
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("TRANSIENT schemas must have DATA_RETENTION_TIME_IN_DAYS = 0")));
    }
    
    @Test
    @DisplayName("Should validate data retention constraints")
    void shouldValidateDataRetentionConstraints() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("dataRetentionTimeInDays", "60");
        attrs.put("maxDataExtensionTimeInDays", "30"); // Invalid: max < retention
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("MAX_DATA_EXTENSION_TIME_IN_DAYS must be >= DATA_RETENTION_TIME_IN_DAYS")));
    }
    
    @Test
    @DisplayName("Should validate comment length")
    void shouldValidateCommentLength() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) sb.append("A"); // > 256 chars
        String longComment = sb.toString();
        attrs.put("comment", longComment);
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, sqlGeneratorChain);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Schema comment cannot exceed 256 characters")));
    }
    
    @Test
    @DisplayName("Should prefer namespace attributes over statement attributes")
    void shouldPreferNamespaceAttributesOverStatementAttributes() {
        // Given - both namespace and statement attributes
        Map<String, String> attrs = new HashMap<>();
        attrs.put("comment", "Namespace comment");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        statement.setComment("Statement comment");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then - namespace attribute should win
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("COMMENT = 'Namespace comment'"));
        assertFalse(sql.contains("Statement comment"));
    }
    
    @Test
    @DisplayName("Should handle special characters in string attributes")
    void shouldHandleSpecialCharactersInStringAttributes() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("comment", "Schema with 'quotes' and \"double quotes\"");
        attrs.put("defaultDdlCollation", "en-'special'-ci");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When  
        Sql[] sqls = generator.generateSql(statement, database, sqlGeneratorChain);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("COMMENT = 'Schema with ''quotes'' and \"double quotes\"'"));
        assertTrue(sql.contains("DEFAULT_DDL_COLLATION = 'en-''special''-ci'"));
    }
}