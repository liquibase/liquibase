package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;

import java.util.Map;

/**
 * Snowflake-specific CreateSequence SQL generator that supports namespace attributes
 * for ORDER/NOORDER sequence options.
 */
public class CreateSequenceGeneratorSnowflake extends liquibase.sqlgenerator.core.CreateSequenceGeneratorSnowflake {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 10; // Higher priority than the base Snowflake generator
    }

    @Override
    public boolean supports(CreateSequenceStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(statement, database, sqlGeneratorChain);
        
        // Get namespace attributes for validation
        Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getSequenceName());
        
        if (attributes != null && !attributes.isEmpty()) {
            // Validate mutual exclusivity of order and noOrder
            boolean order = "true".equals(attributes.get("order"));
            boolean noOrder = "true".equals(attributes.get("noOrder"));
            
            if (order && noOrder) {
                validationErrors.addError("Cannot specify both order and noOrder attributes on createSequence");
            }
        }
        
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(CreateSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // First get the standard SQL from parent generator
        Sql[] baseSql = super.generateSql(statement, database, sqlGeneratorChain);
        
        if (baseSql.length == 0) {
            return baseSql;
        }
        
        // Get namespace attributes
        Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getSequenceName());
        
        if (attributes == null || attributes.isEmpty()) {
            // No namespace attributes, return standard SQL
            return baseSql;
        }
        
        // Check for ORDER/NOORDER attributes
        boolean order = "true".equals(attributes.get("order"));
        boolean noOrder = "true".equals(attributes.get("noOrder"));
        
        // Clean up stored attributes
        SnowflakeNamespaceAttributeStorage.removeAttributes(statement.getSequenceName());
        
        if (!order && !noOrder) {
            // No ORDER-related attributes, return standard SQL
            return baseSql;
        }
        
        // Check if the SQL already contains ORDER/NOORDER
        String originalSql = baseSql[0].toSql();
        if (originalSql.contains(" ORDER") || originalSql.contains(" NOORDER")) {
            // Already has ORDER clause from statement, namespace attributes take precedence
            // Remove existing ORDER/NOORDER
            originalSql = originalSql.replaceAll(" (NO)?ORDER", "");
        }
        
        // Add the namespace-specified ORDER/NOORDER
        StringBuilder enhancedSql = new StringBuilder(originalSql);
        if (order) {
            enhancedSql.append(" ORDER");
        } else if (noOrder) {
            enhancedSql.append(" NOORDER");
        }
        
        return new Sql[]{new UnparsedSql(enhancedSql.toString(), getAffectedSequence(statement))};
    }
}