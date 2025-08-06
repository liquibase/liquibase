package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AlterSequenceGenerator;
import liquibase.statement.core.AlterSequenceStatement;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;

import java.util.Map;

/**
 * Snowflake-specific AlterSequence SQL generator that supports namespace attributes
 * for Snowflake-specific sequence operations like setNoOrder.
 */
public class AlterSequenceGeneratorSnowflake extends AlterSequenceGenerator {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 1;
    }

    @Override
    public boolean supports(AlterSequenceStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(AlterSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = super.validate(statement, database, sqlGeneratorChain);
        
        // Get namespace attributes for validation
        Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getSequenceName());
        
        if (attributes != null && !attributes.isEmpty()) {
            // Validate that setNoOrder is boolean
            String setNoOrder = attributes.get("setNoOrder");
            if (setNoOrder != null && 
                !setNoOrder.equalsIgnoreCase("true") && 
                !setNoOrder.equalsIgnoreCase("false")) {
                validationErrors.addError("setNoOrder must be true or false");
            }
            
            // Validate that unsetComment is boolean
            String unsetComment = attributes.get("unsetComment");
            if (unsetComment != null && 
                !unsetComment.equalsIgnoreCase("true") && 
                !unsetComment.equalsIgnoreCase("false")) {
                validationErrors.addError("unsetComment must be true or false");
            }
            
            // Validate mutual exclusivity of setComment and unsetComment
            boolean hasSetComment = attributes.get("setComment") != null;
            boolean hasUnsetComment = "true".equalsIgnoreCase(attributes.get("unsetComment"));
            
            if (hasSetComment && hasUnsetComment) {
                validationErrors.addError("Cannot both set and unset comment in same operation");
            }
        }
        
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AlterSequenceStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Check for Snowflake-specific namespace attributes
        Map<String, String> attributes = SnowflakeNamespaceAttributeStorage.getAttributes(statement.getSequenceName());
        System.out.println("DEBUG: AlterSequenceGeneratorSnowflake - sequenceName: " + statement.getSequenceName() + ", attributes: " + attributes);
        
        // Check if we need to handle comment operations separately
        boolean hasUnsetComment = attributes != null && "true".equalsIgnoreCase(attributes.get("unsetComment"));
        
        if (hasUnsetComment) {
            // UNSET COMMENT is a separate SQL statement format
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("ALTER SEQUENCE ");
            sqlBuilder.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));
            sqlBuilder.append(" UNSET COMMENT");
            return new Sql[]{new UnparsedSql(sqlBuilder.toString(), getAffectedSequence(statement))};
        }
        
        // Otherwise, use SET format for all other operations
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("ALTER SEQUENCE ");
        sqlBuilder.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));
        
        // Collect all SET operations
        boolean hasSetOperations = false;
        StringBuilder setBuilder = new StringBuilder();
        
        // Add all standard sequence parameters
        if (statement.getIncrementBy() != null) {
            if (hasSetOperations) setBuilder.append(", ");
            setBuilder.append("INCREMENT BY ").append(statement.getIncrementBy());
            hasSetOperations = true;
        }
        
        if (statement.getMinValue() != null) {
            if (hasSetOperations) setBuilder.append(", ");
            setBuilder.append("MINVALUE ").append(statement.getMinValue());
            hasSetOperations = true;
        }
        
        if (statement.getMaxValue() != null) {
            if (hasSetOperations) setBuilder.append(", ");
            setBuilder.append("MAXVALUE ").append(statement.getMaxValue());
            hasSetOperations = true;
        }
        
        if (statement.getCacheSize() != null) {
            if (hasSetOperations) setBuilder.append(", ");
            setBuilder.append("CACHE ").append(statement.getCacheSize());
            hasSetOperations = true;
        }
        
        if (statement.getCycle() != null) {
            if (hasSetOperations) setBuilder.append(", ");
            if (statement.getCycle()) {
                setBuilder.append("CYCLE");
            } else {
                setBuilder.append("NO CYCLE");
            }
            hasSetOperations = true;
        }
        
        if (statement.getOrdered() != null) {
            if (hasSetOperations) setBuilder.append(", ");
            if (statement.getOrdered()) {
                setBuilder.append("ORDER");
            } else {
                setBuilder.append("NOORDER");
                
                // Add irreversibility warning for NOORDER operation
                System.out.println("⚠️  WARNING: IRREVERSIBLE OPERATION - ALTER SEQUENCE " + statement.getSequenceName() + 
                    " SET NOORDER cannot be undone. Once a sequence is changed to NOORDER, " +
                    "it cannot return to ORDER mode. This operation improves concurrency " +
                    "but permanently removes ordering guarantees.");
            }
            hasSetOperations = true;
        }
        
        // Check for Snowflake-specific namespace attributes
        if (attributes != null && !attributes.isEmpty()) {
            // Check for Snowflake-specific setNoOrder option
            boolean setNoOrder = "true".equalsIgnoreCase(attributes.get("setNoOrder"));
            if (setNoOrder) {
                if (hasSetOperations) setBuilder.append(", ");
                setBuilder.append("NOORDER");
                hasSetOperations = true;
                
                // Add irreversibility warning for NOORDER operation
                System.out.println("⚠️  WARNING: IRREVERSIBLE OPERATION - ALTER SEQUENCE " + statement.getSequenceName() + 
                    " SET NOORDER cannot be undone. Once a sequence is changed to NOORDER, " +
                    "it cannot return to ORDER mode. This operation improves concurrency " +
                    "but permanently removes ordering guarantees.");
            }
            
            // Check for setComment
            String setComment = attributes.get("setComment");
            if (setComment != null) {
                if (hasSetOperations) setBuilder.append(", ");
                setBuilder.append("COMMENT = '").append(setComment.replace("'", "''")).append("'");
                hasSetOperations = true;
            }
        }
        
        // If we have SET operations, add them
        if (hasSetOperations) {
            sqlBuilder.append(" SET ");
            sqlBuilder.append(setBuilder.toString());
        }
        
        return new Sql[]{new UnparsedSql(sqlBuilder.toString(), getAffectedSequence(statement))};
    }

    protected liquibase.structure.core.Sequence getAffectedSequence(AlterSequenceStatement statement) {
        return new liquibase.structure.core.Sequence(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName());
    }
}