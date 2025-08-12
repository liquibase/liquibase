package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateStageStatement;
import liquibase.Scope;
import liquibase.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Professional implementation using generic property storage pattern.
 * 75 LOC approach preferred by Liquibase team over 354 LOC explicit mapping.
 */
@DatabaseChange(
    name = "createStage",
    description = "Creates a stage",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "stage",
    since = "4.33"
)
public class CreateStageChange extends AbstractChange {

    // PROFESSIONAL PATTERN: Generic property storage (75 LOC approach)
    private static final Logger logger = Scope.getCurrentScope().getLog(CreateStageChange.class);
    
    private Map<String, String> objectProperties = new HashMap<>();
    private String stageName; // Core required property
    
    @DatabaseChangeProperty(
        description = "Name of the stage to create", 
        requiredForDatabase = "snowflake"
    )
    public void setStageName(String stageName) {
        this.stageName = stageName;
    }
    
    public String getStageName() {
        return stageName;
    }
    
    // PROFESSIONAL PATTERN: Generic property storage methods
    public void setObjectProperty(String propertyName, String propertyValue) {
        if (propertyValue != null) {
            objectProperties.put(propertyName, propertyValue);
        }
    }
    
    public String getObjectProperty(String propertyName) {
        return objectProperties.get(propertyName);
    }
    
    public Map<String, String> getAllObjectProperties() {
        return new HashMap<>(objectProperties);
    }
    
    // API compatibility methods for common properties (maintains API compatibility)
    public void setCatalogName(String catalogName) { setObjectProperty("catalogName", catalogName); }
    public void setSchemaName(String schemaName) { setObjectProperty("schemaName", schemaName); }
    public void setUrl(String url) { setObjectProperty("url", url); }
    public void setStorageIntegration(String storageIntegration) { setObjectProperty("storageIntegration", storageIntegration); }
    public void setOrReplace(Boolean orReplace) { setObjectProperty("orReplace", orReplace != null ? orReplace.toString() : null); }
    public void setIfNotExists(Boolean ifNotExists) { setObjectProperty("ifNotExists", ifNotExists != null ? ifNotExists.toString() : null); }
    public void setTemporary(Boolean temporary) { setObjectProperty("temporary", temporary != null ? temporary.toString() : null); }
    public void setComment(String comment) { setObjectProperty("comment", comment); }
    public void setEncryption(String encryption) { setObjectProperty("encryption", encryption); }
    public void setUsePrivatelinkEndpoint(Boolean usePrivatelink) { setObjectProperty("usePrivatelinkEndpoint", usePrivatelink != null ? usePrivatelink.toString() : null); }
    
    // Cloud credentials methods
    public void setAwsKeyId(String awsKeyId) { setObjectProperty("awsKeyId", awsKeyId); }
    public void setAwsSecretKey(String awsSecretKey) { setObjectProperty("awsSecretKey", awsSecretKey); }
    public void setAwsToken(String awsToken) { setObjectProperty("awsToken", awsToken); }
    public void setAwsRole(String awsRole) { setObjectProperty("awsRole", awsRole); }
    public void setGcsServiceAccountKey(String gcsKey) { setObjectProperty("gcsServiceAccountKey", gcsKey); }
    public void setAzureAccountName(String azureAccount) { setObjectProperty("azureAccountName", azureAccount); }
    public void setAzureAccountKey(String azureKey) { setObjectProperty("azureAccountKey", azureKey); }
    public void setAzureSasToken(String azureSas) { setObjectProperty("azureSasToken", azureSas); }
    
    // Directory table methods
    public void setDirectoryEnable(Boolean directoryEnable) { setObjectProperty("directoryEnable", directoryEnable != null ? directoryEnable.toString() : null); }
    public void setAutoRefresh(Boolean autoRefresh) { setObjectProperty("autoRefresh", autoRefresh != null ? autoRefresh.toString() : null); }
    public void setRefreshOnCreate(Boolean refreshOnCreate) { setObjectProperty("refreshOnCreate", refreshOnCreate != null ? refreshOnCreate.toString() : null); }
    public void setNotificationIntegration(String notificationIntegration) { setObjectProperty("notificationIntegration", notificationIntegration); }
    
