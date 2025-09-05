package liquibase.sqlgenerator.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.CreateStageStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests for CreateStageGeneratorSnowflake SQL generation.
 * Tests complete SQL string output without database dependencies - NO MOCKING!
 * 
 * Follows the proven pattern:
 * - Input: Statement objects with properties
 * - Output: Expected SQL strings
 * - Test: assertEquals(expectedSQL, actualSQL)
 */
@DisplayName("CreateStageGeneratorSnowflake - Pure SQL Tests")
public class CreateStageGeneratorSnowflakeTest {
    
    private CreateStageGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        generator = new CreateStageGeneratorSnowflake();
        database = new SnowflakeDatabase(); // Real database object, no mocking needed
    }
    
    @Test
    @DisplayName("Should generate basic CREATE STAGE SQL")
    void testBasicCreateStage() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("TEST_STAGE");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE STAGE TEST_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CREATE OR REPLACE STAGE SQL")
    void testCreateOrReplaceStage() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("TEST_STAGE");
        statement.setObjectProperty("orReplace", "true");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE OR REPLACE STAGE TEST_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CREATE TEMPORARY STAGE SQL")
    void testCreateTemporaryStage() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("TEMP_STAGE");
        statement.setObjectProperty("temporary", "true");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE TEMPORARY STAGE TEMP_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CREATE STAGE IF NOT EXISTS SQL")
    void testCreateStageIfNotExists() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("TEST_STAGE");
        statement.setObjectProperty("ifNotExists", "true");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE STAGE IF NOT EXISTS TEST_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CREATE OR REPLACE TEMPORARY STAGE SQL")
    void testCreateOrReplaceTemporaryStage() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("TEMP_STAGE");
        statement.setObjectProperty("orReplace", "true");
        statement.setObjectProperty("temporary", "true");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        assertEquals("CREATE OR REPLACE TEMPORARY STAGE TEMP_STAGE", sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate external stage with URL and storage integration")
    void testExternalStageWithUrlAndStorageIntegration() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("EXTERNAL_STAGE");
        statement.setObjectProperty("url", "s3://mybucket/path/");
        statement.setObjectProperty("storageIntegration", "MY_S3_INTEGRATION");
        statement.setObjectProperty("comment", "External stage for S3 data");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE EXTERNAL_STAGE URL = 's3://mybucket/path/' " +
                           "STORAGE_INTEGRATION = MY_S3_INTEGRATION " +
                           "COMMENT = 'External stage for S3 data'";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate stage with AWS credentials")
    void testStageWithAwsCredentials() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("AWS_STAGE");
        statement.setObjectProperty("url", "s3://mybucket/data/");
        statement.setObjectProperty("awsKeyId", "AKIAIOSFODNN7EXAMPLE");
        statement.setObjectProperty("awsSecretKey", "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY");
        statement.setObjectProperty("awsToken", "AQoDYXdzEJr");
        statement.setObjectProperty("awsRole", "arn:aws:iam::123456789012:role/MyRole");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE AWS_STAGE URL = 's3://mybucket/data/' " +
                           "CREDENTIALS = (AWS_KEY_ID = 'AKIAIOSFODNN7EXAMPLE' " +
                           "AWS_SECRET_KEY = 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY' " +
                           "AWS_TOKEN = 'AQoDYXdzEJr' AWS_ROLE = 'arn:aws:iam::123456789012:role/MyRole')";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate stage with GCS credentials")
    void testStageWithGcsCredentials() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("GCS_STAGE");
        statement.setObjectProperty("url", "gcs://mybucket/path/");
        statement.setObjectProperty("gcsServiceAccountKey", "base64encodedkey");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE GCS_STAGE URL = 'gcs://mybucket/path/' " +
                           "CREDENTIALS = (GCS_SERVICE_ACCOUNT_KEY = 'base64encodedkey')";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate stage with Azure credentials using account key")
    void testStageWithAzureAccountKeyCredentials() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("AZURE_STAGE");
        statement.setObjectProperty("url", "azure://myaccount.blob.core.windows.net/mycontainer/path/");
        statement.setObjectProperty("azureAccountName", "myaccount");
        statement.setObjectProperty("azureAccountKey", "base64encodedaccountkey");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE AZURE_STAGE URL = 'azure://myaccount.blob.core.windows.net/mycontainer/path/' " +
                           "CREDENTIALS = (AZURE_ACCOUNT_NAME = 'myaccount' AZURE_ACCOUNT_KEY = 'base64encodedaccountkey')";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate stage with Azure SAS token credentials")
    void testStageWithAzureSasTokenCredentials() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("AZURE_SAS_STAGE");
        statement.setObjectProperty("url", "azure://myaccount.blob.core.windows.net/mycontainer/");
        statement.setObjectProperty("azureAccountName", "myaccount");
        statement.setObjectProperty("azureSasToken", "sp=r&st=2023-01-01T00:00:00Z&se=2024-01-01T00:00:00Z&spr=https&sv=2020-08-04&sr=c&sig=signature");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE AZURE_SAS_STAGE URL = 'azure://myaccount.blob.core.windows.net/mycontainer/' " +
                           "CREDENTIALS = (AZURE_ACCOUNT_NAME = 'myaccount' " +
                           "AZURE_SAS_TOKEN = 'sp=r&st=2023-01-01T00:00:00Z&se=2024-01-01T00:00:00Z&spr=https&sv=2020-08-04&sr=c&sig=signature')";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate stage with encryption configuration")
    void testStageWithEncryption() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("ENCRYPTED_STAGE");
        statement.setObjectProperty("url", "s3://mybucket/encrypted/");
        statement.setObjectProperty("storageIntegration", "MY_INTEGRATION");
        statement.setObjectProperty("encryption", "AWS_SSE_S3");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE ENCRYPTED_STAGE URL = 's3://mybucket/encrypted/' " +
                           "STORAGE_INTEGRATION = MY_INTEGRATION ENCRYPTION = (TYPE = 'AWS_SSE_S3')";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate stage with encryption and KMS key")
    void testStageWithEncryptionAndKmsKey() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("KMS_STAGE");
        statement.setObjectProperty("url", "s3://mybucket/kms/");
        statement.setObjectProperty("storageIntegration", "MY_INTEGRATION");
        statement.setObjectProperty("encryption", "AWS_SSE_KMS");
        statement.setObjectProperty("kmsKeyId", "arn:aws:kms:us-east-1:123456789012:key/12345678-1234-1234-1234-123456789012");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE KMS_STAGE URL = 's3://mybucket/kms/' " +
                           "STORAGE_INTEGRATION = MY_INTEGRATION " +
                           "ENCRYPTION = (TYPE = 'AWS_SSE_KMS' KMS_KEY_ID = 'arn:aws:kms:us-east-1:123456789012:key/12345678-1234-1234-1234-123456789012')";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate stage with file format reference")
    void testStageWithFileFormatReference() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("FILE_FORMAT_STAGE");
        statement.setObjectProperty("url", "s3://mybucket/data/");
        statement.setObjectProperty("storageIntegration", "MY_INTEGRATION");
        statement.setObjectProperty("fileFormat", "MY_CSV_FORMAT");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE FILE_FORMAT_STAGE URL = 's3://mybucket/data/' " +
                           "STORAGE_INTEGRATION = MY_INTEGRATION FILE_FORMAT = (FORMAT_NAME = 'MY_CSV_FORMAT')";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate stage with inline file format")
    void testStageWithInlineFileFormat() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("INLINE_FORMAT_STAGE");
        statement.setObjectProperty("url", "s3://mybucket/csv/");
        statement.setObjectProperty("storageIntegration", "MY_INTEGRATION");
        statement.setObjectProperty("fileFormatType", "CSV");
        statement.setObjectProperty("compression", "GZIP");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE INLINE_FORMAT_STAGE URL = 's3://mybucket/csv/' " +
                           "STORAGE_INTEGRATION = MY_INTEGRATION " +
                           "FILE_FORMAT = (TYPE = 'CSV' COMPRESSION = 'GZIP')";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate stage with directory table enabled")
    void testStageWithDirectoryTable() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("DIRECTORY_STAGE");
        statement.setObjectProperty("url", "s3://mybucket/structured/");
        statement.setObjectProperty("storageIntegration", "MY_INTEGRATION");
        statement.setObjectProperty("directoryEnable", "true");
        statement.setObjectProperty("autoRefresh", "true");
        statement.setObjectProperty("refreshOnCreate", "false");
        statement.setObjectProperty("notificationIntegration", "MY_SNS_INTEGRATION");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE DIRECTORY_STAGE URL = 's3://mybucket/structured/' " +
                           "STORAGE_INTEGRATION = MY_INTEGRATION " +
                           "DIRECTORY = (ENABLE = TRUE AUTO_REFRESH = TRUE REFRESH_ON_CREATE = FALSE " +
                           "NOTIFICATION_INTEGRATION = 'MY_SNS_INTEGRATION')";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate stage with tag")
    void testStageWithTag() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("TAGGED_STAGE");
        statement.setObjectProperty("url", "s3://mybucket/tagged/");
        statement.setObjectProperty("storageIntegration", "MY_INTEGRATION");
        statement.setObjectProperty("tagName", "ENVIRONMENT");
        statement.setObjectProperty("tagValue", "PRODUCTION");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE TAGGED_STAGE URL = 's3://mybucket/tagged/' " +
                           "STORAGE_INTEGRATION = MY_INTEGRATION TAG (ENVIRONMENT = 'PRODUCTION')";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CLONE stage SQL")
    void testCloneStage() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("CLONED_STAGE");
        statement.setObjectProperty("cloneFromStage", "SOURCE_STAGE");
        statement.setObjectProperty("cloneFromSchema", "SOURCE_SCHEMA");
        statement.setObjectProperty("cloneFromCatalog", "SOURCE_DB");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE CLONED_STAGE CLONE SOURCE_DB.SOURCE_SCHEMA.SOURCE_STAGE";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate CLONE stage with time travel SQL")
    void testCloneStageWithTimeTravel() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("TIME_TRAVEL_STAGE");
        statement.setObjectProperty("cloneFromStage", "SOURCE_STAGE");
        statement.setObjectProperty("timeTravelType", "TIMESTAMP");
        statement.setObjectProperty("timeTravelValue", "2023-12-25 10:00:00");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE TIME_TRAVEL_STAGE CLONE SOURCE_STAGE TIMESTAMP (TIMESTAMP => '2023-12-25 10:00:00')";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    @Test
    @DisplayName("Should generate internal stage SQL")
    void testInternalStage() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("INTERNAL_STAGE");
        statement.setObjectProperty("comment", "Internal stage for temp files");
        
        // When
        Sql[] sqls = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqls.length);
        String expectedSQL = "CREATE STAGE INTERNAL_STAGE COMMENT = 'Internal stage for temp files'";
        assertEquals(expectedSQL, sqls[0].toSql());
    }
    
    // ==================== Validation Tests ====================
    
    @Test
    @DisplayName("Should validate that stageName is required")
    void testValidationRequiresStageName() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        // stageName not set
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("stageName is required")));
    }
    
    @Test
    @DisplayName("Should pass validation with valid statement")
    void testValidationPassesWithValidStatement() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName("VALID_STAGE");
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should support Snowflake database")
    void testSupportsSnowflakeDatabase() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        
        // When/Then
        assertTrue(generator.supports(statement, database));
    }
    
    @Test
    @DisplayName("Should not support non-Snowflake database")
    void testDoesNotSupportNonSnowflakeDatabase() {
        // Given
        CreateStageStatement statement = new CreateStageStatement();
        liquibase.database.Database h2Database = org.mockito.Mockito.mock(liquibase.database.Database.class);
        
        // When/Then
        assertFalse(generator.supports(statement, h2Database));
    }
}