package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.DropSequenceGenerator;
import liquibase.statement.core.DropSequenceStatement;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;

import java.util.Map;

/**
 * Snowflake-specific DropSequence SQL generator that supports namespace attributes
 * for Snowflake-specific sequence drop operations like CASCADE and RESTRICT.
 */
public class DropSequenceGeneratorSnowflake extends DropSequenceGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 1;
    }

    @Override
    public boolean supports(DropSequenceStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(DropSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(statement, database, sqlGeneratorChain);
        
        // Get namespace attributes for validation
        Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getSequenceName());
        
        if (attributes != null && !attributes.isEmpty()) {
            // Validate that cascade and restrict are not both set
            String cascade = attributes.get("cascade");
            String restrict = attributes.get("restrict");
            
            if ("true".equals(cascade) && "true".equals(restrict)) {
                validationErrors.addError("Cannot use both cascade and restrict options");
            }
            
            // Validate that cascade is boolean if present
            if (cascade != null && 
                !cascade.equalsIgnoreCase("true") && 
                !cascade.equalsIgnoreCase("false")) {
                validationErrors.addError("cascade must be true or false");
            }
            
            // Validate that restrict is boolean if present
            if (restrict != null && 
                !restrict.equalsIgnoreCase("true") && 
                !restrict.equalsIgnoreCase("false")) {
                validationErrors.addError("restrict must be true or false");
            }
        }
        
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Get the standard DROP SEQUENCE SQL first
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
        
        // Check for Snowflake-specific cascade/restrict options
        boolean cascade = "true".equals(attributes.get("cascade"));
        boolean restrict = "true".equals(attributes.get("restrict"));
        
        if (!cascade && !restrict) {
            // No Snowflake-specific options, return standard SQL
            return baseSql;
        }
        
        // Build enhanced SQL with cascade/restrict
        String originalSql = baseSql[0].toSql();
        String enhancedSql = originalSql;
        
        // Add CASCADE or RESTRICT option
        if (cascade) {
            enhancedSql = enhancedSql + " CASCADE";
        } else if (restrict) {
            enhancedSql = enhancedSql + " RESTRICT";
        }
        
        // Don't clean up stored attributes - let them persist for the duration of the change execution
        // SnowflakeNamespaceAttributeStorage.removeAttributes(statement.getSequenceName());
        
        return new Sql[]{new UnparsedSql(enhancedSql, getAffectedSequence(statement))};
    }

    protected liquibase.structure.core.Sequence getAffectedSequence(DropSequenceStatement statement) {
        return new liquibase.structure.core.Sequence(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName());
    }
}