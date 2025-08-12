package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.AlterStageStatement;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtil;

import java.util.Map;

/**
 * SQL Generator for ALTER STAGE statements in Snowflake.
 * Handles RENAME, SET, UNSET, and REFRESH operations.
 */
public class AlterStageGeneratorSnowflake extends AbstractSqlGenerator<AlterStageStatement> {

    @Override
    public boolean supports(AlterStageStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(AlterStageStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        
        if (StringUtil.isEmpty(statement.getStageName()) || 
            (statement.getStageName() != null && statement.getStageName().trim().isEmpty())) {
            validationErrors.addError("stageName is required for ALTER STAGE");
        }
        
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AlterStageStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder();
        Map<String, String> properties = statement.getObjectProperties();
        
        // Start with ALTER STAGE
        sql.append("ALTER STAGE");
        
        // IF EXISTS clause
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append(" IF EXISTS");
        }
        
        // Stage name with schema qualification
        sql.append(" ");
        if (statement.getCatalogName() != null && statement.getSchemaName() != null) {
            sql.append(database.escapeObjectName(statement.getCatalogName(), Schema.class))
               .append(".")
               .append(database.escapeObjectName(statement.getSchemaName(), Schema.class))
               .append(".");
        } else if (statement.getSchemaName() != null) {
            sql.append(database.escapeObjectName(statement.getSchemaName(), Schema.class))
               .append(".");
        }
        sql.append(database.escapeObjectName(statement.getStageName(), Schema.class));
        
        // Determine operation type and generate appropriate SQL
        if (statement.isRenameOperation()) {
            sql.append(" RENAME TO ").append(database.escapeObjectName(statement.getRenameTo(), Schema.class));
        } else if (statement.hasRefreshOperations()) {
            sql.append(" REFRESH");
            String subPath = properties.get("refreshSubpath");
            if (subPath != null) {
                sql.append(" SUBPATH = '").append(subPath).append("'");
            }
        } else if (statement.hasUnsetOperations()) {
            sql.append(" UNSET ");
            appendUnsetOperations(sql, properties);
        } else if (statement.hasSetOperations() || statement.hasTagOperations()) {
            sql.append(" SET ");
            appendSetOperations(sql, properties, database);
        }
        
        return new Sql[]{new UnparsedSql(sql.toString(), getAffectedTable(statement))};
    }
    
    private void appendSetOperations(StringBuilder sql, Map<String, String> properties, Database database) {
        boolean first = true;
        
        // Handle regular SET operations - check for actual property names
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            // Skip non-SET properties
            if (key.startsWith("unset") || key.equals("renameTo") || key.equals("refreshDirectory") || 
                key.equals("refreshSubpath") || key.equals("catalogName") || key.equals("schemaName") || 
                key.equals("ifExists") || key.equals("tagName") || key.equals("tagValue")) {
                continue;
            }
            
            if (!first) sql.append(", ");
            
            if ("url".equals(key)) {
                sql.append("URL = '").append(value).append("'");
            } else if ("storageIntegration".equals(key)) {
                sql.append("STORAGE_INTEGRATION = ").append(database.escapeObjectName(value, Schema.class));
            } else if ("comment".equals(key)) {
                sql.append("COMMENT = '").append(value.replace("'", "''")).append("'");
            } else if ("encryption".equals(key)) {
                sql.append("ENCRYPTION = (TYPE = '").append(value).append("')");
            } else if ("fileFormat".equals(key)) {
                sql.append("FILE_FORMAT = (TYPE = ").append(value).append(")");
            } else if ("directoryEnable".equals(key)) {
                sql.append("DIRECTORY = (ENABLE = ").append("true".equals(value) ? "TRUE" : "FALSE").append(")");
            } else if (key.startsWith("aws") || key.startsWith("gcs") || key.startsWith("azure")) {
                // Handle cloud credentials - simplified for now
                sql.append(convertCamelCaseToSnakeCase(key)).append(" = '").append(value).append("'");
            } else {
                // Generic property handling
                String propertyName = convertCamelCaseToSnakeCase(key);
                sql.append(propertyName).append(" = '").append(value).append("'");
            }
            
            first = false;
        }
        
        // Handle TAG operations
        String tagName = properties.get("setTagName");
        String tagValue = properties.get("setTagValue");
        if (tagName != null && tagValue != null) {
            if (!first) sql.append(", ");
            sql.append("TAG (").append(tagName).append(" = '").append(tagValue).append("')");
        }
    }
    
    private void appendUnsetOperations(StringBuilder sql, Map<String, String> properties) {
        boolean first = true;
        
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            
            if (key.startsWith("unset")) {
                if (!first) sql.append(", ");
                
                if ("unsetUrl".equals(key)) {
                    sql.append("URL");
                } else if ("unsetStorageIntegration".equals(key)) {
                    sql.append("STORAGE_INTEGRATION");
                } else if ("unsetCredentials".equals(key)) {
                    sql.append("CREDENTIALS");
                } else if ("unsetEncryption".equals(key)) {
                    sql.append("ENCRYPTION");
                } else if ("unsetFileFormat".equals(key)) {
                    sql.append("FILE_FORMAT");
                } else if ("unsetComment".equals(key)) {
                    sql.append("COMMENT");
                } else if ("unsetTagName".equals(key)) {
                    sql.append("TAG (").append(entry.getValue()).append(")");
                }
                
                first = false;
            }
        }
    }
    
    private String convertCamelCaseToSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }
    
    protected String getAffectedTable(AlterStageStatement statement) {
        return statement.getStageName();
    }
}