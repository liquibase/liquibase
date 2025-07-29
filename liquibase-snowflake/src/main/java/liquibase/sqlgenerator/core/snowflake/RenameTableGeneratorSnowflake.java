package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.RenameTableGenerator;
import liquibase.statement.core.RenameTableStatement;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;

import java.util.Map;

/**
 * Snowflake-specific RenameTable SQL generator that supports namespace attributes
 * for preserving Snowflake-specific table attributes during rename operations.
 */
public class RenameTableGeneratorSnowflake extends RenameTableGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 1;
    }

    @Override
    public boolean supports(RenameTableStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(RenameTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(statement, database, sqlGeneratorChain);
        
        // Get namespace attributes for validation
        Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getOldTableName());
        
        if (attributes != null && !attributes.isEmpty()) {
            // Validate that preserveClusterBy is boolean
            String preserveClusterBy = attributes.get("preserveClusterBy");
            if (preserveClusterBy != null && 
                !preserveClusterBy.equalsIgnoreCase("true") && 
                !preserveClusterBy.equalsIgnoreCase("false")) {
                validationErrors.addError("preserveClusterBy must be true or false");
            }
            
            // Validate that preserveAttributes is boolean
            String preserveAttributes = attributes.get("preserveAttributes");
            if (preserveAttributes != null && 
                !preserveAttributes.equalsIgnoreCase("true") && 
                !preserveAttributes.equalsIgnoreCase("false")) {
                validationErrors.addError("preserveAttributes must be true or false");
            }
        }
        
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RenameTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Get the standard RENAME TABLE SQL first
        Sql[] baseSql = super.generateSql(statement, database, sqlGeneratorChain);
        
        if (baseSql.length == 0) {
            return baseSql;
        }
        
        // Get namespace attributes
        Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getOldTableName());
        
        if (attributes == null || attributes.isEmpty()) {
            // No namespace attributes, return standard SQL
            return baseSql;
        }
        
        // Check for Snowflake-specific preservation options
        boolean preserveClusterBy = "true".equals(attributes.get("preserveClusterBy"));
        boolean preserveAttributes = "true".equals(attributes.get("preserveAttributes"));
        
        if (!preserveClusterBy && !preserveAttributes) {
            // No preservation options, return standard SQL
            return baseSql;
        }
        
        // Build enhanced SQL with preservation options
        StringBuilder sqlBuilder = new StringBuilder();
        
        // Start with the basic rename
        String originalSql = baseSql[0].toSql();
        sqlBuilder.append(originalSql);
        
        // Add preservation comments for documentation
        if (preserveClusterBy || preserveAttributes) {
            sqlBuilder.append(";\n-- Snowflake table attributes preserved during rename");
            
            if (preserveClusterBy) {
                sqlBuilder.append("\n-- Clustering key preserved automatically by Snowflake");
            }
            
            if (preserveAttributes) {
                sqlBuilder.append("\n-- Table attributes (retention, tracking, etc.) preserved automatically by Snowflake");
            }
        }
        
        return new Sql[]{new UnparsedSql(sqlBuilder.toString(), getAffectedTable(statement))};
    }

    protected liquibase.structure.core.Table getAffectedTable(RenameTableStatement statement) {
        return new liquibase.structure.core.Table(statement.getCatalogName(), statement.getSchemaName(), statement.getNewTableName());
    }
}