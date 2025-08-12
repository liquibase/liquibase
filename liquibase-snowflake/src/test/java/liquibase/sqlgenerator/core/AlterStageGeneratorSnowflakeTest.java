package liquibase.sqlgenerator.core;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.core.AlterStageStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for AlterStageGeneratorSnowflake with 90%+ coverage focus.
 * Tests SQL generation for ALTER STAGE operations including SET, UNSET, RENAME, and REFRESH.
 */
@DisplayName("AlterStageGeneratorSnowflake")
public class AlterStageGeneratorSnowflakeTest {
    
    private AlterStageGeneratorSnowflake generator;
    private SnowflakeDatabase database;
    
    @BeforeEach
    void setUp() {
        generator = new AlterStageGeneratorSnowflake();
        database = new SnowflakeDatabase();
    }
    
    // ==================== Support Tests ====================
    
    @Test
    @DisplayName("Should support Snowflake database")
    void shouldSupportSnowflakeDatabase() {
        AlterStageStatement statement = new AlterStageStatement();
        assertTrue(generator.supports(statement, database));
    }
    
    @Test
    @DisplayName("Should not support non-Snowflake database")
    void shouldNotSupportNonSnowflakeDatabase() {
        AlterStageStatement statement = new AlterStageStatement();
        liquibase.database.Database h2Database = org.mockito.Mockito.mock(liquibase.database.Database.class);
        assertFalse(generator.supports(statement, h2Database));
    }
    
    // ==================== Validation Tests ====================
    
