package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.DropTableGenerator;
import liquibase.statement.core.DropTableStatement;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;

import java.util.Map;

/**
 * Snowflake-specific DropTable SQL generator that supports namespace attributes
 * for Snowflake-specific drop options like CASCADE and RESTRICT.
 */
public class DropTableGeneratorSnowflake extends DropTableGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 1;
    }

    @Override
    public boolean supports(DropTableStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(DropTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(statement, database, sqlGeneratorChain);
        
        // Get namespace attributes for validation
        Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getTableName());
        
        if (attributes != null && !attributes.isEmpty()) {
            // Validate mutual exclusivity of CASCADE and RESTRICT
            boolean cascade = "true".equals(attributes.get("cascade"));
            boolean restrict = "true".equals(attributes.get("restrict"));
            
            if (cascade && restrict) {
                validationErrors.addError("Cannot specify both cascade and restrict options on dropTable");
            }
        }
        
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropTableStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Get the standard DROP TABLE SQL first
        Sql[] baseSql = super.generateSql(statement, database, sqlGeneratorChain);
        
        if (baseSql.length == 0) {
            return baseSql;
        }
        
        // Get namespace attributes
        Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getTableName());
        
        if (attributes == null || attributes.isEmpty()) {
            // No namespace attributes, return standard SQL
            return baseSql;
        }
        
        // Check for Snowflake-specific options
        boolean cascade = "true".equals(attributes.get("cascade"));
        boolean restrict = "true".equals(attributes.get("restrict"));
        
        if (!cascade && !restrict) {
            // No Snowflake-specific options, return standard SQL
            return baseSql;
        }
        
        // Enhance the DROP TABLE statement with Snowflake options
        String originalSql = baseSql[0].toSql();
        String enhancedSql = originalSql;
        
        if (cascade && !originalSql.contains("CASCADE")) {
            enhancedSql = enhancedSql + " CASCADE";
        } else if (restrict && !originalSql.contains("RESTRICT")) {
            enhancedSql = enhancedSql + " RESTRICT";
        }
        
        return new Sql[]{new UnparsedSql(enhancedSql, getAffectedTable(statement))};
    }

    protected liquibase.structure.core.Table getAffectedTable(DropTableStatement statement) {
        return new liquibase.structure.core.Table(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
    }
}