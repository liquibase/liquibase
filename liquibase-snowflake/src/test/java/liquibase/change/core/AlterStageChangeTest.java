package liquibase.change.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterStageStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for AlterStageChange with 90%+ coverage focus.
 * Tests validation, property management, and ALTER operations.
 */
@DisplayName("AlterStageChange")
public class AlterStageChangeTest {
    
    private AlterStageChange change;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        change = new AlterStageChange();
        database = new SnowflakeDatabase();
    }
    
    // ==================== Basic Functionality Tests ====================
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(change.supports(database));
    }
    
    @Test
    @DisplayName("Should not support non-Snowflake database") 
    void shouldNotSupportNonSnowflakeDatabase() {
        liquibase.database.Database h2Database = org.mockito.Mockito.mock(liquibase.database.Database.class);
        assertFalse(change.supports(h2Database));
    }
    
    // ==================== Stage Name Tests ====================
    
    @Test
    @DisplayName("Should handle stageName getter and setter")
    void shouldHandleStageNameGetterAndSetter() {
        // Test default value
        assertNull(change.getStageName());
        
        // Test setting value
        change.setStageName("MY_STAGE");
        assertEquals("MY_STAGE", change.getStageName());
        
        // Test setting different value
        change.setStageName("ANOTHER_STAGE");
        assertEquals("ANOTHER_STAGE", change.getStageName());
        
        // Test setting to null
        change.setStageName(null);
        assertNull(change.getStageName());
    }
    
    // ==================== Property Management Tests ====================
    
    @Test
    @DisplayName("Should handle object properties correctly")
    void shouldHandleObjectPropertiesCorrectly() {
        // Test setting property
        change.setObjectProperty("url", "s3://mybucket");
        assertEquals("s3://mybucket", change.getObjectProperty("url"));
        
        // Test null value handling
        change.setObjectProperty("encryption", null);
        assertNull(change.getObjectProperty("encryption"));
        
        // Test getting non-existent property
        assertNull(change.getObjectProperty("nonExistent"));
        
        // Test getAllObjectProperties
        change.setObjectProperty("storageIntegration", "MY_INTEGRATION");
        assertNotNull(change.getAllObjectProperties());
        assertTrue(change.getAllObjectProperties().containsKey("url"));
        assertTrue(change.getAllObjectProperties().containsKey("storageIntegration"));
    }
    
    // ==================== Schema Properties Tests ====================
    
    @Test
    @DisplayName("Should handle schema properties correctly")
    void shouldHandleSchemaPropertiesCorrectly() {
        // Test setCatalogName
        change.setCatalogName("MY_DATABASE");
        assertEquals("MY_DATABASE", change.getCatalogName());
        
        // Test setSchemaName
        change.setSchemaName("MY_SCHEMA");
        assertEquals("MY_SCHEMA", change.getSchemaName());
    }
    
    // ==================== SET Operations Tests ====================
    
    @Test
    @DisplayName("Should handle URL property")
    void shouldHandleUrlProperty() {
        change.setUrl("s3://my-bucket/path/");
        assertEquals("s3://my-bucket/path/", change.getObjectProperty("url"));
    }
    
    @Test
    @DisplayName("Should handle storage integration property")
    void shouldHandleStorageIntegrationProperty() {
        change.setStorageIntegration("MY_STORAGE_INTEGRATION");
        assertEquals("MY_STORAGE_INTEGRATION", change.getObjectProperty("storageIntegration"));
    }
    
    @Test
    @DisplayName("Should handle comment property")
    void shouldHandleCommentProperty() {
        change.setComment("Stage for data loading");
        assertEquals("Stage for data loading", change.getObjectProperty("comment"));
    }
    
    @Test
    @DisplayName("Should handle encryption property")
    void shouldHandleEncryptionProperty() {
        change.setEncryption("AES256");
        assertEquals("AES256", change.getObjectProperty("encryption"));
    }
    
    @Test
    @DisplayName("Should handle file format property")
    void shouldHandleFileFormatProperty() {
        change.setFileFormat("CSV");
        assertEquals("CSV", change.getObjectProperty("fileFormat"));
    }
    
    @Test
    @DisplayName("Should handle directory enable property")
    void shouldHandleDirectoryEnableProperty() {
        change.setDirectoryEnable(true);
        assertEquals("true", change.getObjectProperty("directoryEnable"));
        
        change.setDirectoryEnable(false);
        assertEquals("false", change.getObjectProperty("directoryEnable"));
        
        change.setDirectoryEnable(null);
        // Should not add null values
        assertFalse(change.getAllObjectProperties().containsKey("directoryEnable") && 
                    change.getAllObjectProperties().get("directoryEnable") != null);
    }
    
    // ==================== Cloud Credentials Tests ====================
    
    @Test
    @DisplayName("Should handle AWS credentials")
    void shouldHandleAwsCredentials() {
        change.setAwsKeyId("AKIATEST123");
        change.setAwsSecretKey("secretkey123");
        change.setAwsToken("sessiontoken123");
        change.setAwsRole("arn:aws:iam::123:role/MyRole");
        
        assertEquals("AKIATEST123", change.getObjectProperty("awsKeyId"));
        assertEquals("secretkey123", change.getObjectProperty("awsSecretKey"));
        assertEquals("sessiontoken123", change.getObjectProperty("awsToken"));
        assertEquals("arn:aws:iam::123:role/MyRole", change.getObjectProperty("awsRole"));
    }
    
    @Test
    @DisplayName("Should handle GCS credentials")
    void shouldHandleGcsCredentials() {
        change.setGcsServiceAccountKey("key123");
        assertEquals("key123", change.getObjectProperty("gcsServiceAccountKey"));
    }
    
    @Test
    @DisplayName("Should handle Azure credentials")
    void shouldHandleAzureCredentials() {
        change.setAzureAccountName("myaccount");
        change.setAzureAccountKey("key123");
        change.setAzureSasToken("sas123");
        
        assertEquals("myaccount", change.getObjectProperty("azureAccountName"));
        assertEquals("key123", change.getObjectProperty("azureAccountKey"));
        assertEquals("sas123", change.getObjectProperty("azureSasToken"));
    }
    
    // ==================== UNSET Operations Tests ====================
    
    @Test
    @DisplayName("Should handle UNSET operations")
    void shouldHandleUnsetOperations() {
        change.setUnsetUrl(true);
        change.setUnsetStorageIntegration(true);
        change.setUnsetCredentials(true);
        change.setUnsetEncryption(true);
        change.setUnsetFileFormat(true);
        change.setUnsetComment(true);
        
        assertEquals("true", change.getObjectProperty("unsetUrl"));
        assertEquals("true", change.getObjectProperty("unsetStorageIntegration"));
        assertEquals("true", change.getObjectProperty("unsetCredentials"));
        assertEquals("true", change.getObjectProperty("unsetEncryption"));
        assertEquals("true", change.getObjectProperty("unsetFileFormat"));
        assertEquals("true", change.getObjectProperty("unsetComment"));
    }
    
    // ==================== Additional Operations Tests ====================
    
    @Test
    @DisplayName("Should handle rename operation")
    void shouldHandleRenameOperation() {
        change.setRenameTo("NEW_STAGE_NAME");
        assertEquals("NEW_STAGE_NAME", change.getObjectProperty("renameTo"));
    }
    
    @Test
    @DisplayName("Should handle tag operations")
    void shouldHandleTagOperations() {
        change.setTagName("Environment");
        change.setTagValue("Production");
        change.setUnsetTagName("OldTag");
        
        assertEquals("Environment", change.getObjectProperty("tagName"));
        assertEquals("Production", change.getObjectProperty("tagValue"));
        assertEquals("OldTag", change.getObjectProperty("unsetTagName"));
    }
    
    @Test
    @DisplayName("Should handle if exists flag")
    void shouldHandleIfExistsFlag() {
        // Test default value (null)
        assertNull(change.getIfExists());
        
        // Test setting to true
        change.setIfExists(true);
        assertTrue(change.getIfExists());
        
        // Test setting to false
        change.setIfExists(false);
        assertFalse(change.getIfExists());
        
        // Test setting to null
        change.setIfExists(null);
        assertNull(change.getIfExists());
    }
    
    @Test
    @DisplayName("Should handle refresh operations")
    void shouldHandleRefreshOperations() {
        change.setRefreshDirectory(true);
        change.setRefreshSubpath("/subpath");
        
        assertEquals("true", change.getObjectProperty("refreshDirectory"));
        assertEquals("/subpath", change.getObjectProperty("refreshSubpath"));
    }
    
    // ==================== Validation Tests ====================
    
    @Test
    @DisplayName("Should require stageName")
    void shouldRequireStageName() {
        // Given - Change without required stageName
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorMessages().size());
        assertTrue(errors.getErrorMessages().get(0).contains("stageName is required"));
    }
    
    @Test
    @DisplayName("Should validate empty stageName as invalid")
    void shouldValidateEmptyStageNameAsInvalid() {
        // Given
        change.setStageName("");
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("stageName is required")));
    }
    
    @Test
    @DisplayName("Should validate whitespace-only stageName as invalid")
    void shouldValidateWhitespaceOnlyStageNameAsInvalid() {
        // Given
        change.setStageName("   ");
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("stageName is required")));
    }
    
    @Test
    @DisplayName("Should pass validation with valid stageName and operation")
    void shouldPassValidationWithValidStageNameAndOperation() {
        // Given
        change.setStageName("VALID_STAGE");
        change.setComment("Valid comment"); // Add at least one operation
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should validate SET and UNSET conflict for same property")
    void shouldValidateSetAndUnsetConflict() {
        // Given
        change.setStageName("TEST_STAGE");
        change.setUrl("s3://bucket");       // SET url
        change.setUnsetUrl(true);           // UNSET url (conflict!)
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot both SET and UNSET the same property")));
    }
    
    @Test
    @DisplayName("Should require at least one operation")
    void shouldRequireAtLeastOneOperation() {
        // Given
        change.setStageName("TEST_STAGE");
        // No SET, UNSET, RENAME, or REFRESH operations
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("ALTER STAGE requires at least one SET, UNSET, RENAME, or REFRESH operation")));
    }
    
    // ==================== Statement Generation Tests ====================
    
    @Test
    @DisplayName("Should generate basic alter stage statement")
    void shouldGenerateBasicAlterStageStatement() {
        // Given
        change.setStageName("TEST_STAGE");
        change.setUrl("s3://my-bucket");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof AlterStageStatement);
        
        AlterStageStatement stmt = (AlterStageStatement) statements[0];
        assertEquals("TEST_STAGE", stmt.getStageName());
        assertEquals("s3://my-bucket", stmt.getObjectProperty("url"));
    }
    
    @Test
    @DisplayName("Should generate statement with schema qualification")
    void shouldGenerateStatementWithSchemaQualification() {
        // Given
        change.setStageName("QUALIFIED_STAGE");
        change.setSchemaName("MY_SCHEMA");
        change.setCatalogName("MY_DATABASE");
        change.setComment("Updated comment");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        assertEquals(1, statements.length);
        AlterStageStatement stmt = (AlterStageStatement) statements[0];
        
        assertEquals("QUALIFIED_STAGE", stmt.getStageName());
        assertEquals("MY_SCHEMA", stmt.getObjectProperty("schemaName"));
        assertEquals("MY_DATABASE", stmt.getObjectProperty("catalogName"));
        assertEquals("Updated comment", stmt.getObjectProperty("comment"));
    }
    
    @Test
    @DisplayName("Should generate statement with all properties")
    void shouldGenerateStatementWithAllProperties() {
        // Given
        change.setStageName("FULL_ALTER_STAGE");
        change.setSchemaName("TEST_SCHEMA");
        change.setCatalogName("TEST_DB");
        change.setIfExists(true);
        
        // SET operations
        change.setUrl("s3://updated-bucket");
        change.setStorageIntegration("NEW_INTEGRATION");
        change.setComment("Updated stage comment");
        change.setEncryption("AES256");
        
        // UNSET operations
        change.setUnsetCredentials(true);
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        assertEquals(1, statements.length);
        AlterStageStatement stmt = (AlterStageStatement) statements[0];
        
        assertEquals("FULL_ALTER_STAGE", stmt.getStageName());
        assertEquals("TEST_SCHEMA", stmt.getObjectProperty("schemaName"));
        assertEquals("TEST_DB", stmt.getObjectProperty("catalogName"));
        assertEquals("true", stmt.getObjectProperty("ifExists"));
        assertEquals("s3://updated-bucket", stmt.getObjectProperty("url"));
        assertEquals("NEW_INTEGRATION", stmt.getObjectProperty("storageIntegration"));
        assertEquals("Updated stage comment", stmt.getObjectProperty("comment"));
        assertEquals("AES256", stmt.getObjectProperty("encryption"));
        assertEquals("true", stmt.getObjectProperty("unsetCredentials"));
    }
    
    // ==================== Confirmation Message Tests ====================
    
    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        // Given
        change.setStageName("TEST_STAGE");
        
        // When
        String message = change.getConfirmationMessage();
        
        // Then
        assertNotNull(message);
        assertTrue(message.contains("Stage TEST_STAGE altered"));
    }
    
    @Test
    @DisplayName("Should generate confirmation message with schema qualification")
    void shouldGenerateConfirmationMessageWithSchemaQualification() {
        // Given
        change.setStageName("QUALIFIED_STAGE");
        change.setSchemaName("MY_SCHEMA");
        change.setCatalogName("MY_DB");
        
        // When
        String message = change.getConfirmationMessage();
        
        // Then
        assertNotNull(message);
        assertTrue(message.contains("Stage"));
        assertTrue(message.contains("QUALIFIED_STAGE"));
        assertTrue(message.contains("altered"));
    }
    
    // ==================== Helper Method Tests ====================
    
    @Test
    @DisplayName("Should detect SET operations correctly")
    void shouldDetectSetOperationsCorrectly() {
        // Given - no operations
        change.setStageName("TEST_STAGE");
        assertFalse(change.hasAnySetOperation());
        
        // When - add SET operation
        change.setUrl("s3://bucket");
        
        // Then
        assertTrue(change.hasAnySetOperation());
    }
    
    @Test
    @DisplayName("Should detect UNSET operations correctly")
    void shouldDetectUnsetOperationsCorrectly() {
        // Given - no operations
        change.setStageName("TEST_STAGE");
        assertFalse(change.hasAnyUnsetOperation());
        
        // When - add UNSET operation
        change.setUnsetUrl(true);
        
        // Then
        assertTrue(change.hasAnyUnsetOperation());
    }
    
    @Test
    @DisplayName("Should detect conflicting SET/UNSET operations")
    void shouldDetectConflictingSetUnsetOperations() {
        // Given
        change.setStageName("TEST_STAGE");
        
        // No conflicts initially
        assertFalse(change.hasConflictingSetUnset());
        
        // Add SET operation - still no conflict
        change.setUrl("s3://bucket");
        assertFalse(change.hasConflictingSetUnset());
        
        // Add conflicting UNSET operation for same property
        change.setUnsetUrl(true);
        assertTrue(change.hasConflictingSetUnset());
    }
}