    @Test
    @DisplayName("Should pass validation with valid stageName")
    void shouldPassValidationWithValidStageName() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("VALID_STAGE");
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertFalse(errors.hasErrors());
    }
    
    @Test
    @DisplayName("Should require stageName")
    void shouldRequireStageName() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        // stageName not set
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("stageName is required for ALTER STAGE")));
    }
    
    @Test
    @DisplayName("Should reject null stageName")
    void shouldRejectNullStageName() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName(null);
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("stageName is required for ALTER STAGE")));
    }
    
    @Test
    @DisplayName("Should reject empty stageName")
    void shouldRejectEmptyStageName() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("");
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("stageName is required for ALTER STAGE")));
    }
    
    @Test
    @DisplayName("Should reject whitespace-only stageName")
    void shouldRejectWhitespaceOnlyStageName() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("   ");
        
        // When
        ValidationErrors errors = generator.validate(statement, database, null);
        
        // Then
        assertTrue(errors.hasErrors());
        assertTrue(errors.getErrorMessages().stream()
            .anyMatch(msg -> msg.contains("stageName is required for ALTER STAGE")));
    }
    
    // ==================== Basic SQL Generation Tests ====================
    
    @Test
    @DisplayName("Should generate basic ALTER STAGE with SET operation")
    void shouldGenerateBasicAlterStageWithSetOperation() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("MY_STAGE");
        statement.setObjectProperty("url", "s3://my-bucket/path/");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqlArray.length);
        String sql = sqlArray[0].toSql();
        
        // Should contain basic ALTER STAGE structure
        assertTrue(sql.contains("ALTER STAGE MY_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("URL = 's3://my-bucket/path/'"));
    }
    
    @Test
    @DisplayName("Should generate ALTER STAGE with IF EXISTS")
    void shouldGenerateAlterStageWithIfExists() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("CONDITIONAL_STAGE");
        statement.setObjectProperty("ifExists", "true");
        statement.setObjectProperty("comment", "Updated comment");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqlArray.length);
        String sql = sqlArray[0].toSql();
        
        assertTrue(sql.contains("ALTER STAGE IF EXISTS CONDITIONAL_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("COMMENT = 'Updated comment'"));
    }
    
    @Test
    @DisplayName("Should generate ALTER STAGE with schema qualification")
    void shouldGenerateAlterStageWithSchemaQualification() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("QUALIFIED_STAGE");
        statement.setObjectProperty("schemaName", "MY_SCHEMA");
        statement.setObjectProperty("catalogName", "MY_DATABASE");
        statement.setObjectProperty("storageIntegration", "MY_INTEGRATION");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqlArray.length);
        String sql = sqlArray[0].toSql();
        
        assertTrue(sql.contains("MY_DATABASE.MY_SCHEMA.QUALIFIED_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("STORAGE_INTEGRATION = MY_INTEGRATION"));
    }
    
    // ==================== SET Operations Tests ====================
    
    @Test
    @DisplayName("Should generate SET operations for URL")
    void shouldGenerateSetOperationsForUrl() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("URL_STAGE");
        statement.setObjectProperty("url", "s3://new-bucket/data/");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE URL_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("URL = 's3://new-bucket/data/'"));
    }
    
    @Test
    @DisplayName("Should generate SET operations for storage integration")
    void shouldGenerateSetOperationsForStorageIntegration() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("INTEGRATION_STAGE");
        statement.setObjectProperty("storageIntegration", "NEW_INTEGRATION");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE INTEGRATION_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("STORAGE_INTEGRATION = NEW_INTEGRATION"));
    }
    
    @Test
    @DisplayName("Should generate SET operations for encryption")
    void shouldGenerateSetOperationsForEncryption() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("ENCRYPTED_STAGE");
        statement.setObjectProperty("encryption", "AES256");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE ENCRYPTED_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("ENCRYPTION = (TYPE = 'AES256')"));
    }
    
    @Test
    @DisplayName("Should generate SET operations for AWS credentials")
    void shouldGenerateSetOperationsForAwsCredentials() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("AWS_STAGE");
        statement.setObjectProperty("awsKeyId", "AKIATEST123");
        statement.setObjectProperty("awsSecretKey", "secretkey123");
        statement.setObjectProperty("awsToken", "sessiontoken123");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE AWS_STAGE"));
        assertTrue(sql.contains("SET"));
        // Individual credential properties (not wrapped in CREDENTIALS)
        assertTrue(sql.contains("AWS_KEY_ID = 'AKIATEST123'"));
        assertTrue(sql.contains("AWS_SECRET_KEY = 'secretkey123'"));
        assertTrue(sql.contains("AWS_TOKEN = 'sessiontoken123'"));
    }
    
    @Test
    @DisplayName("Should generate SET operations for file format")
    void shouldGenerateSetOperationsForFileFormat() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("FORMAT_STAGE");
        statement.setObjectProperty("fileFormat", "CSV");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE FORMAT_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("FILE_FORMAT = (TYPE = CSV)"));
    }
    
    @Test
    @DisplayName("Should generate SET operations for comment")
    void shouldGenerateSetOperationsForComment() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("COMMENTED_STAGE");
        statement.setObjectProperty("comment", "Updated stage for production data");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE COMMENTED_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("COMMENT = 'Updated stage for production data'"));
    }
    
    // ==================== UNSET Operations Tests ====================
    
    @Test
    @DisplayName("Should generate UNSET operations")
    void shouldGenerateUnsetOperations() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("UNSET_STAGE");
        statement.setObjectProperty("unsetUrl", "true");
        statement.setObjectProperty("unsetCredentials", "true");
        statement.setObjectProperty("unsetEncryption", "true");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE UNSET_STAGE"));
        assertTrue(sql.contains("UNSET"));
        assertTrue(sql.contains("URL"));
        assertTrue(sql.contains("CREDENTIALS"));
        assertTrue(sql.contains("ENCRYPTION"));
    }
    
    @Test
    @DisplayName("Should generate UNSET for specific properties")
    void shouldGenerateUnsetForSpecificProperties() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("SPECIFIC_UNSET_STAGE");
        statement.setObjectProperty("unsetStorageIntegration", "true");
        statement.setObjectProperty("unsetComment", "true");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE SPECIFIC_UNSET_STAGE"));
        assertTrue(sql.contains("UNSET"));
        assertTrue(sql.contains("STORAGE_INTEGRATION"));
        assertTrue(sql.contains("COMMENT"));
    }
    
    // ==================== RENAME Operation Tests ====================
    
    @Test
    @DisplayName("Should generate RENAME operation")
    void shouldGenerateRenameOperation() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("OLD_STAGE");
        statement.setObjectProperty("renameTo", "NEW_STAGE");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE OLD_STAGE"));
        assertTrue(sql.contains("RENAME TO NEW_STAGE"));
    }
    
    // ==================== REFRESH Operations Tests ====================
    
    @Test
    @DisplayName("Should generate REFRESH directory operation")
    void shouldGenerateRefreshDirectoryOperation() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("REFRESH_STAGE");
        statement.setObjectProperty("refreshDirectory", "true");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE REFRESH_STAGE"));
        assertTrue(sql.contains("REFRESH"));
    }
    
    @Test
    @DisplayName("Should generate REFRESH with subpath")
    void shouldGenerateRefreshWithSubpath() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("SUBPATH_STAGE");
        statement.setObjectProperty("refreshDirectory", "true");
        statement.setObjectProperty("refreshSubpath", "/data/2023/");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE SUBPATH_STAGE"));
        assertTrue(sql.contains("REFRESH"));
        assertTrue(sql.contains("'/data/2023/'"));
    }
    
    // ==================== Combined Operations Tests ====================
    
    @Test
    @DisplayName("Should generate multiple SET operations")
    void shouldGenerateMultipleSetOperations() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("MULTI_SET_STAGE");
        statement.setObjectProperty("url", "s3://updated-bucket/");
        statement.setObjectProperty("storageIntegration", "UPDATED_INTEGRATION");
        statement.setObjectProperty("comment", "Multiple updates");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE MULTI_SET_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("URL = 's3://updated-bucket/'"));
        assertTrue(sql.contains("STORAGE_INTEGRATION = UPDATED_INTEGRATION"));
        assertTrue(sql.contains("COMMENT = 'Multiple updates'"));
    }
    
    @Test
    @DisplayName("Should generate SET and UNSET operations together")
    void shouldGenerateSetAndUnsetOperationsTogether() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("SET_UNSET_STAGE");
        // SET operations
        statement.setObjectProperty("url", "s3://new-bucket/");
        statement.setObjectProperty("comment", "Updated comment");
        // UNSET operations
        statement.setObjectProperty("unsetCredentials", "true");
        statement.setObjectProperty("unsetEncryption", "true");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE SET_UNSET_STAGE"));
        
        // When both SET and UNSET operations are present, UNSET takes priority
        assertTrue(sql.contains("UNSET"));
        assertTrue(sql.contains("CREDENTIALS"));
        assertTrue(sql.contains("ENCRYPTION"));
        
        // SET operations are not executed when UNSET operations are present
        assertFalse(sql.contains(" SET "));  // More specific - not part of UNSET
        assertFalse(sql.contains("URL ="));
        assertFalse(sql.contains("COMMENT ="));
    }
    
    // ==================== SET Operations - Cloud Credentials Coverage ====================
    
    @Test
    @DisplayName("Should generate SET operations for GCS credentials")
    void shouldGenerateSetOperationsForGcsCredentials() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("GCS_STAGE");
        statement.setObjectProperty("gcsServiceAccount", "service-account@project.iam.gserviceaccount.com");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE GCS_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("GCS_SERVICE_ACCOUNT = 'service-account@project.iam.gserviceaccount.com'"));
    }
    
    @Test
    @DisplayName("Should generate SET operations for Azure credentials")
    void shouldGenerateSetOperationsForAzureCredentials() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("AZURE_STAGE");
        statement.setObjectProperty("azureSasToken", "?sp=racwdl&st=2023-01-01");
        statement.setObjectProperty("azureTenantId", "tenant-123");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE AZURE_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("AZURE_SAS_TOKEN = '?sp=racwdl&st=2023-01-01'"));
        assertTrue(sql.contains("AZURE_TENANT_ID = 'tenant-123'"));
    }
    
    @Test
    @DisplayName("Should generate SET operations for directory enable")
    void shouldGenerateSetOperationsForDirectoryEnable() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("DIR_STAGE");
        statement.setObjectProperty("directoryEnable", "true");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE DIR_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("DIRECTORY = (ENABLE = TRUE)"));
    }
    
    @Test
    @DisplayName("Should generate SET operations for directory disable")
    void shouldGenerateSetOperationsForDirectoryDisable() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("DIR_DISABLE_STAGE");
        statement.setObjectProperty("directoryEnable", "false");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE DIR_DISABLE_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("DIRECTORY = (ENABLE = FALSE)"));
    }
    
    @Test
    @DisplayName("Should generate SET operations for TAG operations")
    void shouldGenerateSetOperationsForTagOperations() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("TAG_STAGE");
        statement.setObjectProperty("setTagName", "DEPARTMENT");
        statement.setObjectProperty("setTagValue", "ENGINEERING");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE TAG_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("TAG (DEPARTMENT = 'ENGINEERING')"));
    }
    
    @Test
    @DisplayName("Should generate SET operations with mixed properties and TAG")
    void shouldGenerateSetOperationsWithMixedPropertiesAndTag() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("MIXED_TAG_STAGE");
        statement.setObjectProperty("comment", "Stage with tag");
        statement.setObjectProperty("setTagName", "ENVIRONMENT");
        statement.setObjectProperty("setTagValue", "PRODUCTION");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE MIXED_TAG_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("COMMENT = 'Stage with tag'"));
        assertTrue(sql.contains("TAG (ENVIRONMENT = 'PRODUCTION')"));
    }
    
    @Test
    @DisplayName("Should generate SET operations for generic camelCase properties")
    void shouldGenerateSetOperationsForGenericCamelCaseProperties() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("GENERIC_STAGE");
        statement.setObjectProperty("customProperty", "customValue");
        statement.setObjectProperty("anotherCamelCase", "anotherValue");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE GENERIC_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("CUSTOM_PROPERTY = 'customValue'"));
        assertTrue(sql.contains("ANOTHER_CAMEL_CASE = 'anotherValue'"));
    }
    
    // ==================== UNSET Operations - Complete Coverage ====================
    
    @Test
    @DisplayName("Should generate UNSET for file format")
    void shouldGenerateUnsetForFileFormat() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("UNSET_FORMAT_STAGE");
        statement.setObjectProperty("unsetFileFormat", "true");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE UNSET_FORMAT_STAGE"));
        assertTrue(sql.contains("UNSET"));
        assertTrue(sql.contains("FILE_FORMAT"));
    }
    
    @Test
    @DisplayName("Should generate UNSET for tag with tag name")
    void shouldGenerateUnsetForTagWithTagName() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("UNSET_TAG_STAGE");
        statement.setObjectProperty("unsetTagName", "DEPARTMENT");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE UNSET_TAG_STAGE"));
        assertTrue(sql.contains("UNSET"));
        assertTrue(sql.contains("TAG (DEPARTMENT)"));
    }
    
    // ==================== convertCamelCaseToSnakeCase Method Coverage ====================
    
    @Test
    @DisplayName("Should convert camelCase to SNAKE_CASE for AWS credentials")
    void shouldConvertCamelCaseToSnakeCaseForAwsCredentials() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("CAMEL_CASE_STAGE");
        statement.setObjectProperty("awsAccessKeyId", "AKIATEST123");
        statement.setObjectProperty("awsSecretAccessKey", "secret123");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE CAMEL_CASE_STAGE"));
        assertTrue(sql.contains("SET"));
        // Should convert awsAccessKeyId -> AWS_ACCESS_KEY_ID
        assertTrue(sql.contains("AWS_ACCESS_KEY_ID = 'AKIATEST123'"));
        // Should convert awsSecretAccessKey -> AWS_SECRET_ACCESS_KEY
        assertTrue(sql.contains("AWS_SECRET_ACCESS_KEY = 'secret123'"));
    }
    
    @Test
    @DisplayName("Should convert camelCase to SNAKE_CASE for complex property names")
    void shouldConvertCamelCaseToSnakeCaseForComplexPropertyNames() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("COMPLEX_STAGE");
        statement.setObjectProperty("veryLongPropertyNameHere", "testValue");
        statement.setObjectProperty("anotherComplexCamelCase", "anotherValue");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE COMPLEX_STAGE"));
        assertTrue(sql.contains("SET"));
        // Should convert veryLongPropertyNameHere -> VERY_LONG_PROPERTY_NAME_HERE
        assertTrue(sql.contains("VERY_LONG_PROPERTY_NAME_HERE = 'testValue'"));
        // Should convert anotherComplexCamelCase -> ANOTHER_COMPLEX_CAMEL_CASE
        assertTrue(sql.contains("ANOTHER_COMPLEX_CAMEL_CASE = 'anotherValue'"));
    }
    
    // ==================== Edge Cases and Error Handling ====================
    
    @Test
    @DisplayName("Should handle empty properties gracefully")
    void shouldHandleEmptyPropertiesGracefully() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("EMPTY_PROPS_STAGE");
        // No properties set - should generate basic ALTER STAGE
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqlArray.length);
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE EMPTY_PROPS_STAGE"));
    }
    
    @Test
    @DisplayName("Should handle special characters in property values")
    void shouldHandleSpecialCharactersInPropertyValues() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("SPECIAL_STAGE");
        statement.setObjectProperty("comment", "Stage with 'quotes' and \"double quotes\"");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE SPECIAL_STAGE"));
        assertTrue(sql.contains("SET"));
        // Should escape quotes properly
        assertTrue(sql.contains("COMMENT = 'Stage with ''quotes'' and \"double quotes\"'"));
    }
    
    @Test
    @DisplayName("Should handle properties to skip correctly in SET operations")
    void shouldHandlePropertiesToSkipCorrectlyInSetOperations() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("SKIP_PROPS_STAGE");
        // Add properties that should be skipped in SET operations
        // NOTE: Remove refreshDirectory to avoid REFRESH path, remove unsetUrl/renameTo to avoid UNSET/RENAME paths
        statement.setObjectProperty("refreshSubpath", "/path");   // Should be skipped
        statement.setObjectProperty("catalogName", "DB");         // Should be skipped (used for schema qualification)
        statement.setObjectProperty("schemaName", "SCHEMA");      // Should be skipped (used for schema qualification)
        statement.setObjectProperty("ifExists", "true");          // Should be skipped (used for IF EXISTS)
        statement.setObjectProperty("tagName", "TAG");            // Should be skipped
        statement.setObjectProperty("tagValue", "VALUE");         // Should be skipped
        statement.setObjectProperty("comment", "Valid comment");  // Should NOT be skipped
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        // Schema qualification and IF EXISTS should be applied
        assertTrue(sql.contains("ALTER STAGE IF EXISTS DB.\"SCHEMA\".SKIP_PROPS_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("COMMENT = 'Valid comment'"));
        
        // Verify skipped properties are not in SET clause (but are used for other purposes)
        assertFalse(sql.contains("REFRESH_SUBPATH"));    // Not in SET clause
        assertFalse(sql.contains("CATALOG_NAME"));       // Not in SET clause
        assertFalse(sql.contains("SCHEMA_NAME"));        // Not in SET clause  
        assertFalse(sql.contains("IF_EXISTS"));          // Not in SET clause
        assertFalse(sql.contains("TAG_NAME"));           // Not in SET clause
        assertFalse(sql.contains("TAG_VALUE"));          // Not in SET clause
    }
    
    @Test
    @DisplayName("Should handle single property in SET operations")
    void shouldHandleSinglePropertyInSetOperations() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("SINGLE_PROP_STAGE");
        statement.setObjectProperty("url", "s3://single-bucket/");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE SINGLE_PROP_STAGE"));
        assertTrue(sql.contains("SET"));
        assertTrue(sql.contains("URL = 's3://single-bucket/'"));
        // Should not have unnecessary commas
        assertFalse(sql.contains(", URL"));
        assertFalse(sql.contains("URL, "));
    }
    
    @Test
    @DisplayName("Should handle multiple UNSET operations with comma separation")
    void shouldHandleMultipleUnsetOperationsWithCommaSeparation() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("MULTI_UNSET_STAGE");
        statement.setObjectProperty("unsetUrl", "true");
        statement.setObjectProperty("unsetCredentials", "true");
        statement.setObjectProperty("unsetEncryption", "true");
        statement.setObjectProperty("unsetComment", "true");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE MULTI_UNSET_STAGE"));
        assertTrue(sql.contains("UNSET"));
        assertTrue(sql.contains("URL"));
        assertTrue(sql.contains("CREDENTIALS"));
        assertTrue(sql.contains("ENCRYPTION"));
        assertTrue(sql.contains("COMMENT"));
        // Should have proper comma separation
        int commaCount = sql.length() - sql.replace(",", "").length();
        assertTrue(commaCount >= 3, "Should have at least 3 commas for 4 UNSET operations");
    }
    
    @Test
    @DisplayName("Should handle single UNSET operation without comma")
    void shouldHandleSingleUnsetOperationWithoutComma() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("SINGLE_UNSET_STAGE");
        statement.setObjectProperty("unsetUrl", "true");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE SINGLE_UNSET_STAGE"));
        assertTrue(sql.contains("UNSET"));
        assertTrue(sql.contains("URL"));
        // Should not have unnecessary commas
        assertFalse(sql.contains(", URL"));
        assertFalse(sql.contains("URL, "));
    }
    
    @Test
    @DisplayName("Should handle null property values gracefully")
    void shouldHandleNullPropertyValuesGracefully() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("NULL_PROPS_STAGE");
        statement.setObjectProperty("url", null);
        statement.setObjectProperty("comment", "Valid comment");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        String sql = sqlArray[0].toSql();
        assertTrue(sql.contains("ALTER STAGE NULL_PROPS_STAGE"));
        assertTrue(sql.contains("SET"));
        // Should only include non-null properties
        assertTrue(sql.contains("COMMENT = 'Valid comment'"));
        assertFalse(sql.contains("URL =")); // Null URL should be skipped
    }
    
    @Test
    @DisplayName("Should return exactly one SQL statement")
    void shouldReturnExactlyOneSqlStatement() {
        // Given
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName("SINGLE_STATEMENT_STAGE");
        statement.setObjectProperty("comment", "Test comment");
        
        // When
        Sql[] sqlArray = generator.generateSql(statement, database, null);
        
        // Then
        assertEquals(1, sqlArray.length);
        assertNotNull(sqlArray[0]);
        assertNotNull(sqlArray[0].toSql());
        assertFalse(sqlArray[0].toSql().trim().isEmpty());
    }
}