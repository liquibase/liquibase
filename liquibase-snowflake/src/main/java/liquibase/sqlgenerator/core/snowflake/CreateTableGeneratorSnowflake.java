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
        // First get the standard CREATE TABLE SQL
        Sql[] baseSql = super.generateSql(statement, database, sqlGeneratorChain);
        
        if (baseSql.length == 0) {
            return baseSql;
        }
        
        // Enhance the CREATE TABLE statement with Snowflake-specific features
        String originalSql = baseSql[0].toSql();
        
        // Initialize variables
        boolean isTransient = false;
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
        
        // Use the approach of encoding in tablespace and remarks
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
        
        // Rebuild the CREATE TABLE statement with Snowflake enhancements
        StringBuilder enhancedSql = new StringBuilder();
        
        // Find the position after "CREATE TABLE tablename"
        String createTablePrefix = "CREATE TABLE ";
        int tableNameEnd = originalSql.indexOf(" (", createTablePrefix.length());
        if (tableNameEnd == -1) {
            // Fallback to original SQL if we can't parse it
            return baseSql;
        }
        
        // Add CREATE TABLE part
        enhancedSql.append(originalSql, 0, tableNameEnd);
        
        // Add column definitions and constraints
        enhancedSql.append(originalSql.substring(tableNameEnd));
        
        // Now add Snowflake-specific options before the final semicolon or at the end
        List<String> snowflakeOptions = new ArrayList<>();
        
        if (isTransient) {
            // For transient tables, we need to insert TRANSIENT after CREATE but before TABLE
            String modifiedSql = originalSql.replaceFirst("CREATE TABLE", "CREATE TRANSIENT TABLE");
            enhancedSql = new StringBuilder(modifiedSql);
        }
        
        if (clusterByColumns != null && !clusterByColumns.isEmpty()) {
            snowflakeOptions.add("CLUSTER BY (" + clusterByColumns + ")");
        }
        
        // DATA_RETENTION_TIME_IN_DAYS is only valid for permanent tables, not transient tables
        if (!isTransient && dataRetentionDays != null && !dataRetentionDays.isEmpty()) {
            snowflakeOptions.add("DATA_RETENTION_TIME_IN_DAYS = " + dataRetentionDays);
        }
        
        // MAX_DATA_EXTENSION_TIME_IN_DAYS is only valid for permanent tables, not transient tables
        if (!isTransient && maxDataExtensionDays != null && !maxDataExtensionDays.isEmpty()) {
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