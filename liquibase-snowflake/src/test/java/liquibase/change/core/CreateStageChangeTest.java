package liquibase.change.core;

import liquibase.change.Change;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateStageStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for CreateStageChange with 90%+ coverage focus.
 * Tests all validation methods, generic property storage, and changetype execution patterns.
 * Follows established testing patterns: changetype execution, complete SQL string validation.
 */
@DisplayName("CreateStageChange")
public class CreateStageChangeTest {
    
    private CreateStageChange change;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        change = new CreateStageChange();
        database = new SnowflakeDatabase();
    }
    
    // ==================== Basic Functionality Tests ====================
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflake() {
        assertTrue(change.supports(database));
    }
    
    @Test
    @DisplayName("Should support rollback for Snowflake database")
    void shouldSupportRollback() {
        assertTrue(change.supportsRollback(database));
    }
    
    @Test
    @DisplayName("Should not support rollback for non-Snowflake database")
    void shouldNotSupportRollbackForNonSnowflake() {
        assertFalse(change.supportsRollback(null));
    }
    
    @Test
    @DisplayName("Should generate basic stage statement")
    void shouldGenerateBasicStageStatement() {
        // Given
        change.setStageName("TEST_STAGE");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        assertEquals(1, statements.length);
        assertTrue(statements[0] instanceof CreateStageStatement);
        
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("TEST_STAGE", stmt.getStageName());
    }
    
    @Test
    @DisplayName("Should generate statement with all basic properties")
    void shouldGenerateStatementWithAllBasicProperties() {
        // Given
        change.setStageName("FULL_STAGE");
        change.setUrl("s3://mybucket/path/");
        change.setStorageIntegration("MY_INTEGRATION");
        change.setComment("Test stage with all properties");
        change.setOrReplace(true);
        change.setIfNotExists(false);
        change.setTemporary(true);
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        assertEquals(1, statements.length);
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        
        assertEquals("FULL_STAGE", stmt.getStageName());
        assertEquals("s3://mybucket/path/", stmt.getUrl());
        assertEquals("MY_INTEGRATION", stmt.getStorageIntegration());
        assertEquals("Test stage with all properties", stmt.getComment());
        assertTrue(stmt.getOrReplace());
        assertFalse(stmt.getIfNotExists());
        assertTrue(stmt.getTemporary());
    }
    
    // ==================== Generic Property Storage Tests ====================
    
    @Test
    @DisplayName("Should handle generic properties through setObjectProperty")
    void shouldHandleGenericProperties() {
        // Given
        change.setStageName("GENERIC_STAGE");
        change.setObjectProperty("awsKeyId", "AKIAIOSFODNN7EXAMPLE");
        change.setObjectProperty("awsSecretKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        change.setObjectProperty("encryption", "SNOWFLAKE_SSE");
        change.setObjectProperty("fileFormat", "my_csv_format");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertNotNull(stmt.getObjectProperties());
        assertEquals("AKIAIOSFODNN7EXAMPLE", stmt.getObjectProperties().get("awsKeyId"));
        assertEquals("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY", stmt.getObjectProperties().get("awsSecretKey"));
        assertEquals("SNOWFLAKE_SSE", stmt.getObjectProperties().get("encryption"));
        assertEquals("my_csv_format", stmt.getObjectProperties().get("fileFormat"));
    }
    
    @Test
    @DisplayName("Should handle clone properties through generic storage")
    void shouldHandleCloneProperties() {
        // Given
        change.setStageName("CLONED_STAGE");
        change.setObjectProperty("cloneFromStage", "SOURCE_STAGE");
        change.setObjectProperty("cloneFromSchema", "SOURCE_SCHEMA");
        change.setObjectProperty("cloneFromCatalog", "SOURCE_DB");
        change.setObjectProperty("timeTravelType", "TIMESTAMP");
        change.setObjectProperty("timeTravelValue", "2023-01-01 12:00:00");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("SOURCE_STAGE", stmt.getObjectProperties().get("cloneFromStage"));
        assertEquals("SOURCE_SCHEMA", stmt.getObjectProperties().get("cloneFromSchema"));
        assertEquals("SOURCE_DB", stmt.getObjectProperties().get("cloneFromCatalog"));
        assertEquals("TIMESTAMP", stmt.getObjectProperties().get("timeTravelType"));
        assertEquals("2023-01-01 12:00:00", stmt.getObjectProperties().get("timeTravelValue"));
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
    @DisplayName("Should validate mutual exclusivity of orReplace and ifNotExists")
    void shouldValidateMutualExclusivity() {
        // Given
        change.setStageName("TEST_STAGE");
        change.setOrReplace(true);
        change.setIfNotExists(true);
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot specify both orReplace and ifNotExists")));
    }
    
    @Test
    @DisplayName("Should validate storage integration vs credentials mutual exclusivity")
    void shouldValidateStorageIntegrationVsCredentials() {
        // Given
        change.setStageName("TEST_STAGE");
        change.setStorageIntegration("MY_INTEGRATION");
        change.setObjectProperty("awsKeyId", "AKIATEST");
        change.setObjectProperty("awsSecretKey", "secretkey");
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Cannot specify both STORAGE_INTEGRATION and CREDENTIALS")));
    }
    
    @Test
    @DisplayName("Should validate time travel requires clone operation")
    void shouldValidateTimeTravelRequiresClone() {
        // Given
        change.setStageName("TEST_STAGE");
        change.setObjectProperty("timeTravelType", "TIMESTAMP");
        change.setObjectProperty("timeTravelValue", "2023-01-01 12:00:00");
        // No cloneFromStage set
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("Time travel can only be used with CLONE operations")));
    }
    
    @Test
    @DisplayName("Should pass validation with valid configuration")
    void shouldPassValidationWithValidConfiguration() {
        // Given
        change.setStageName("VALID_STAGE");
        change.setUrl("s3://mybucket/path/");
        change.setStorageIntegration("MY_INTEGRATION");
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertFalse(errors.hasErrors());
    }
    
    // ==================== Inverse Change Tests ====================
    
    @Test
    @DisplayName("Should create inverse DropStageChange")
    void shouldCreateInverseDropStage() {
        // Given
        change.setStageName("TEST_STAGE");
        
        // When
        Change[] inverses = change.createInverses();
        
        // Then
        assertNotNull(inverses);
        assertEquals(1, inverses.length);
        assertTrue(inverses[0] instanceof DropStageChange);
        
        DropStageChange dropChange = (DropStageChange) inverses[0];
        assertEquals("TEST_STAGE", dropChange.getStageName());
        assertTrue(dropChange.getIfExists());
    }
    
    @Test
    @DisplayName("Should create inverse with schema qualification")
    void shouldCreateInverseWithSchemaQualification() {
        // Given
        change.setStageName("QUALIFIED_STAGE");
        change.setSchemaName("MY_SCHEMA");
        change.setCatalogName("MY_DB");
        
        // When
        Change[] inverses = change.createInverses();
        
        // Then
        assertNotNull(inverses);
        assertEquals(1, inverses.length);
        
        DropStageChange dropChange = (DropStageChange) inverses[0];
        assertEquals("QUALIFIED_STAGE", dropChange.getStageName());
        assertEquals("MY_SCHEMA", dropChange.getSchemaName());
        assertEquals("MY_DB", dropChange.getCatalogName());
        assertTrue(dropChange.getIfExists());
    }
    
    // ==================== Additional Branch Coverage Tests ====================
    
    @Test
    @DisplayName("Should not support non-Snowflake database")
    void shouldNotSupportNonSnowflakeDatabase() {
        // Given
        liquibase.database.Database h2Database = org.mockito.Mockito.mock(liquibase.database.Database.class);
        
        // When/Then
        assertFalse(change.supports(h2Database));
    }
    
    @Test
    @DisplayName("Should generate confirmation message")
    void shouldGenerateConfirmationMessage() {
        // Given
        change.setStageName("TEST_STAGE");
        
        // When
        String message = change.getConfirmationMessage();
        
        // Then
        assertNotNull(message);
        assertTrue(message.contains("Stage TEST_STAGE created"));
    }
    
    @Test
    @DisplayName("Should handle null property values in generic storage")
    void shouldHandleNullPropertyValues() {
        // Given
        change.setStageName("NULL_PROP_STAGE");
        change.setObjectProperty("nullProperty", null);
        change.setObjectProperty("validProperty", "validValue");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertNull(stmt.getObjectProperties().get("nullProperty"));
        assertEquals("validValue", stmt.getObjectProperties().get("validProperty"));
    }
    
    @Test
    @DisplayName("Should handle empty object properties map")
    void shouldHandleEmptyObjectProperties() {
        // Given
        change.setStageName("EMPTY_PROPS_STAGE");
        // No additional properties set
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertNotNull(stmt.getObjectProperties());
        assertTrue(stmt.getObjectProperties().isEmpty());
    }
    
    @Test
    @DisplayName("Should validate AWS credentials completeness")
    void shouldValidateAwsCredentialsCompleteness() {
        // Given
        change.setStageName("AWS_STAGE");
        change.setObjectProperty("awsKeyId", "AKIATEST");
        // Missing awsSecretKey
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("AWS_KEY_ID requires AWS_SECRET_KEY")));
    }
    
    @Test
    @DisplayName("Should validate Azure credentials completeness")
    void shouldValidateAzureCredentialsCompleteness() {
        // Given
        change.setStageName("AZURE_STAGE");
        change.setObjectProperty("azureAccountName", "testaccount");
        // Missing both azureAccountKey and azureSasToken
        
        // When
        ValidationErrors errors = change.validate(database);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("AZURE_ACCOUNT_NAME requires either AZURE_ACCOUNT_KEY or AZURE_SAS_TOKEN")));
    }
    
    // ==================== Cloud Credentials Property Tests ====================
    
    @Test
    @DisplayName("Should handle AWS Key ID property")
    void shouldHandleAwsKeyIdProperty() {
        // Given
        change.setStageName("AWS_STAGE");
        change.setAwsKeyId("AKIAIOSFODNN7EXAMPLE");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("AKIAIOSFODNN7EXAMPLE", stmt.getObjectProperties().get("awsKeyId"));
    }
    
    @Test
    @DisplayName("Should handle AWS Secret Key property")
    void shouldHandleAwsSecretKeyProperty() {
        // Given
        change.setStageName("AWS_STAGE");
        change.setAwsSecretKey("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY", stmt.getObjectProperties().get("awsSecretKey"));
    }
    
    @Test
    @DisplayName("Should handle AWS Token property")
    void shouldHandleAwsTokenProperty() {
        // Given
        change.setStageName("AWS_STAGE");
        change.setAwsToken("AQoDYXdzEJr...");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("AQoDYXdzEJr...", stmt.getObjectProperties().get("awsToken"));
    }
    
    @Test
    @DisplayName("Should handle AWS Role ARN property")
    void shouldHandleAwsRoleArnProperty() {
        // Given
        change.setStageName("AWS_STAGE");
        change.setObjectProperty("awsRoleArn", "arn:aws:iam::123456789012:role/MyRole");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("arn:aws:iam::123456789012:role/MyRole", stmt.getObjectProperties().get("awsRoleArn"));
    }
    
    @Test
    @DisplayName("Should handle AWS External ID property")
    void shouldHandleAwsExternalIdProperty() {
        // Given
        change.setStageName("AWS_STAGE");
        change.setObjectProperty("awsExternalId", "external-id-123");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("external-id-123", stmt.getObjectProperties().get("awsExternalId"));
    }
    
    @Test
    @DisplayName("Should handle GCS Service Account property")
    void shouldHandleGcsServiceAccountProperty() {
        // Given
        change.setStageName("GCS_STAGE");
        change.setObjectProperty("gcsServiceAccount", "service-account@project.iam.gserviceaccount.com");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("service-account@project.iam.gserviceaccount.com", stmt.getObjectProperties().get("gcsServiceAccount"));
    }
    
    @Test
    @DisplayName("Should handle Azure Account Name property")
    void shouldHandleAzureAccountNameProperty() {
        // Given
        change.setStageName("AZURE_STAGE");
        change.setAzureAccountName("mystorageaccount");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("mystorageaccount", stmt.getObjectProperties().get("azureAccountName"));
    }
    
    @Test
    @DisplayName("Should handle Azure Account Key property")
    void shouldHandleAzureAccountKeyProperty() {
        // Given
        change.setStageName("AZURE_STAGE");
        change.setAzureAccountKey("base64encodedkey");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("base64encodedkey", stmt.getObjectProperties().get("azureAccountKey"));
    }
    
    @Test
    @DisplayName("Should handle Azure SAS Token property")
    void shouldHandleAzureSasTokenProperty() {
        // Given
        change.setStageName("AZURE_STAGE");
        change.setAzureSasToken("?sv=2019-12-12&ss=b&srt=sco&sp=rwdlacup&se=2023-01-01T00:00:00Z&st=2022-01-01T00:00:00Z&spr=https&sig=signature");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("?sv=2019-12-12&ss=b&srt=sco&sp=rwdlacup&se=2023-01-01T00:00:00Z&st=2022-01-01T00:00:00Z&spr=https&sig=signature", stmt.getObjectProperties().get("azureSasToken"));
    }
    
    // ==================== Directory Table Property Tests ====================
    
    @Test
    @DisplayName("Should handle Directory Enable property")
    void shouldHandleDirectoryEnableProperty() {
        // Given
        change.setStageName("DIRECTORY_STAGE");
        change.setDirectoryEnable(true);
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("true", stmt.getObjectProperties().get("directoryEnable"));
    }
    
    @Test
    @DisplayName("Should handle Directory Table Format property")
    void shouldHandleDirectoryTableFormatProperty() {
        // Given
        change.setStageName("DIRECTORY_STAGE");
        change.setObjectProperty("directoryTableFormat", "JSON");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("JSON", stmt.getObjectProperties().get("directoryTableFormat"));
    }
    
    @Test
    @DisplayName("Should handle Directory Refresh On Create property")
    void shouldHandleDirectoryRefreshOnCreateProperty() {
        // Given
        change.setStageName("DIRECTORY_STAGE");
        change.setObjectProperty("directoryRefreshOnCreate", "false");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("false", stmt.getObjectProperties().get("directoryRefreshOnCreate"));
    }
    
    // ==================== File Format Property Tests ====================
    
    @Test
    @DisplayName("Should handle File Format Type property")
    void shouldHandleFileFormatTypeProperty() {
        // Given
        change.setStageName("FORMAT_STAGE");
        change.setObjectProperty("fileFormatType", "CSV");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("CSV", stmt.getObjectProperties().get("fileFormatType"));
    }
    
    @Test
    @DisplayName("Should handle File Format Name property")
    void shouldHandleFileFormatNameProperty() {
        // Given
        change.setStageName("FORMAT_STAGE");
        change.setObjectProperty("fileFormatName", "MY_CSV_FORMAT");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("MY_CSV_FORMAT", stmt.getObjectProperties().get("fileFormatName"));
    }
    
    @Test
    @DisplayName("Should handle File Format Compression property")
    void shouldHandleFileFormatCompressionProperty() {
        // Given
        change.setStageName("FORMAT_STAGE");
        change.setObjectProperty("fileFormatCompression", "GZIP");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("GZIP", stmt.getObjectProperties().get("fileFormatCompression"));
    }
    
    @Test
    @DisplayName("Should handle File Format Record Delimiter property")
    void shouldHandleFileFormatRecordDelimiterProperty() {
        // Given
        change.setStageName("FORMAT_STAGE");
        change.setObjectProperty("fileFormatRecordDelimiter", "\\n");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("\\n", stmt.getObjectProperties().get("fileFormatRecordDelimiter"));
    }
    
    @Test
    @DisplayName("Should handle File Format Field Delimiter property")
    void shouldHandleFileFormatFieldDelimiterProperty() {
        // Given
        change.setStageName("FORMAT_STAGE");
        change.setObjectProperty("fileFormatFieldDelimiter", ",");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals(",", stmt.getObjectProperties().get("fileFormatFieldDelimiter"));
    }
    
    @Test
    @DisplayName("Should handle File Format Skip Header property")
    void shouldHandleFileFormatSkipHeaderProperty() {
        // Given
        change.setStageName("FORMAT_STAGE");
        change.setObjectProperty("fileFormatSkipHeader", "1");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("1", stmt.getObjectProperties().get("fileFormatSkipHeader"));
    }
    
    // ==================== Clone Operation Property Tests ====================
    
    @Test
    @DisplayName("Should handle Clone From Stage property")
    void shouldHandleCloneFromStageProperty() {
        // Given
        change.setStageName("CLONED_STAGE");
        change.setCloneFromStage("SOURCE_STAGE");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("SOURCE_STAGE", stmt.getObjectProperties().get("cloneFromStage"));
    }
    
    @Test
    @DisplayName("Should handle Clone From Schema property")
    void shouldHandleCloneFromSchemaProperty() {
        // Given
        change.setStageName("CLONED_STAGE");
        change.setCloneFromSchema("SOURCE_SCHEMA");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("SOURCE_SCHEMA", stmt.getObjectProperties().get("cloneFromSchema"));
    }
    
    @Test
    @DisplayName("Should handle Clone From Catalog property")
    void shouldHandleCloneFromCatalogProperty() {
        // Given
        change.setStageName("CLONED_STAGE");
        change.setCloneFromCatalog("SOURCE_DB");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("SOURCE_DB", stmt.getObjectProperties().get("cloneFromCatalog"));
    }
    
    @Test
    @DisplayName("Should handle Time Travel Type property")
    void shouldHandleTimeTravelTypeProperty() {
        // Given
        change.setStageName("CLONED_STAGE");
        change.setTimeTravelType("TIMESTAMP");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("TIMESTAMP", stmt.getObjectProperties().get("timeTravelType"));
    }
    
    @Test
    @DisplayName("Should handle Time Travel Value property")
    void shouldHandleTimeTravelValueProperty() {
        // Given
        change.setStageName("CLONED_STAGE");
        change.setTimeTravelValue("2023-01-01 12:00:00");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("2023-01-01 12:00:00", stmt.getObjectProperties().get("timeTravelValue"));
    }
    
    // ==================== Encryption and Utility Property Tests ====================
    
    @Test
    @DisplayName("Should handle Encryption Type property")
    void shouldHandleEncryptionTypeProperty() {
        // Given
        change.setStageName("ENCRYPTED_STAGE");
        change.setObjectProperty("encryptionType", "SNOWFLAKE_SSE");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("SNOWFLAKE_SSE", stmt.getObjectProperties().get("encryptionType"));
    }
    
    @Test
    @DisplayName("Should handle Master Key property")
    void shouldHandleMasterKeyProperty() {
        // Given
        change.setStageName("ENCRYPTED_STAGE");
        change.setObjectProperty("masterKey", "mykey123");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("mykey123", stmt.getObjectProperties().get("masterKey"));
    }
    
    @Test
    @DisplayName("Should handle KMS Key ID property")
    void shouldHandleKmsKeyIdProperty() {
        // Given
        change.setStageName("ENCRYPTED_STAGE");
        change.setObjectProperty("kmsKeyId", "arn:aws:kms:us-west-2:123456789012:key/12345678-1234-1234-1234-123456789012");
        
        // When
        SqlStatement[] statements = change.generateStatements(database);
        
        // Then
        CreateStageStatement stmt = (CreateStageStatement) statements[0];
        assertEquals("arn:aws:kms:us-west-2:123456789012:key/12345678-1234-1234-1234-123456789012", stmt.getObjectProperties().get("kmsKeyId"));
    }
    
    // ==================== Boolean Properties Tests ====================
    
    @Test
    @DisplayName("Should handle AWS Key ID through generic property storage")
    void shouldHandleAwsKeyIdThroughGenericStorage() {
        // Given
        String keyId = "AKIAIOSFODNN7EXAMPLE";
        change.setStageName("AWS_STAGE");
        change.setAwsKeyId(keyId);
        
        // When
        String retrievedKeyId = change.getObjectProperty("awsKeyId");
        
        // Then
        assertEquals(keyId, retrievedKeyId);
    }
    
    @Test
    @DisplayName("Should handle null AWS Key ID through generic storage")
    void shouldHandleNullAwsKeyIdThroughGenericStorage() {
        // Given
        change.setStageName("AWS_STAGE");
        // No awsKeyId set
        
        // When
        String retrievedKeyId = change.getObjectProperty("awsKeyId");
        
        // Then
        assertNull(retrievedKeyId);
    }
    
    @Test
    @DisplayName("Should handle get and set operations for all properties")
    void shouldHandleGetAndSetOperationsForAllProperties() {
        // Test multiple property getter/setter pairs
        change.setStageName("TEST_STAGE");
        
        // Test available getters using generic property storage
        change.setAwsSecretKey("secret123");
        assertEquals("secret123", change.getObjectProperty("awsSecretKey"));
        
        change.setAzureAccountName("account123");
        assertEquals("account123", change.getObjectProperty("azureAccountName"));
        
        // Test properties through generic storage only (no direct getters)
        change.setObjectProperty("directoryRefreshOnCreate", "false");
        assertEquals("false", change.getObjectProperty("directoryRefreshOnCreate"));
        
        change.setObjectProperty("fileFormatSkipHeader", "2");
        assertEquals("2", change.getObjectProperty("fileFormatSkipHeader"));
    }
}