package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import liquibase.sql.Sql;
import liquibase.statement.core.CreateSchemaStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure SQL tests for CreateSchemaGeneratorSnowflake namespace attribute support.
 * Tests SQL generation without mocks, focusing on actual string output validation.
 */
@DisplayName("CreateSchemaGeneratorSnowflake Namespace Attributes")
public class CreateSchemaGeneratorSnowflakeNamespaceTest {
    
    private CreateSchemaGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    private CreateSchemaStatement statement;
    
    @BeforeEach
    void setUp() {
        generator = new CreateSchemaGeneratorSnowflake();
        database = new SnowflakeDatabase();
        statement = new CreateSchemaStatement();
        statement.setSchemaName("TEST_SCHEMA");
        
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
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE OR REPLACE SCHEMA TEST_SCHEMA", sql);
        
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
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA IF NOT EXISTS TEST_SCHEMA", sql);
    }
    
    @Test
    @DisplayName("Should create TRANSIENT schema from namespace attribute")
    void shouldCreateTransientSchemaFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("transient", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE TRANSIENT SCHEMA TEST_SCHEMA", sql);
    }
    
    @Test
    @DisplayName("Should create schema WITH MANAGED ACCESS from namespace attribute")
    void shouldCreateManagedAccessSchemaFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("managedAccess", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA WITH MANAGED ACCESS", sql);
    }
    
    @Test
    @DisplayName("Should create schema with CLONE from namespace attribute")
    void shouldCreateCloneSchemaFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("cloneFrom", "SOURCE_SCHEMA");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // No mock needed - testing actual SQL generation
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA CLONE SOURCE_SCHEMA", sql);
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
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA DATA_RETENTION_TIME_IN_DAYS = 30", sql);
    }
    
    @Test
    @DisplayName("Should add max data extension from namespace attribute")
    void shouldAddMaxDataExtensionFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("maxDataExtensionTimeInDays", "60");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA MAX_DATA_EXTENSION_TIME_IN_DAYS = 60", sql);
    }
    
    @Test
    @DisplayName("Should add default DDL collation from namespace attribute")
    void shouldAddDefaultDdlCollationFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("defaultDdlCollation", "en-ci");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA DEFAULT_DDL_COLLATION = 'en-ci'", sql);
    }
    
    @Test
    @DisplayName("Should add pipe execution paused from namespace attribute")
    void shouldAddPipeExecutionPausedFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("pipeExecutionPaused", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA PIPE_EXECUTION_PAUSED = TRUE", sql);
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
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA EXTERNAL_VOLUME = 'external_vol'", sql);
    }
    
    @Test
    @DisplayName("Should add catalog from namespace attribute")
    void shouldAddCatalogFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("catalog", "test_catalog");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA CATALOG = 'test_catalog'", sql);
    }
    
    @Test
    @DisplayName("Should add classification profile from namespace attribute")
    void shouldAddClassificationProfileFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("classificationProfile", "sensitive_profile");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA CLASSIFICATION_PROFILE = 'sensitive_profile'", sql);
    }
    
    @Test
    @DisplayName("Should add replace invalid characters from namespace attribute")
    void shouldAddReplaceInvalidCharactersFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("replaceInvalidCharacters", "true");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA REPLACE_INVALID_CHARACTERS = TRUE", sql);
    }
    
    @Test
    @DisplayName("Should add storage serialization policy from namespace attribute")
    void shouldAddStorageSerializationPolicyFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("storageSerializationPolicy", "optimized");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA STORAGE_SERIALIZATION_POLICY = optimized", sql);
    }
    
    @Test
    @DisplayName("Should add tag from namespace attribute")
    void shouldAddTagFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("tag", "production");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA TAG (production)", sql);
    }
    
    @Test
    @DisplayName("Should add comment from namespace attribute")
    void shouldAddCommentFromNamespaceAttribute() {
        // Given
        Map<String, String> attrs = new HashMap<>();
        attrs.put("comment", "Production schema with enhanced features");
        SnowflakeNamespaceAttributeStorage.storeAttributes("TEST_SCHEMA", attrs);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA COMMENT = 'Production schema with enhanced features'", sql);
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
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE OR REPLACE TRANSIENT SCHEMA TEST_SCHEMA WITH MANAGED ACCESS DATA_RETENTION_TIME_IN_DAYS = 0 COMMENT = 'Complex schema configuration'", sql);
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
        
        // No mock needed - testing actual SQL generation
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA CLONE PROD_SCHEMA WITH MANAGED ACCESS COMMENT = 'Development clone of production'", sql);
    }
    
    // Validation Tests
    
    @Test
    @DisplayName("Should validate mutual exclusivity of OR REPLACE and IF NOT EXISTS")
    void shouldValidateMutualExclusityOfOrReplaceAndIfNotExists() {
        // Given - set conflicting statement attributes (not namespace attributes)
        statement.setOrReplace(true);
        statement.setIfNotExists(true);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
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
        ValidationErrors errors = generator.validate(statement, database, null);
        
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
        ValidationErrors errors = generator.validate(statement, database, null);
        
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
        ValidationErrors errors = generator.validate(statement, database, null);
        
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
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then - namespace attribute should win
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA COMMENT = 'Namespace comment'", sql);
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
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String sql = sqls[0].toSql();
        assertEquals("CREATE SCHEMA TEST_SCHEMA DEFAULT_DDL_COLLATION = 'en-''special''-ci' COMMENT = 'Schema with ''quotes'' and \"double quotes\"'", sql);
    }
}