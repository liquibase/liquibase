package liquibase.sqlgenerator.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.DropStageStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests for DropStageGeneratorSnowflake SQL generation.
 * Tests complete SQL string output for DROP STAGE operations.
 * Follows the proven pattern: complete SQL string assertions.
 */
@DisplayName("DropStageGeneratorSnowflake - Pure SQL Tests")
public class DropStageGeneratorSnowflakeTest {
    
    private DropStageGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        generator = new DropStageGeneratorSnowflake();
        database = new SnowflakeDatabase(); // Real database object, no mocking needed
    }
    
    @Test
    @DisplayName("Should generate basic DROP STAGE SQL")
    void testBasicDropStage() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setStageName("TEST_STAGE");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("DROP STAGE TEST_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate DROP STAGE IF EXISTS SQL")
    void testDropStageIfExists() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setStageName("IF_EXISTS_STAGE");
        statement.setIfExists(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("DROP STAGE IF EXISTS IF_EXISTS_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate DROP STAGE with schema qualification SQL")
    void testDropStageWithSchemaQualification() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setCatalogName("MY_DB");
        statement.setSchemaName("MY_SCHEMA");
        statement.setStageName("QUALIFIED_STAGE");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("DROP STAGE MY_DB.MY_SCHEMA.QUALIFIED_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate DROP STAGE with schema-only qualification SQL")
    void testDropStageWithSchemaOnlyQualification() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setSchemaName("MY_SCHEMA");
        statement.setStageName("SCHEMA_STAGE");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("DROP STAGE MY_SCHEMA.SCHEMA_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate DROP STAGE IF EXISTS with full qualification SQL")
    void testDropStageIfExistsWithFullQualification() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setCatalogName("PROD_DB");
        statement.setSchemaName("DATA_SCHEMA");
        statement.setStageName("LEGACY_STAGE");
        statement.setIfExists(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("DROP STAGE IF EXISTS PROD_DB.DATA_SCHEMA.LEGACY_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle ifExists=false (explicit)")
    void testDropStageIfExistsFalse() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setStageName("EXPLICIT_STAGE");
        statement.setIfExists(false);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("DROP STAGE EXPLICIT_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle null ifExists (default behavior)")
    void testDropStageIfExistsNull() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setStageName("DEFAULT_STAGE");
        statement.setIfExists(null);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("DROP STAGE DEFAULT_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle complex stage names with special characters")
    void testDropStageWithComplexName() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setStageName("STAGE_WITH_UNDERSCORES_AND_123");
        statement.setIfExists(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("DROP STAGE IF EXISTS STAGE_WITH_UNDERSCORES_AND_123", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle mixed case names correctly")
    void testDropStageWithMixedCaseNames() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setCatalogName("MyDatabase");
        statement.setSchemaName("MySchema");
        statement.setStageName("MyStage");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("DROP STAGE MyDatabase.MySchema.MyStage", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle numeric suffixes in names")
    void testDropStageWithNumericSuffixes() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setCatalogName("DB_2023");
        statement.setSchemaName("SCHEMA_V2");
        statement.setStageName("STAGE_001");
        statement.setIfExists(true);
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("DROP STAGE IF EXISTS DB_2023.SCHEMA_V2.STAGE_001", sqls[0].toSql());
    }
    
    // ==================== Validation Tests ====================
    
    @Test
    @DisplayName("Should validate that stageName is required")
    void testValidationRequiresStageName() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        // stageName not set
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("stageName is required")));
    }
    
    @Test
    @DisplayName("Should validate empty stageName as invalid")
    void testValidationRejectsEmptyStageName() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setStageName("");
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("stageName is required")));
    }
    
    @Test
    @DisplayName("Should validate whitespace-only stageName as invalid")
    void testValidationRejectsWhitespaceOnlyStageName() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setStageName("   ");
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("stageName is required")));
    }
    
    @Test
    @DisplayName("Should pass validation with valid stageName")
    void testValidationPassesWithValidStageName() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setStageName("VALID_STAGE");
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should pass validation with all properties")
    void testValidationPassesWithAllProperties() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setCatalogName("VALID_DB");
        statement.setSchemaName("VALID_SCHEMA");
        statement.setStageName("VALID_STAGE");
        statement.setIfExists(true);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should pass validation with schema qualification but no catalog")
    void testValidationPassesWithSchemaQualification() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setSchemaName("VALID_SCHEMA");
        statement.setStageName("VALID_STAGE");
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertFalse(errors.hasErrors());
    }
    
    // ==================== Database Support Tests ====================
    
    @Test
    @DisplayName("Should support Snowflake database")
    void testSupportsSnowflakeDatabase() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        
        // When/Then
        assertTrue(generator.supports(statement, database));
    }
    
    @Test
    @DisplayName("Should not support non-Snowflake database")
    void testDoesNotSupportNonSnowflakeDatabase() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        liquibase.database.Database h2Database = org.mockito.Mockito.mock(liquibase.database.Database.class);
        
        // When/Then
        assertFalse(generator.supports(statement, h2Database));
    }
    
    // ==================== Edge Case Tests ====================
    
    @Test
    @DisplayName("Should handle null catalog with schema")
    void testNullCatalogWithSchema() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setCatalogName(null);
        statement.setSchemaName("MY_SCHEMA");
        statement.setStageName("MY_STAGE");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("DROP STAGE MY_SCHEMA.MY_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle empty string catalog")
    void testEmptyStringCatalog() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setCatalogName("");
        statement.setSchemaName("MY_SCHEMA");
        statement.setStageName("MY_STAGE");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        // Empty catalog should be treated as null - only schema qualification
        assertEquals("DROP STAGE MY_SCHEMA.MY_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should handle empty string schema")
    void testEmptyStringSchema() {
        // Given
        DropStageStatement statement = new DropStageStatement();
        statement.setCatalogName("MY_DB");
        statement.setSchemaName("");
        statement.setStageName("MY_STAGE");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        // Empty schema should result in no qualification
        assertEquals("DROP STAGE MY_STAGE", sqls[0].toSql());
    }
}