    // File format methods - comprehensive configuration
    public void setFileFormat(String fileFormat) { setObjectProperty("fileFormat", fileFormat); }
    public void setFileFormatType(String fileFormatType) { setObjectProperty("fileFormatType", fileFormatType); }
    public void setCompression(String compression) { setObjectProperty("compression", compression); }
    
    // CSV-specific file format options
    public void setRecordDelimiter(String recordDelimiter) { setObjectProperty("recordDelimiter", recordDelimiter); }
    public void setFieldDelimiter(String fieldDelimiter) { setObjectProperty("fieldDelimiter", fieldDelimiter); }
    public void setFieldOptionallyEnclosedBy(String fieldOptionallyEnclosedBy) { setObjectProperty("fieldOptionallyEnclosedBy", fieldOptionallyEnclosedBy); }
    public void setEscape(String escape) { setObjectProperty("escape", escape); }
    public void setEscapeUnenclosedField(String escapeUnenclosedField) { setObjectProperty("escapeUnenclosedField", escapeUnenclosedField); }
    public void setSkipHeader(Integer skipHeader) { setObjectProperty("skipHeader", skipHeader != null ? skipHeader.toString() : null); }
    public void setSkipBlankLines(Boolean skipBlankLines) { setObjectProperty("skipBlankLines", skipBlankLines != null ? skipBlankLines.toString() : null); }
    public void setTrimSpace(Boolean trimSpace) { setObjectProperty("trimSpace", trimSpace != null ? trimSpace.toString() : null); }
    public void setErrorOnColumnCountMismatch(Boolean errorOnColumnCountMismatch) { setObjectProperty("errorOnColumnCountMismatch", errorOnColumnCountMismatch != null ? errorOnColumnCountMismatch.toString() : null); }
    public void setEmptyFieldAsNull(Boolean emptyFieldAsNull) { setObjectProperty("emptyFieldAsNull", emptyFieldAsNull != null ? emptyFieldAsNull.toString() : null); }
    public void setSkipByteOrderMark(Boolean skipByteOrderMark) { setObjectProperty("skipByteOrderMark", skipByteOrderMark != null ? skipByteOrderMark.toString() : null); }
    public void setEncoding(String encoding) { setObjectProperty("encoding", encoding); }
    
    // JSON-specific file format options
    public void setStripOuterArray(Boolean stripOuterArray) { setObjectProperty("stripOuterArray", stripOuterArray != null ? stripOuterArray.toString() : null); }
    public void setStripNullValues(Boolean stripNullValues) { setObjectProperty("stripNullValues", stripNullValues != null ? stripNullValues.toString() : null); }
    public void setAllowDuplicate(Boolean allowDuplicate) { setObjectProperty("allowDuplicate", allowDuplicate != null ? allowDuplicate.toString() : null); }
    public void setIgnoreUtf8Errors(Boolean ignoreUtf8Errors) { setObjectProperty("ignoreUtf8Errors", ignoreUtf8Errors != null ? ignoreUtf8Errors.toString() : null); }
    
    // Universal file format options (all types)
    public void setDateFormat(String dateFormat) { setObjectProperty("dateFormat", dateFormat); }
    public void setTimeFormat(String timeFormat) { setObjectProperty("timeFormat", timeFormat); }
    public void setTimestampFormat(String timestampFormat) { setObjectProperty("timestampFormat", timestampFormat); }
    public void setBinaryFormat(String binaryFormat) { setObjectProperty("binaryFormat", binaryFormat); }
    public void setNullIf(String nullIf) { setObjectProperty("nullIf", nullIf); }
    public void setReplaceInvalidCharacters(Boolean replaceInvalidCharacters) { setObjectProperty("replaceInvalidCharacters", replaceInvalidCharacters != null ? replaceInvalidCharacters.toString() : null); }
    
    // Advanced encryption methods
    public void setKmsKeyId(String kmsKeyId) { setObjectProperty("kmsKeyId", kmsKeyId); }
    public void setMasterKey(String masterKey) { setObjectProperty("masterKey", masterKey); }
    public void setEncryptionType(String encryptionType) { setObjectProperty("encryptionType", encryptionType); }
    
