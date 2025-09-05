package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.CreateTableGenerator;
import liquibase.statement.core.CreateTableStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CreateTableGeneratorSnowflake extends CreateTableGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 1;
    }

    @Override
    public boolean supports(CreateTableStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(CreateTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(statement, database, sqlGeneratorChain);
        
        // Additional Snowflake-specific validations
        if (statement.getTablespace() != null && 
            (statement.getTablespace().toLowerCase().contains("transient") || 
             statement.getTablespace().toLowerCase().contains("temporary"))) {
            // Allow these in tablespace field for backward compatibility
        }
        
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Check validation first - prevent generating invalid SQL
        ValidationErrors errors = validate(statement, database, sqlGeneratorChain);
        if (errors.hasErrors()) {
            throw new RuntimeException("Validation failed for CreateTable: " + errors.toString());
        }
        
        // First get the standard CREATE TABLE SQL
        Sql[] baseSql = super.generateSql(statement, database, sqlGeneratorChain);
        
        if (baseSql.length == 0) {
            return baseSql;
        }
        
        // Enhance the CREATE TABLE statement with Snowflake-specific features
        String originalSql = baseSql[0].toSql();
        
        // Initialize variables
        boolean isTransient = false;
        boolean isVolatile = false;
        boolean isTemporary = false;
        boolean isLocalTemporary = false;
        boolean isGlobalTemporary = false;
        String clusterByColumns = null;
        String dataRetentionDays = null;
        String maxDataExtensionDays = null;
        String comment = null;
        boolean copyGrants = false;
        boolean changeTracking = false;
        boolean enableSchemaEvolution = false;
        String stageFileFormat = null;
        String stageCopyOptions = null;
        String defaultDdlCollation = null;
        String tag = null;
        
        // First, check for namespace attributes
        Map<String, String> namespaceAttrs = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getTableName());
        if (namespaceAttrs != null && !namespaceAttrs.isEmpty()) {
            // Process namespace attributes
            isTransient = Boolean.parseBoolean(namespaceAttrs.get("transient"));
            isVolatile = Boolean.parseBoolean(namespaceAttrs.get("volatile"));
            isTemporary = Boolean.parseBoolean(namespaceAttrs.get("temporary"));
            isLocalTemporary = Boolean.parseBoolean(namespaceAttrs.get("localTemporary"));
            isGlobalTemporary = Boolean.parseBoolean(namespaceAttrs.get("globalTemporary"));
            clusterByColumns = namespaceAttrs.get("clusterBy");
            dataRetentionDays = namespaceAttrs.get("dataRetentionTimeInDays");
            maxDataExtensionDays = namespaceAttrs.get("maxDataExtensionTimeInDays");
            String attrChangeTracking = namespaceAttrs.get("changeTracking");
            if (attrChangeTracking != null) {
                changeTracking = Boolean.parseBoolean(attrChangeTracking);
            }
            String attrCopyGrants = namespaceAttrs.get("copyGrants");
            if (attrCopyGrants != null) {
                copyGrants = Boolean.parseBoolean(attrCopyGrants);
            }
            String attrSchemaEvolution = namespaceAttrs.get("enableSchemaEvolution");
            if (attrSchemaEvolution != null) {
                enableSchemaEvolution = Boolean.parseBoolean(attrSchemaEvolution);
            }
            defaultDdlCollation = namespaceAttrs.get("defaultDdlCollation");
            
            // Process additional namespace attributes  
            String namespaceComment = namespaceAttrs.get("comment");
            if (namespaceComment != null) {
                comment = namespaceComment;
            }
            
            tag = namespaceAttrs.get("tag");
            stageFileFormat = namespaceAttrs.get("stageFileFormat");
            stageCopyOptions = namespaceAttrs.get("stageCopyOptions");
            
            // Clean up stored attributes
            SnowflakeNamespaceAttributeStorage.removeAttributes(statement.getTableName());
        } else {
            // Fall back to legacy approach of encoding in tablespace and remarks
            isTransient = statement.getTablespace() != null && 
                         statement.getTablespace().toLowerCase().contains("transient");
        
            if (statement.getRemarks() != null && !statement.getRemarks().trim().isEmpty()) {
                String remarks = statement.getRemarks().trim();
            
            // Parse special Snowflake options from remarks
            // Format: "CLUSTER_BY:col1,col2|DATA_RETENTION:7|COMMENT:actual comment"
            String[] options = remarks.split("\\|");
            for (String option : options) {
                if (option.startsWith("CLUSTER_BY:")) {
                    clusterByColumns = option.substring("CLUSTER_BY:".length()).trim();
                } else if (option.startsWith("DATA_RETENTION:")) {
                    dataRetentionDays = option.substring("DATA_RETENTION:".length()).trim();
                } else if (option.startsWith("MAX_DATA_EXTENSION:")) {
                    maxDataExtensionDays = option.substring("MAX_DATA_EXTENSION:".length()).trim();
                } else if (option.startsWith("COPY_GRANTS:")) {
                    copyGrants = Boolean.parseBoolean(option.substring("COPY_GRANTS:".length()).trim());
                } else if (option.startsWith("CHANGE_TRACKING:")) {
                    changeTracking = Boolean.parseBoolean(option.substring("CHANGE_TRACKING:".length()).trim());
                } else if (option.startsWith("ENABLE_SCHEMA_EVOLUTION:")) {
                    enableSchemaEvolution = Boolean.parseBoolean(option.substring("ENABLE_SCHEMA_EVOLUTION:".length()).trim());
                } else if (option.startsWith("STAGE_FILE_FORMAT:")) {
                    stageFileFormat = option.substring("STAGE_FILE_FORMAT:".length()).trim();
                } else if (option.startsWith("STAGE_COPY_OPTIONS:")) {
                    stageCopyOptions = option.substring("STAGE_COPY_OPTIONS:".length()).trim();
                } else if (option.startsWith("DEFAULT_DDL_COLLATION:")) {
                    defaultDdlCollation = option.substring("DEFAULT_DDL_COLLATION:".length()).trim();
                } else if (option.startsWith("TAG:")) {
                    tag = option.substring("TAG:".length()).trim();
                } else if (option.startsWith("COMMENT:")) {
                    comment = option.substring("COMMENT:".length()).trim();
                } else if (!option.contains(":")) {
                    // If no prefix, treat as a regular comment
                    comment = option;
                }
            }
            }
        }
        
        // Validate mutual exclusivity of table types
        int tableTypeCount = 0;
        if (isTransient) tableTypeCount++;
        if (isVolatile) tableTypeCount++;
        if (isTemporary || isLocalTemporary || isGlobalTemporary) tableTypeCount++;
        if (tableTypeCount > 1) {
            throw new RuntimeException("Only one table type (transient, volatile, temporary) can be specified");
        }
        
        // Rebuild the CREATE TABLE statement with Snowflake enhancements
        StringBuilder enhancedSql = new StringBuilder();
        
        // Find the position after "CREATE TABLE tablename"
        String createTablePrefix = "CREATE TABLE ";
        int tableNameEnd = originalSql.indexOf(" (", createTablePrefix.length());
        if (tableNameEnd == -1) {
            // Fallback to original SQL if we can't parse it
            return baseSql;
        }
        
        // Handle table type modifiers
        String tableTypeModifier = "";
        if (isTransient) {
            tableTypeModifier = "TRANSIENT ";
        } else if (isVolatile) {
            tableTypeModifier = "VOLATILE ";
        } else if (isTemporary) {
            tableTypeModifier = "TEMPORARY ";
        } else if (isLocalTemporary) {
            tableTypeModifier = "LOCAL TEMPORARY ";
        } else if (isGlobalTemporary) {
            tableTypeModifier = "GLOBAL TEMPORARY ";
        }
        
        if (!tableTypeModifier.isEmpty()) {
            // Insert table type modifier after CREATE
            enhancedSql.append("CREATE ").append(tableTypeModifier).append("TABLE");
            enhancedSql.append(originalSql.substring(createTablePrefix.length() - 1));
        } else {
            // Use original SQL as base
            enhancedSql.append(originalSql);
        }
        
        // Now add Snowflake-specific options before the final semicolon or at the end
        List<String> snowflakeOptions = new ArrayList<>();
        
        if (clusterByColumns != null && !clusterByColumns.isEmpty()) {
            snowflakeOptions.add("CLUSTER BY (" + clusterByColumns + ")");
        }
        
        // DATA_RETENTION_TIME_IN_DAYS is only valid for permanent tables
        boolean isPermanentTable = !isTransient && !isVolatile && !isTemporary && !isLocalTemporary && !isGlobalTemporary;
        if (isPermanentTable && dataRetentionDays != null && !dataRetentionDays.isEmpty()) {
            snowflakeOptions.add("DATA_RETENTION_TIME_IN_DAYS = " + dataRetentionDays);
        }
        
        // MAX_DATA_EXTENSION_TIME_IN_DAYS is only valid for permanent tables
        if (isPermanentTable && maxDataExtensionDays != null && !maxDataExtensionDays.isEmpty()) {
            snowflakeOptions.add("MAX_DATA_EXTENSION_TIME_IN_DAYS = " + maxDataExtensionDays);
        }
        
        if (copyGrants) {
            snowflakeOptions.add("COPY GRANTS");
        }
        
        if (changeTracking) {
            snowflakeOptions.add("CHANGE_TRACKING = TRUE");
        }
        
        if (enableSchemaEvolution) {
            snowflakeOptions.add("ENABLE_SCHEMA_EVOLUTION = TRUE");
        }
        
        if (stageFileFormat != null && !stageFileFormat.isEmpty()) {
            snowflakeOptions.add("STAGE_FILE_FORMAT = " + stageFileFormat);
        }
        
        if (stageCopyOptions != null && !stageCopyOptions.isEmpty()) {
            snowflakeOptions.add("STAGE_COPY_OPTIONS = " + stageCopyOptions);
        }
        
        if (defaultDdlCollation != null && !defaultDdlCollation.isEmpty()) {
            snowflakeOptions.add("DEFAULT_DDL_COLLATION = '" + defaultDdlCollation.replace("'", "''") + "'");
        }
        
        if (tag != null && !tag.isEmpty()) {
            snowflakeOptions.add("TAG (" + tag + ")");
        }
        
        if (comment != null && !comment.isEmpty()) {
            snowflakeOptions.add("COMMENT = '" + comment.replace("'", "''") + "'");
        }
        
        if (!snowflakeOptions.isEmpty()) {
            // Remove trailing semicolon if present
            String sqlStr = enhancedSql.toString();
            if (sqlStr.endsWith(";")) {
                sqlStr = sqlStr.substring(0, sqlStr.length() - 1);
            }
            
            // Add Snowflake options
            sqlStr += " " + String.join(" ", snowflakeOptions);
            enhancedSql = new StringBuilder(sqlStr);
        }
        
        return new Sql[]{new UnparsedSql(enhancedSql.toString())};
    }
}