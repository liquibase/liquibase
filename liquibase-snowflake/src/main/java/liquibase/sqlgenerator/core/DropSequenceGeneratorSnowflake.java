package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.snowflake.DropSequenceStatementSnowflake;
import liquibase.Scope;
import liquibase.logging.Logger;

/**
 * SQL generator for DROP SEQUENCE operations in Snowflake.
 * Supports IF EXISTS, CASCADE, and RESTRICT options.
 */
public class DropSequenceGeneratorSnowflake extends AbstractSqlGenerator<DropSequenceStatementSnowflake> {
    
    private static final Logger logger = Scope.getCurrentScope().getLog(DropSequenceGeneratorSnowflake.class);

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(DropSequenceStatementSnowflake statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(DropSequenceStatementSnowflake statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("sequenceName", statement.getSequenceName());

        // Validate mutually exclusive cascade/restrict options
        if (Boolean.TRUE.equals(statement.getCascade()) && Boolean.TRUE.equals(statement.getRestrict())) {
            validationErrors.addError("Cannot use both CASCADE and RESTRICT options");
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropSequenceStatementSnowflake statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Check validation first
        ValidationErrors errors = validate(statement, database, sqlGeneratorChain);
        if (errors.hasErrors()) {
            throw new RuntimeException("Validation failed for DropSequence: " + errors.toString());
        }

        StringBuilder sql = new StringBuilder();
        
        sql.append("DROP SEQUENCE ");
        
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        
        sql.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));
        
        // Add CASCADE or RESTRICT if specified
        if (Boolean.TRUE.equals(statement.getCascade())) {
            sql.append(" CASCADE");
            // ℹ️ INFO: CASCADE is non-functional in Snowflake
            logger.fine("CASCADE keyword is non-functional in Snowflake - " +
                "sequence '" + statement.getSequenceName() + "' will be dropped but dependent objects will NOT be affected. " +
                "This provides syntax consistency only. Manual dependency management required.");
        } else if (Boolean.TRUE.equals(statement.getRestrict())) {
            sql.append(" RESTRICT");
            // ℹ️ INFO: RESTRICT is non-functional in Snowflake
            logger.fine("RESTRICT keyword is non-functional in Snowflake - " +
                "sequence '" + statement.getSequenceName() + "' will be dropped without dependency checking. " +
                "This provides syntax consistency only. Manual dependency verification recommended.");
        }

        return new Sql[]{new UnparsedSql(sql.toString(), getAffectedSequence(statement))};
    }

    private liquibase.structure.core.Sequence getAffectedSequence(DropSequenceStatementSnowflake statement) {
        return new liquibase.structure.core.Sequence()
            .setName(statement.getSequenceName())
            .setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}