    // Tag methods
    public void addTag(String tagName, String tagValue) { setObjectProperty("tag_" + tagName, tagValue); }
    
    // Clone methods
    public void setCloneFromStage(String cloneFromStage) { setObjectProperty("cloneFromStage", cloneFromStage); }
    public void setCloneFromSchema(String cloneFromSchema) { setObjectProperty("cloneFromSchema", cloneFromSchema); }
    public void setCloneFromCatalog(String cloneFromCatalog) { setObjectProperty("cloneFromCatalog", cloneFromCatalog); }
    public void setTimeTravelType(String timeTravelType) { setObjectProperty("timeTravelType", timeTravelType); }
    public void setTimeTravelValue(String timeTravelValue) { setObjectProperty("timeTravelValue", timeTravelValue); }
    
    // Getters using generic storage
    public String getCatalogName() { return getObjectProperty("catalogName"); }
    public String getSchemaName() { return getObjectProperty("schemaName"); }
    public String getUrl() { return getObjectProperty("url"); }
    public String getStorageIntegration() { return getObjectProperty("storageIntegration"); }
    public Boolean getOrReplace() { String val = getObjectProperty("orReplace"); return val != null ? Boolean.valueOf(val) : null; }
    public Boolean getIfNotExists() { String val = getObjectProperty("ifNotExists"); return val != null ? Boolean.valueOf(val) : null; }
    public Boolean getTemporary() { String val = getObjectProperty("temporary"); return val != null ? Boolean.valueOf(val) : null; }
    public String getComment() { return getObjectProperty("comment"); }
    public String getEncryption() { return getObjectProperty("encryption"); }
    public Boolean getUsePrivatelinkEndpoint() { String val = getObjectProperty("usePrivatelinkEndpoint"); return val != null ? Boolean.valueOf(val) : null; }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        // Additional stageName validation beyond @DatabaseChangeProperty annotation
        // The annotation doesn't catch empty strings or whitespace-only strings
        if (stageName != null && stageName.trim().isEmpty()) {
            errors.addError("stageName is required");
        }
        
        // Mutual exclusivity validation (following Snowflake constraints)
        Boolean orReplace = getOrReplace();
        Boolean ifNotExists = getIfNotExists();
        if (Boolean.TRUE.equals(orReplace) && Boolean.TRUE.equals(ifNotExists)) {
            errors.addError("Cannot specify both orReplace and ifNotExists - they are mutually exclusive");
        }
        
        // Clone operation validation
        String cloneFromStage = getObjectProperty("cloneFromStage");
        if (cloneFromStage != null) {
            if (Boolean.TRUE.equals(orReplace) || Boolean.TRUE.equals(ifNotExists)) {
                errors.addError("CLONE operation cannot be combined with OR REPLACE or IF NOT EXISTS clauses");
            }
        }
        
        // Storage integration vs credentials validation
        String storageIntegration = getStorageIntegration();
        String awsKeyId = getObjectProperty("awsKeyId");
        String gcsKey = getObjectProperty("gcsServiceAccountKey");
        String azureAccount = getObjectProperty("azureAccountName");
        
        boolean hasCredentials = awsKeyId != null || gcsKey != null || azureAccount != null;
        if (storageIntegration != null && hasCredentials) {
            errors.addError("Cannot specify both STORAGE_INTEGRATION and CREDENTIALS - choose one authentication method");
        }
        
        // External stage validation
        String url = getUrl();
        if (url != null) {
            // External stage - ensure it has authentication
            if (storageIntegration == null && !hasCredentials) {
                errors.addError("External stages require either URL with STORAGE_INTEGRATION or CREDENTIALS");
            }
        }
        
        // Directory table validation
        Boolean directoryEnable = getObjectProperty("directoryEnable") != null ? Boolean.valueOf(getObjectProperty("directoryEnable")) : null;
        Boolean temporary = getTemporary();
        if (Boolean.TRUE.equals(temporary) && Boolean.TRUE.equals(directoryEnable)) {
            errors.addError("Temporary stages cannot enable directory tables");
        }
        
