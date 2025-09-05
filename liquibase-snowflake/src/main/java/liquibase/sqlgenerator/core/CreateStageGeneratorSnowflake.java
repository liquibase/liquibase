package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.CreateStageStatement;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtil;

import java.util.Map;
import java.util.HashMap;

/**
 * SQL Generator for CREATE STAGE statements in Snowflake.
 * Handles complex conditional SQL generation with multiple DDL patterns.
 */
public class CreateStageGeneratorSnowflake extends AbstractSqlGenerator<CreateStageStatement> {

    @Override
    public boolean supports(CreateStageStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(CreateStageStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        
        if (StringUtil.isEmpty(statement.getStageName())) {
            validationErrors.addError("stageName is required for CREATE STAGE");
        }
        
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateStageStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder();
        Map<String, String> properties = statement.getObjectProperties();
        
        // Start with CREATE
        sql.append("CREATE");
        
        // OR REPLACE clause
        if (Boolean.TRUE.equals(statement.getOrReplace())) {
            sql.append(" OR REPLACE");
        }
        
        // TEMPORARY clause
        if (Boolean.TRUE.equals(statement.getTemporary())) {
            sql.append(" TEMPORARY");
        }
        
        sql.append(" STAGE");
        
        // IF NOT EXISTS clause
        if (Boolean.TRUE.equals(statement.getIfNotExists())) {
            sql.append(" IF NOT EXISTS");
        }
        
        // Stage name with schema qualification
        sql.append(" ");
        if (!StringUtil.isEmpty(statement.getCatalogName()) && !StringUtil.isEmpty(statement.getSchemaName())) {
            sql.append(database.escapeObjectName(statement.getCatalogName(), Schema.class))
               .append(".")
               .append(database.escapeObjectName(statement.getSchemaName(), Schema.class))
               .append(".");
        } else if (!StringUtil.isEmpty(statement.getSchemaName())) {
            sql.append(database.escapeObjectName(statement.getSchemaName(), Schema.class))
               .append(".");
        }
        sql.append(database.escapeObjectName(statement.getStageName(), Schema.class));
        
        // Check if this is a CLONE operation
        String cloneFromStage = properties.get("cloneFromStage");
        if (cloneFromStage != null) {
            sql.append(" CLONE ");
            
            // Add source stage with optional schema/catalog qualification
            String cloneFromCatalog = properties.get("cloneFromCatalog");
            String cloneFromSchema = properties.get("cloneFromSchema");
            
            if (!StringUtil.isEmpty(cloneFromCatalog) && !StringUtil.isEmpty(cloneFromSchema)) {
                sql.append(database.escapeObjectName(cloneFromCatalog, Schema.class))
                   .append(".")
                   .append(database.escapeObjectName(cloneFromSchema, Schema.class))
                   .append(".");
            } else if (!StringUtil.isEmpty(cloneFromSchema)) {
                sql.append(database.escapeObjectName(cloneFromSchema, Schema.class))
                   .append(".");
            }
            sql.append(database.escapeObjectName(cloneFromStage, Schema.class));
            
            // Add time travel clause if specified
            String timeTravelType = properties.get("timeTravelType");
            String timeTravelValue = properties.get("timeTravelValue");
            if (timeTravelType != null && timeTravelValue != null) {
                sql.append(" ").append(timeTravelType.toUpperCase()).append(" (");
                
                if ("TIMESTAMP".equals(timeTravelType.toUpperCase())) {
                    sql.append("TIMESTAMP => '").append(timeTravelValue).append("'");
                } else if ("OFFSET".equals(timeTravelType.toUpperCase())) {
                    sql.append("OFFSET => ").append(timeTravelValue);
                } else if ("STATEMENT".equals(timeTravelType.toUpperCase())) {
                    sql.append("STATEMENT => '").append(timeTravelValue).append("'");
                }
                sql.append(")");
            }
        } else {
            // Regular CREATE STAGE (not CLONE)
            
            // URL for external stages
            String url = statement.getUrl();
            if (url != null) {
                sql.append(" URL = '").append(url).append("'");
            }
            
            // Authentication - Storage Integration or Credentials (mutually exclusive)
            String storageIntegration = statement.getStorageIntegration();
            if (storageIntegration != null) {
                sql.append(" STORAGE_INTEGRATION = ").append(database.escapeObjectName(storageIntegration, Schema.class));
            } else {
                // Check for direct credentials
                appendCredentials(sql, properties, database);
            }
            
            // Encryption
            appendEncryption(sql, properties);
            
            // File format
            appendFileFormat(sql, properties);
            
            // Directory table
            appendDirectory(sql, properties);
            
            // Use private link endpoint
            String usePrivatelink = properties.get("usePrivatelinkEndpoint");
            if ("true".equals(usePrivatelink)) {
                sql.append(" USE_PRIVATELINK_ENDPOINT = TRUE");
            }
            
            // Comment
            String comment = statement.getComment();
            if (comment != null) {
                sql.append(" COMMENT = '").append(comment.replace("'", "''")).append("'");
            }
            
            // Tags
            appendTags(sql, properties);
        }
        
        return new Sql[]{new UnparsedSql(sql.toString(), getAffectedTable(statement))};
    }
    
    private void appendCredentials(StringBuilder sql, Map<String, String> properties, Database database) {
        String awsKeyId = properties.get("awsKeyId");
        String awsSecretKey = properties.get("awsSecretKey");
        String gcsKey = properties.get("gcsServiceAccountKey");
        String azureAccount = properties.get("azureAccountName");
        
        if (awsKeyId != null && awsSecretKey != null) {
            sql.append(" CREDENTIALS = (");
            sql.append("AWS_KEY_ID = '").append(awsKeyId).append("'");
            sql.append(" AWS_SECRET_KEY = '").append(awsSecretKey).append("'");
            
            String awsToken = properties.get("awsToken");
            if (awsToken != null) {
                sql.append(" AWS_TOKEN = '").append(awsToken).append("'");
            }
            
            String awsRole = properties.get("awsRole");
            if (awsRole != null) {
                sql.append(" AWS_ROLE = '").append(awsRole).append("'");
            }
            
            sql.append(")");
        } else if (gcsKey != null) {
            sql.append(" CREDENTIALS = (");
            sql.append("GCS_SERVICE_ACCOUNT_KEY = '").append(gcsKey).append("'");
            sql.append(")");
        } else if (azureAccount != null) {
            String azureKey = properties.get("azureAccountKey");
            String azureSas = properties.get("azureSasToken");
            
            sql.append(" CREDENTIALS = (");
            sql.append("AZURE_ACCOUNT_NAME = '").append(azureAccount).append("'");
            
            if (azureKey != null) {
                sql.append(" AZURE_ACCOUNT_KEY = '").append(azureKey).append("'");
            } else if (azureSas != null) {
                sql.append(" AZURE_SAS_TOKEN = '").append(azureSas).append("'");
            }
            
            sql.append(")");
        }
    }
    
    private void appendEncryption(StringBuilder sql, Map<String, String> properties) {
        String encryption = properties.get("encryption");
        String encryptionType = properties.get("encryptionType");
        
        // Use encryptionType if specified, otherwise fall back to encryption
        String actualEncryptionType = encryptionType != null ? encryptionType : encryption;
        
        if (actualEncryptionType != null) {
            sql.append(" ENCRYPTION = (");
            
            // Snowflake internal encryption types
            if ("SNOWFLAKE_FULL".equals(actualEncryptionType) || "SNOWFLAKE_SSE".equals(actualEncryptionType)) {
                sql.append("TYPE = '").append(actualEncryptionType).append("'");
            } else {
                // Cloud-specific encryption types
                sql.append("TYPE = '").append(actualEncryptionType).append("'");
                
                // Add cloud-specific encryption parameters
                appendCloudEncryptionParameters(sql, properties, actualEncryptionType);
            }
            
            sql.append(")");
        }
    }
    
    /**
     * Append cloud-specific encryption parameters based on encryption type
     */
    private void appendCloudEncryptionParameters(StringBuilder sql, Map<String, String> properties, String encryptionType) {
        String kmsKeyId = properties.get("kmsKeyId");
        String masterKey = properties.get("masterKey");
        
        // AWS encryption types
        if (encryptionType.startsWith("AWS_")) {
            if ("AWS_SSE_KMS".equals(encryptionType) && kmsKeyId != null) {
                sql.append(" KMS_KEY_ID = '").append(kmsKeyId).append("'");
            } else if ("AWS_CSE".equals(encryptionType) && masterKey != null) {
                sql.append(" MASTER_KEY = '").append(masterKey).append("'");
            }
        }
        // Google Cloud encryption types
        else if (encryptionType.startsWith("GCS_")) {
            if ("GCS_SSE_KMS".equals(encryptionType) && kmsKeyId != null) {
                sql.append(" KMS_KEY_ID = '").append(kmsKeyId).append("'");
            }
        }
        // Azure encryption types
        else if (encryptionType.startsWith("AZURE_")) {
            if ("AZURE_CSE".equals(encryptionType) && masterKey != null) {
                sql.append(" MASTER_KEY = '").append(masterKey).append("'");
            }
        }
        // Generic fallback for any encryption type with KMS or master key
        else {
            if (kmsKeyId != null) {
                sql.append(" KMS_KEY_ID = '").append(kmsKeyId).append("'");
            }
            if (masterKey != null) {
                sql.append(" MASTER_KEY = '").append(masterKey).append("'");
            }
        }
    }
    
    private void appendFileFormat(StringBuilder sql, Map<String, String> properties) {
        String fileFormat = properties.get("fileFormat");
        String fileFormatType = properties.get("fileFormatType");
        
        // Check if any file format options are specified
        if (hasAnyFileFormatOptions(properties)) {
            sql.append(" FILE_FORMAT = (");
            
            if (fileFormat != null) {
                // Reference to existing file format
                sql.append("FORMAT_NAME = '").append(fileFormat).append("'");
            } else {
                // Inline file format definition with comprehensive options
                boolean hasOptions = false;
                
                // Type (required for inline definitions)
                if (fileFormatType != null) {
                    sql.append("TYPE = '").append(fileFormatType).append("'");
                    hasOptions = true;
                }
                
                // Compression
                String compression = properties.get("compression");
                if (compression != null) {
                    if (hasOptions) sql.append(" ");
                    sql.append("COMPRESSION = '").append(compression).append("'");
                    hasOptions = true;
                }
                
                // CSV-specific options
                hasOptions = appendCSVOptions(sql, properties, hasOptions);
                
                // JSON-specific options
                hasOptions = appendJSONOptions(sql, properties, hasOptions);
                
                // Universal format options
                hasOptions = appendUniversalFormatOptions(sql, properties, hasOptions);
            }
            
            sql.append(")");
        }
    }
    
    /**
     * Check if any file format options are specified
     */
    private boolean hasAnyFileFormatOptions(Map<String, String> properties) {
        return properties.get("fileFormat") != null ||
               properties.get("fileFormatType") != null ||
               properties.get("compression") != null ||
               properties.get("recordDelimiter") != null ||
               properties.get("fieldDelimiter") != null ||
               properties.get("fieldOptionallyEnclosedBy") != null ||
               properties.get("escape") != null ||
               properties.get("skipHeader") != null ||
               properties.get("dateFormat") != null ||
               properties.get("timeFormat") != null ||
               properties.get("timestampFormat") != null ||
               properties.get("binaryFormat") != null ||
               properties.get("nullIf") != null ||
               properties.get("stripOuterArray") != null ||
               properties.get("stripNullValues") != null ||
               properties.get("allowDuplicate") != null;
    }
    
    /**
     * Append CSV-specific file format options
     */
    private boolean appendCSVOptions(StringBuilder sql, Map<String, String> properties, boolean hasOptions) {
        // Record delimiter
        String recordDelimiter = properties.get("recordDelimiter");
        if (recordDelimiter != null) {
            if (hasOptions) sql.append(" ");
            sql.append("RECORD_DELIMITER = '").append(escapeDelimiter(recordDelimiter)).append("'");
            hasOptions = true;
        }
        
        // Field delimiter
        String fieldDelimiter = properties.get("fieldDelimiter");
        if (fieldDelimiter != null) {
            if (hasOptions) sql.append(" ");
            sql.append("FIELD_DELIMITER = '").append(escapeDelimiter(fieldDelimiter)).append("'");
            hasOptions = true;
        }
        
        // Field optionally enclosed by
        String fieldOptionallyEnclosedBy = properties.get("fieldOptionallyEnclosedBy");
        if (fieldOptionallyEnclosedBy != null) {
            if (hasOptions) sql.append(" ");
            sql.append("FIELD_OPTIONALLY_ENCLOSED_BY = '").append(escapeDelimiter(fieldOptionallyEnclosedBy)).append("'");
            hasOptions = true;
        }
        
        // Escape character
        String escape = properties.get("escape");
        if (escape != null) {
            if (hasOptions) sql.append(" ");
            sql.append("ESCAPE = '").append(escapeDelimiter(escape)).append("'");
            hasOptions = true;
        }
        
        // Escape unenclosed field
        String escapeUnenclosedField = properties.get("escapeUnenclosedField");
        if (escapeUnenclosedField != null) {
            if (hasOptions) sql.append(" ");
            sql.append("ESCAPE_UNENCLOSED_FIELD = '").append(escapeDelimiter(escapeUnenclosedField)).append("'");
            hasOptions = true;
        }
        
        // Skip header
        String skipHeader = properties.get("skipHeader");
        if (skipHeader != null) {
            if (hasOptions) sql.append(" ");
            sql.append("SKIP_HEADER = ").append(skipHeader);
            hasOptions = true;
        }
        
        // Boolean options
        hasOptions = appendBooleanOption(sql, properties, "skipBlankLines", "SKIP_BLANK_LINES", hasOptions);
        hasOptions = appendBooleanOption(sql, properties, "trimSpace", "TRIM_SPACE", hasOptions);
        hasOptions = appendBooleanOption(sql, properties, "errorOnColumnCountMismatch", "ERROR_ON_COLUMN_COUNT_MISMATCH", hasOptions);
        hasOptions = appendBooleanOption(sql, properties, "emptyFieldAsNull", "EMPTY_FIELD_AS_NULL", hasOptions);
        hasOptions = appendBooleanOption(sql, properties, "skipByteOrderMark", "SKIP_BYTE_ORDER_MARK", hasOptions);
        
        // Encoding
        String encoding = properties.get("encoding");
        if (encoding != null) {
            if (hasOptions) sql.append(" ");
            sql.append("ENCODING = '").append(encoding).append("'");
            hasOptions = true;
        }
        
        return hasOptions;
    }
    
    /**
     * Append JSON-specific file format options
     */
    private boolean appendJSONOptions(StringBuilder sql, Map<String, String> properties, boolean hasOptions) {
        hasOptions = appendBooleanOption(sql, properties, "stripOuterArray", "STRIP_OUTER_ARRAY", hasOptions);
        hasOptions = appendBooleanOption(sql, properties, "stripNullValues", "STRIP_NULL_VALUES", hasOptions);
        hasOptions = appendBooleanOption(sql, properties, "allowDuplicate", "ALLOW_DUPLICATE", hasOptions);
        hasOptions = appendBooleanOption(sql, properties, "ignoreUtf8Errors", "IGNORE_UTF8_ERRORS", hasOptions);
        
        return hasOptions;
    }
    
    /**
     * Append universal file format options (all types)
     */
    private boolean appendUniversalFormatOptions(StringBuilder sql, Map<String, String> properties, boolean hasOptions) {
        // Date/time format options
        String dateFormat = properties.get("dateFormat");
        if (dateFormat != null) {
            if (hasOptions) sql.append(" ");
            sql.append("DATE_FORMAT = '").append(dateFormat).append("'");
            hasOptions = true;
        }
        
        String timeFormat = properties.get("timeFormat");
        if (timeFormat != null) {
            if (hasOptions) sql.append(" ");
            sql.append("TIME_FORMAT = '").append(timeFormat).append("'");
            hasOptions = true;
        }
        
        String timestampFormat = properties.get("timestampFormat");
        if (timestampFormat != null) {
            if (hasOptions) sql.append(" ");
            sql.append("TIMESTAMP_FORMAT = '").append(timestampFormat).append("'");
            hasOptions = true;
        }
        
        // Binary format
        String binaryFormat = properties.get("binaryFormat");
        if (binaryFormat != null) {
            if (hasOptions) sql.append(" ");
            sql.append("BINARY_FORMAT = '").append(binaryFormat).append("'");
            hasOptions = true;
        }
        
        // Null handling
        String nullIf = properties.get("nullIf");
        if (nullIf != null) {
            if (hasOptions) sql.append(" ");
            sql.append("NULL_IF = (").append(nullIf).append(")");
            hasOptions = true;
        }
        
        hasOptions = appendBooleanOption(sql, properties, "replaceInvalidCharacters", "REPLACE_INVALID_CHARACTERS", hasOptions);
        
        return hasOptions;
    }
    
    /**
     * Helper method to append boolean options
     */
    private boolean appendBooleanOption(StringBuilder sql, Map<String, String> properties, 
                                       String propertyKey, String sqlKey, boolean hasOptions) {
        String value = properties.get(propertyKey);
        if (value != null) {
            if (hasOptions) sql.append(" ");
            sql.append(sqlKey).append(" = ").append(value.toUpperCase());
            return true;
        }
        return hasOptions;
    }
    
    /**
     * Helper method to escape delimiter characters for SQL
     */
    private String escapeDelimiter(String delimiter) {
        // Handle special characters that need escaping
        if ("\n".equals(delimiter)) return "\\n";
        if ("\t".equals(delimiter)) return "\\t";
        if ("\r".equals(delimiter)) return "\\r";
        if ("'".equals(delimiter)) return "''";
        return delimiter;
    }
    
    private void appendDirectory(StringBuilder sql, Map<String, String> properties) {
        String directoryEnable = properties.get("directoryEnable");
        if ("true".equals(directoryEnable)) {
            sql.append(" DIRECTORY = (ENABLE = TRUE");
            
            String autoRefresh = properties.get("autoRefresh");
            if ("true".equals(autoRefresh)) {
                sql.append(" AUTO_REFRESH = TRUE");
            } else if ("false".equals(autoRefresh)) {
                sql.append(" AUTO_REFRESH = FALSE");
            }
            
            String refreshOnCreate = properties.get("refreshOnCreate");
            if ("true".equals(refreshOnCreate)) {
                sql.append(" REFRESH_ON_CREATE = TRUE");
            } else if ("false".equals(refreshOnCreate)) {
                sql.append(" REFRESH_ON_CREATE = FALSE");
            }
            
            String notificationIntegration = properties.get("notificationIntegration");
            if (notificationIntegration != null) {
                sql.append(" NOTIFICATION_INTEGRATION = '").append(notificationIntegration).append("'");
            }
            
            sql.append(")");
        }
    }
    
    private void appendTags(StringBuilder sql, Map<String, String> properties) {
        // Collect all tag properties (prefixed with "tag_")
        Map<String, String> tags = new HashMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey().startsWith("tag_")) {
                String tagName = entry.getKey().substring(4); // Remove "tag_" prefix
                tags.put(tagName, entry.getValue());
            }
        }
        
        // Legacy single tag support (backward compatibility)
        String tagName = properties.get("tagName");
        String tagValue = properties.get("tagValue");
        if (tagName != null && tagValue != null) {
            tags.put(tagName, tagValue);
        }
        
        if (!tags.isEmpty()) {
            sql.append(" TAG (");
            boolean first = true;
            for (Map.Entry<String, String> tag : tags.entrySet()) {
                if (!first) {
                    sql.append(", ");
                }
                sql.append(tag.getKey()).append(" = '").append(tag.getValue().replace("'", "''")).append("'");
                first = false;
            }
            sql.append(")");
        }
    }
    
    protected String getAffectedTable(CreateStageStatement statement) {
        return statement.getStageName();
    }
}