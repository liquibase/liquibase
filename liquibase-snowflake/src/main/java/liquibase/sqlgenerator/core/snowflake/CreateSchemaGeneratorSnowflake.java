package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.CreateSchemaStatement;
import liquibase.structure.core.Table;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateSchemaGeneratorSnowflake extends AbstractSqlGenerator<CreateSchemaStatement> {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 10; // Higher priority to ensure this generator is used
    }

    @Override
    public boolean supports(CreateSchemaStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(CreateSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        errors.checkRequiredField("schemaName", statement.getSchemaName());
        
        // Validate that orReplace and ifNotExists are not both set
        if (Boolean.TRUE.equals(statement.getOrReplace()) && Boolean.TRUE.equals(statement.getIfNotExists())) {
            errors.addError("OR REPLACE and IF NOT EXISTS are mutually exclusive in Snowflake CREATE SCHEMA operations");
        }
        
        // Validate namespace attributes if present
        Map<String, String> namespaceAttrs = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getSchemaName());
        if (namespaceAttrs != null && !namespaceAttrs.isEmpty()) {
            
            // Validate transient schema constraints
            boolean isTransient = Boolean.parseBoolean(namespaceAttrs.get("transient"));
            String dataRetentionDays = namespaceAttrs.get("dataRetentionTimeInDays");
            
            if (isTransient && dataRetentionDays != null && !dataRetentionDays.equals("0")) {
                errors.addError("TRANSIENT schemas must have DATA_RETENTION_TIME_IN_DAYS = 0");
            }
            
            // Validate data retention constraints
            if (dataRetentionDays != null && namespaceAttrs.get("maxDataExtensionTimeInDays") != null) {
                try {
                    int dataRetention = Integer.parseInt(dataRetentionDays);
                    int maxDataExtension = Integer.parseInt(namespaceAttrs.get("maxDataExtensionTimeInDays"));
                    
                    if (dataRetention < 0 || dataRetention > 90) {
                        errors.addError("DATA_RETENTION_TIME_IN_DAYS must be between 0 and 90");
                    }
                    if (maxDataExtension < 0 || maxDataExtension > 90) {
                        errors.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS must be between 0 and 90");
                    }
                    if (maxDataExtension < dataRetention) {
                        errors.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS must be >= DATA_RETENTION_TIME_IN_DAYS");
                    }
                } catch (NumberFormatException e) {
                    errors.addError("DATA_RETENTION_TIME_IN_DAYS and MAX_DATA_EXTENSION_TIME_IN_DAYS must be valid integers");
                }
            }
            
            // Validate comment length
            String comment = namespaceAttrs.get("comment");
            if (comment != null && comment.length() > 256) {
                errors.addError("Schema comment cannot exceed 256 characters");
            }
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(CreateSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Check validation first - prevent generating invalid SQL
        ValidationErrors errors = validate(statement, database, sqlGeneratorChain);
        if (errors.hasErrors()) {
            throw new RuntimeException("Validation failed for CreateSchema: " + errors.toString());
        }
        
        StringBuilder sql = new StringBuilder("CREATE ");
        
        // Initialize variables for namespace and statement attributes
        boolean orReplace = Boolean.TRUE.equals(statement.getOrReplace());
        boolean ifNotExists = Boolean.TRUE.equals(statement.getIfNotExists());
        boolean isTransient = statement.getTransient() != null && statement.getTransient();
        boolean managedAccess = statement.getManaged() != null && statement.getManaged();
        String cloneFrom = statement.getCloneFrom();
        String comment = statement.getComment();
        String dataRetentionTimeInDays = statement.getDataRetentionTimeInDays();
        String maxDataExtensionTimeInDays = statement.getMaxDataExtensionTimeInDays();
        String defaultDdlCollation = statement.getDefaultDdlCollation();
        String pipeExecutionPaused = statement.getPipeExecutionPaused();
        String externalVolume = null;
        String catalog = null;
        Boolean replaceInvalidCharacters = null;
        String storageSerializationPolicy = null;
        String classificationProfile = null;
        String catalogSync = null;
        String catalogSyncNamespaceMode = null;
        String catalogSyncNamespaceFlattenDelimiter = null;
        String tag = null;
        
        // Check for namespace attributes first - they override statement attributes
        Map<String, String> namespaceAttrs = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getSchemaName());
        if (namespaceAttrs != null && !namespaceAttrs.isEmpty()) {
            // Process namespace attributes
            if (namespaceAttrs.get("orReplace") != null) {
                orReplace = Boolean.parseBoolean(namespaceAttrs.get("orReplace"));
            }
            if (namespaceAttrs.get("ifNotExists") != null) {
                ifNotExists = Boolean.parseBoolean(namespaceAttrs.get("ifNotExists"));
            }
            if (namespaceAttrs.get("transient") != null) {
                isTransient = Boolean.parseBoolean(namespaceAttrs.get("transient"));
            }
            if (namespaceAttrs.get("managedAccess") != null) {
                managedAccess = Boolean.parseBoolean(namespaceAttrs.get("managedAccess"));
            }
            
            cloneFrom = namespaceAttrs.get("cloneFrom");
            
            if (namespaceAttrs.get("comment") != null) {
                comment = namespaceAttrs.get("comment");
            }
            if (namespaceAttrs.get("dataRetentionTimeInDays") != null) {
                dataRetentionTimeInDays = namespaceAttrs.get("dataRetentionTimeInDays");
            }
            if (namespaceAttrs.get("maxDataExtensionTimeInDays") != null) {
                maxDataExtensionTimeInDays = namespaceAttrs.get("maxDataExtensionTimeInDays");
            }
            if (namespaceAttrs.get("defaultDdlCollation") != null) {
                defaultDdlCollation = namespaceAttrs.get("defaultDdlCollation");
            }
            if (namespaceAttrs.get("pipeExecutionPaused") != null) {
                pipeExecutionPaused = namespaceAttrs.get("pipeExecutionPaused");
            }
            
            externalVolume = namespaceAttrs.get("externalVolume");
            catalog = namespaceAttrs.get("catalog");
            
            if (namespaceAttrs.get("replaceInvalidCharacters") != null) {
                replaceInvalidCharacters = Boolean.parseBoolean(namespaceAttrs.get("replaceInvalidCharacters"));
            }
            
            storageSerializationPolicy = namespaceAttrs.get("storageSerializationPolicy");
            classificationProfile = namespaceAttrs.get("classificationProfile");
            catalogSync = namespaceAttrs.get("catalogSync");
            catalogSyncNamespaceMode = namespaceAttrs.get("catalogSyncNamespaceMode");
            catalogSyncNamespaceFlattenDelimiter = namespaceAttrs.get("catalogSyncNamespaceFlattenDelimiter");
            tag = namespaceAttrs.get("tag");
            
            // Clean up stored attributes
            SnowflakeNamespaceAttributeStorage.removeAttributes(statement.getSchemaName());
        }
        
        // Build SQL with processed attributes
        if (orReplace) {
            sql.append("OR REPLACE ");
        }
        
        if (isTransient) {
            sql.append("TRANSIENT ");
        }
        
        sql.append("SCHEMA ");
        
        if (ifNotExists) {
            sql.append("IF NOT EXISTS ");
        }
        
        // For schema creation, only qualify with database if catalogName is explicitly provided
        if (statement.getCatalogName() != null && !statement.getCatalogName().isEmpty()) {
            sql.append(database.escapeObjectName(statement.getCatalogName(), null, statement.getSchemaName(), liquibase.structure.core.Schema.class));
        } else {
            sql.append(database.escapeObjectName(statement.getSchemaName(), liquibase.structure.core.Schema.class));
        }
        
        // Handle cloning first (must come before other options)
        if (cloneFrom != null && !cloneFrom.isEmpty()) {
            if (statement.getCatalogName() != null && !statement.getCatalogName().isEmpty()) {
                sql.append(" CLONE ").append(database.escapeObjectName(statement.getCatalogName(), null, cloneFrom, liquibase.structure.core.Schema.class));
            } else {
                sql.append(" CLONE ").append(database.escapeObjectName(cloneFrom, liquibase.structure.core.Schema.class));
            }
        }
        
        // Build options list
        List<String> options = new ArrayList<>();
        
        if (managedAccess) {
            options.add("WITH MANAGED ACCESS");
        }
        
        if (dataRetentionTimeInDays != null && !dataRetentionTimeInDays.isEmpty()) {
            options.add("DATA_RETENTION_TIME_IN_DAYS = " + dataRetentionTimeInDays);
        }
        
        if (maxDataExtensionTimeInDays != null && !maxDataExtensionTimeInDays.isEmpty()) {
            options.add("MAX_DATA_EXTENSION_TIME_IN_DAYS = " + maxDataExtensionTimeInDays);
        }
        
        if (defaultDdlCollation != null && !defaultDdlCollation.isEmpty()) {
            options.add("DEFAULT_DDL_COLLATION = '" + defaultDdlCollation.replace("'", "''") + "'");
        }
        
        if (pipeExecutionPaused != null && !pipeExecutionPaused.isEmpty()) {
            options.add("PIPE_EXECUTION_PAUSED = " + pipeExecutionPaused.toUpperCase());
        }
        
        if (externalVolume != null && !externalVolume.isEmpty()) {
            options.add("EXTERNAL_VOLUME = '" + externalVolume.replace("'", "''") + "'");
        }
        
        if (catalog != null && !catalog.isEmpty()) {
            options.add("CATALOG = '" + catalog.replace("'", "''") + "'");
        }
        
        if (replaceInvalidCharacters != null) {
            options.add("REPLACE_INVALID_CHARACTERS = " + replaceInvalidCharacters.toString().toUpperCase());
        }
        
        if (storageSerializationPolicy != null && !storageSerializationPolicy.isEmpty()) {
            options.add("STORAGE_SERIALIZATION_POLICY = " + storageSerializationPolicy);
        }
        
        if (classificationProfile != null && !classificationProfile.isEmpty()) {
            options.add("CLASSIFICATION_PROFILE = '" + classificationProfile.replace("'", "''") + "'");
        }
        
        if (catalogSync != null && !catalogSync.isEmpty()) {
            options.add("CATALOG_SYNC = " + catalogSync);
        }
        
        if (catalogSyncNamespaceMode != null && !catalogSyncNamespaceMode.isEmpty()) {
            options.add("CATALOG_SYNC_NAMESPACE_MODE = " + catalogSyncNamespaceMode);
        }
        
        if (catalogSyncNamespaceFlattenDelimiter != null && !catalogSyncNamespaceFlattenDelimiter.isEmpty()) {
            options.add("CATALOG_SYNC_NAMESPACE_FLATTEN_DELIMITER = '" + catalogSyncNamespaceFlattenDelimiter.replace("'", "''") + "'");
        }
        
        if (tag != null && !tag.isEmpty()) {
            options.add("TAG (" + tag + ")");
        }
        
        if (comment != null && !comment.isEmpty()) {
            options.add("COMMENT = '" + comment.replace("'", "''") + "'");
        }
        
        if (!options.isEmpty()) {
            sql.append(" ");
            sql.append(String.join(" ", options));
        }

        return new Sql[]{new UnparsedSql(sql.toString())};
    }
}