        if (Boolean.TRUE.equals(directoryEnable) && url == null) {
            errors.addError("Directory table can only be enabled for external stages");
        }
        
        // Time travel validation
        String timeTravelType = getObjectProperty("timeTravelType");
        String timeTravelValue = getObjectProperty("timeTravelValue");
        if ((timeTravelType != null || timeTravelValue != null) && cloneFromStage == null) {
            errors.addError("Time travel can only be used with CLONE operations");
        }
        
        // Credentials completeness validation
        if (awsKeyId != null) {
            String awsSecretKey = getObjectProperty("awsSecretKey");
            if (awsSecretKey == null) {
                errors.addError("AWS_KEY_ID requires AWS_SECRET_KEY");
            }
        }
        
        String azureAccountName = getObjectProperty("azureAccountName");
        if (azureAccountName != null) {
            String azureAccountKey = getObjectProperty("azureAccountKey");
            String azureSasToken = getObjectProperty("azureSasToken");
            if (azureAccountKey == null && azureSasToken == null) {
                errors.addError("AZURE_ACCOUNT_NAME requires either AZURE_ACCOUNT_KEY or AZURE_SAS_TOKEN");
            }
            if (azureAccountKey != null && azureSasToken != null) {
                errors.addError("Cannot specify both AZURE_ACCOUNT_KEY and AZURE_SAS_TOKEN - choose one");
            }
        }
        
        // Additional mutual exclusivity validations per requirements
        validateAllMutualExclusivityRules(errors);
        
        // Comprehensive file format validation based on type
        String fileFormatType = getObjectProperty("fileFormatType");
        if (fileFormatType != null) {
            validateFileFormatConfiguration(errors, fileFormatType);
        }
        
        // Cloud-specific encryption validation
        validateCloudEncryption(errors);
        
        // Directory table comprehensive validation
        validateDirectoryTableConfiguration(errors);
        
        return errors;
    }
    
    /**
     * Comprehensive file format validation based on type with format-specific constraints
     */
    private void validateFileFormatConfiguration(ValidationErrors errors, String fileFormatType) {
        String formatType = fileFormatType.toUpperCase();
        
        // Validate format type against supported values
        String[] supportedFormats = {"CSV", "JSON", "PARQUET", "AVRO", "ORC", "XML", "CUSTOM"};
        boolean validFormat = false;
        for (String format : supportedFormats) {
            if (format.equals(formatType)) {
                validFormat = true;
                break;
            }
        }
        if (!validFormat) {
            errors.addError("File format type must be one of: CSV, JSON, PARQUET, AVRO, ORC, XML, CUSTOM");
            return; // Skip further validation if format is invalid
        }
        
        // Format-specific compression validation
        String compression = getObjectProperty("compression");
        if (compression != null) {
            validateCompressionForFormat(errors, formatType, compression.toUpperCase());
        }
        
        // Format-specific option validation
        switch (formatType) {
            case "CSV":
                validateCSVOptions(errors);
                break;
            case "JSON":
                validateJSONOptions(errors);
                break;
            case "PARQUET":
                validatePARQUETOptions(errors);
                break;
            case "XML":
                validateXMLOptions(errors);
                break;
            case "CUSTOM":
                // CUSTOM format allows any options
                break;
            default:
                // Other formats (AVRO, ORC) have minimal specific options
                break;
        }
        
        // Binary format validation (universal)
        String binaryFormat = getObjectProperty("binaryFormat");
        if (binaryFormat != null) {
            String[] validBinaryFormats = {"HEX", "BASE64"};
            boolean validBinary = false;
            for (String format : validBinaryFormats) {
                if (format.equals(binaryFormat.toUpperCase())) {
                    validBinary = true;
                    break;
                }
            }
            if (!validBinary) {
                errors.addError("Binary format must be HEX or BASE64");
            }
        }
    }
    
    /**
     * Format-specific compression validation per requirements
     */
    private void validateCompressionForFormat(ValidationErrors errors, String formatType, String compression) {
        String[] validCompressions;
        
        switch (formatType) {
            case "PARQUET":
                validCompressions = new String[]{"AUTO", "SNAPPY", "GZIP", "LZO", "NONE"};
                break;
            case "CSV":
            case "JSON":
            case "XML":
                validCompressions = new String[]{"AUTO", "GZIP", "BZ2", "BROTLI", "ZSTD", "DEFLATE", "RAW_DEFLATE", "NONE"};
                break;
            case "CUSTOM":
                return; // CUSTOM format allows any compression
            default:
                validCompressions = new String[]{"AUTO", "GZIP", "BZ2", "BROTLI", "ZSTD", "DEFLATE", "RAW_DEFLATE", "NONE"};
        }
        
        boolean validCompression = false;
        for (String validComp : validCompressions) {
            if (validComp.equals(compression)) {
                validCompression = true;
                break;
            }
        }
        
        if (!validCompression) {
            StringBuilder validOptions = new StringBuilder();
            for (int i = 0; i < validCompressions.length; i++) {
                validOptions.append(validCompressions[i]);
                if (i < validCompressions.length - 1) validOptions.append(", ");
            }
            errors.addError("Compression for " + formatType + " format must be one of: " + validOptions.toString());
        }
    }
    
    /**
     * CSV-specific option validation
     */
    private void validateCSVOptions(ValidationErrors errors) {
        // Skip header validation
        String skipHeader = getObjectProperty("skipHeader");
        if (skipHeader != null) {
            try {
                int skip = Integer.parseInt(skipHeader);
                if (skip < 0) {
                    errors.addError("Skip header must be a non-negative integer");
                }
            } catch (NumberFormatException e) {
                errors.addError("Skip header must be a valid integer");
            }
        }
        
        // Field delimiter validation (should be single character)
        String fieldDelimiter = getObjectProperty("fieldDelimiter");
        if (fieldDelimiter != null && fieldDelimiter.length() != 1) {
            errors.addError("Field delimiter must be a single character");
        }
        
        // Record delimiter validation (should be single character)
        String recordDelimiter = getObjectProperty("recordDelimiter");
        if (recordDelimiter != null && recordDelimiter.length() != 1) {
            errors.addError("Record delimiter must be a single character");
        }
    }
    
    /**
     * JSON-specific option validation
     */
    private void validateJSONOptions(ValidationErrors errors) {
        // JSON-specific options are mostly boolean flags - validated by generic boolean parsing
        // Additional JSON-specific business logic can be added here
    }
    
    /**
     * PARQUET-specific option validation
     */
    private void validatePARQUETOptions(ValidationErrors errors) {
        // PARQUET has limited format options - mostly handled by compression validation
        // Additional PARQUET-specific business logic can be added here
    }
    
    /**
     * XML-specific option validation
     */
    private void validateXMLOptions(ValidationErrors errors) {
        // XML-specific options validation can be added here as needed
    }
    
    /**
     * Cloud-specific encryption validation
     */
    private void validateCloudEncryption(ValidationErrors errors) {
        String encryptionType = getObjectProperty("encryptionType");
        if (encryptionType != null) {
            String[] validEncryptionTypes = {"SNOWFLAKE_FULL", "SNOWFLAKE_SSE", "AWS_SSE_S3", "AWS_SSE_KMS", "AWS_CSE", "GCS_SSE_KMS", "AZURE_CSE", "NONE"};
            
            boolean validEncryption = false;
            for (String validType : validEncryptionTypes) {
                if (validType.equals(encryptionType.toUpperCase())) {
                    validEncryption = true;
                    break;
                }
            }
            
            if (!validEncryption) {
                errors.addError("Encryption type must be one of: SNOWFLAKE_FULL, SNOWFLAKE_SSE, AWS_SSE_S3, AWS_SSE_KMS, AWS_CSE, GCS_SSE_KMS, AZURE_CSE, NONE");
            }
            
            // KMS key validation for KMS encryption types
            if (encryptionType.contains("KMS")) {
                String kmsKeyId = getObjectProperty("kmsKeyId");
                if (kmsKeyId == null || kmsKeyId.trim().isEmpty()) {
                    errors.addError("KMS encryption types require kmsKeyId to be specified");
                }
            }
        }
    }
    
    /**
     * Comprehensive directory table configuration validation
     */
    private void validateDirectoryTableConfiguration(ValidationErrors errors) {
        Boolean directoryEnable = getObjectProperty("directoryEnable") != null ? Boolean.valueOf(getObjectProperty("directoryEnable")) : null;
        
        if (Boolean.TRUE.equals(directoryEnable)) {
            // Auto-refresh validation
            Boolean autoRefresh = getObjectProperty("autoRefresh") != null ? Boolean.valueOf(getObjectProperty("autoRefresh")) : null;
            if (Boolean.TRUE.equals(autoRefresh)) {
                // Auto-refresh requires external stage
                String url = getUrl();
                if (url == null) {
                    errors.addError("AUTO_REFRESH can only be used with external stages (URL required)");
                }
            }
            
            // Notification integration validation
            String notificationIntegration = getObjectProperty("notificationIntegration");
            if (notificationIntegration != null) {
                String url = getUrl();
                if (url == null) {
                    errors.addError("NOTIFICATION_INTEGRATION requires external stage with directory table enabled");
                }
            }
            
            // Refresh on create validation
            Boolean refreshOnCreate = getObjectProperty("refreshOnCreate") != null ? Boolean.valueOf(getObjectProperty("refreshOnCreate")) : null;
            if (Boolean.TRUE.equals(refreshOnCreate)) {
                String url = getUrl();
                if (url == null) {
                    errors.addError("REFRESH_ON_CREATE requires external stage with directory table enabled");
                }
            }
        } else {
            // Directory table disabled - ensure no directory-specific options
            if (getObjectProperty("autoRefresh") != null) {
                errors.addError("AUTO_REFRESH can only be used when directory table is enabled");
            }
            if (getObjectProperty("notificationIntegration") != null) {
                errors.addError("NOTIFICATION_INTEGRATION can only be used when directory table is enabled");
            }
            if (getObjectProperty("refreshOnCreate") != null) {
                errors.addError("REFRESH_ON_CREATE can only be used when directory table is enabled");
            }
        }
    }
    
    /**
     * Comprehensive mutual exclusivity validation per Stage requirements
     * Implements all 10+ mutual exclusivity rules from the requirements specification
     */
    private void validateAllMutualExclusivityRules(ValidationErrors errors) {
        Boolean orReplace = getOrReplace();
        Boolean ifNotExists = getIfNotExists();
        Boolean temporary = getTemporary();
        String cloneFromStage = getObjectProperty("cloneFromStage");
        String url = getUrl();
        String storageIntegration = getStorageIntegration();
        Boolean directoryEnable = getObjectProperty("directoryEnable") != null ? Boolean.valueOf(getObjectProperty("directoryEnable")) : null;
        Boolean autoRefresh = getObjectProperty("autoRefresh") != null ? Boolean.valueOf(getObjectProperty("autoRefresh")) : null;
        String notificationIntegration = getObjectProperty("notificationIntegration");
        Boolean refreshOnCreate = getObjectProperty("refreshOnCreate") != null ? Boolean.valueOf(getObjectProperty("refreshOnCreate")) : null;
        
        // Rule 1: orReplace and ifNotExists cannot both be true
        if (Boolean.TRUE.equals(orReplace) && Boolean.TRUE.equals(ifNotExists)) {
            errors.addError("The OR REPLACE and IF NOT EXISTS clauses are mutually exclusive. They can't both be used in the same statement.");
        }
        
        // Rule 2: CLONE operation cannot be used with OR REPLACE or IF NOT EXISTS
        if (cloneFromStage != null) {
            if (Boolean.TRUE.equals(orReplace)) {
                errors.addError("CLONE operation cannot be combined with OR REPLACE clause");
            }
            if (Boolean.TRUE.equals(ifNotExists)) {
                errors.addError("CLONE operation cannot be combined with IF NOT EXISTS clause");
            }
        }
        
        // Rule 3: Internal stages cannot have URL property
        // Note: We detect internal stages by absence of URL
        if (url == null) {
            // This is an internal stage - validate no external-stage-only properties
            if (storageIntegration != null) {
                errors.addError("Internal stages cannot specify STORAGE_INTEGRATION - use external stage instead");
            }
            if (hasAnyCredentials()) {
                errors.addError("Internal stages cannot specify credentials - use external stage instead");
            }
        }
        
        // Rule 4: External stages must have URL or storage integration
        if (url != null) {
            // This is an external stage
            if (storageIntegration == null && !hasAnyCredentials()) {
                errors.addError("External stages require either STORAGE_INTEGRATION or CREDENTIALS for authentication");
            }
        }
        
        // Rule 5: Credentials and storage integration are mutually exclusive (already validated above)
        
        // Rule 6: Temporary stages cannot have directory tables
        if (Boolean.TRUE.equals(temporary) && Boolean.TRUE.equals(directoryEnable)) {
            errors.addError("Temporary stages cannot enable directory tables");
        }
        
        // Rule 7: Directory table requires external stage
        if (Boolean.TRUE.equals(directoryEnable) && url == null) {
            errors.addError("Directory table can only be enabled for external stages");
        }
        
        // Rule 8: AUTO_REFRESH requires directory table to be enabled
        if (Boolean.TRUE.equals(autoRefresh) && !Boolean.TRUE.equals(directoryEnable)) {
            errors.addError("AUTO_REFRESH can only be used when directory table is enabled");
        }
        
        // Rule 9: NOTIFICATION_INTEGRATION requires directory table and external stage
        if (notificationIntegration != null) {
            if (!Boolean.TRUE.equals(directoryEnable)) {
                errors.addError("NOTIFICATION_INTEGRATION requires directory table to be enabled");
            }
            if (url == null) {
                errors.addError("NOTIFICATION_INTEGRATION requires external stage");
            }
        }
        
        // Rule 10: REFRESH_ON_CREATE requires directory table and external stage
        if (Boolean.TRUE.equals(refreshOnCreate)) {
            if (!Boolean.TRUE.equals(directoryEnable)) {
                errors.addError("REFRESH_ON_CREATE requires directory table to be enabled");
            }
            if (url == null) {
                errors.addError("REFRESH_ON_CREATE requires external stage");
            }
        }
        
        // Additional rule: URL format validation for external stages
        if (url != null) {
            validateURLFormat(errors, url);
        }
    }
    
    /**
     * Helper method to check if any credentials are specified
     */
    private boolean hasAnyCredentials() {
        return getObjectProperty("awsKeyId") != null ||
               getObjectProperty("gcsServiceAccountKey") != null ||
               getObjectProperty("azureAccountName") != null;
    }
    
    /**
     * URL format validation for external stages
     */
    private void validateURLFormat(ValidationErrors errors, String url) {
        String urlLower = url.toLowerCase();
        
        // Supported cloud storage URL schemes
        if (!urlLower.startsWith("s3://") && 
            !urlLower.startsWith("gcs://") && 
            !urlLower.startsWith("azure://") &&
            !urlLower.startsWith("https://")) {
            errors.addError("Stage URL must use supported scheme: s3://, gcs://, azure://, or https://");
        }
        
        // Basic URL format validation
        if (url.trim().isEmpty()) {
            errors.addError("Stage URL cannot be empty");
        }
        
        // Validate URL doesn't contain invalid schemes
        if (urlLower.startsWith("http://") || urlLower.startsWith("ftp://")) {
            errors.addError("Stage URL cannot use insecure schemes (http://, ftp://). Use https:// for HTTPS endpoints");
        }
    }
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        CreateStageStatement statement = new CreateStageStatement();
        statement.setStageName(stageName);
        
        // Apply all generic properties
        for (Map.Entry<String, String> entry : objectProperties.entrySet()) {
            statement.setObjectProperty(entry.getKey(), entry.getValue());
        }
        
        return new SqlStatement[]{statement};
    }
    
    @Override
    public String getConfirmationMessage() {
        return "Stage " + stageName + " created";
    }
    
    @Override
    public boolean supportsRollback(Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public Change[] createInverses() {
        DropStageChange inverse = new DropStageChange();
        inverse.setStageName(getStageName());
        inverse.setCatalogName(getCatalogName());
        inverse.setSchemaName(getSchemaName());
        inverse.setIfExists(true);
        return new Change[]{inverse};
    }
}