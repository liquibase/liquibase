package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.snowflake.AlterSequenceStatementSnowflake;
import liquibase.Scope;
import liquibase.logging.Logger;

/**
 * SQL generator for ALTER SEQUENCE operations in Snowflake.
 * Supports rename, increment changes, ordering modifications, and comment management.
 */
public class AlterSequenceGeneratorSnowflake extends AbstractSqlGenerator<AlterSequenceStatementSnowflake> {
    
    private static final Logger logger = Scope.getCurrentScope().getLog(AlterSequenceGeneratorSnowflake.class);

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(AlterSequenceStatementSnowflake statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(AlterSequenceStatementSnowflake statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkRequiredField("sequenceName", statement.getSequenceName());

        // Validate that at least one alteration is specified
        if (statement.getNewSequenceName() == null && 
            statement.getIncrementBy() == null && 
            statement.getOrdered() == null && 
            statement.getComment() == null && 
            !Boolean.TRUE.equals(statement.getUnsetComment())) {
            validationErrors.addError("At least one alteration must be specified");
        }

        // Validate conflicting comment operations
        if (statement.getComment() != null && Boolean.TRUE.equals(statement.getUnsetComment())) {
            validationErrors.addError("Cannot both set comment and unset comment in the same operation");
        }

        // Validate increment value is non-zero
        if (statement.getIncrementBy() != null && statement.getIncrementBy().equals(java.math.BigInteger.ZERO)) {
            validationErrors.addError("Increment value cannot be zero");
        }

        return validationErrors;
    }

    @Override
    public Sql[] generateSql(AlterSequenceStatementSnowflake statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Check validation first
        ValidationErrors errors = validate(statement, database, sqlGeneratorChain);
        if (errors.hasErrors()) {
            throw new RuntimeException("Validation failed for AlterSequence: " + errors.toString());
        }

        // Handle RENAME operation separately
        if (statement.getNewSequenceName() != null) {
            return generateRenameSql(statement, database);
        }

        // Handle SET operations (increment, ordering, comment)
        return generateSetSql(statement, database);
    }

    private Sql[] generateRenameSql(AlterSequenceStatementSnowflake statement, Database database) {
        StringBuilder sql = new StringBuilder();
        
        sql.append("ALTER SEQUENCE ");
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        sql.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));
        sql.append(" RENAME TO ");
        sql.append(database.escapeObjectName(statement.getNewSequenceName(), liquibase.structure.core.Sequence.class));

        return new Sql[]{new UnparsedSql(sql.toString(), getAffectedSequence(statement))};
    }

    private Sql[] generateSetSql(AlterSequenceStatementSnowflake statement, Database database) {
        StringBuilder sql = new StringBuilder();
        
        sql.append("ALTER SEQUENCE ");
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        sql.append(database.escapeSequenceName(statement.getCatalogName(), statement.getSchemaName(), statement.getSequenceName()));

        // Handle UNSET COMMENT separately
        if (Boolean.TRUE.equals(statement.getUnsetComment())) {
            sql.append(" UNSET COMMENT");
            return new Sql[]{new UnparsedSql(sql.toString(), getAffectedSequence(statement))};
        }

        // Handle SET operations
        sql.append(" SET");
        boolean hasSetClause = false;

        // Add INCREMENT BY
        if (statement.getIncrementBy() != null) {
            sql.append(" INCREMENT BY ").append(statement.getIncrementBy());
            hasSetClause = true;
        }

        // Add ORDER/NOORDER
        if (statement.getOrdered() != null) {
            if (statement.getOrdered()) {
                sql.append(" ORDER");
            } else {
                sql.append(" NOORDER");
                // ⚠️ CRITICAL WARNING: NOORDER is irreversible in Snowflake
                logger.warning("CRITICAL: ALTER SEQUENCE SET NOORDER is IRREVERSIBLE - " +
                    "sequence '" + statement.getSequenceName() + "' cannot be changed back to ORDER behavior. " +
                    "This provides better performance but eliminates ordering guarantees permanently.");
            }
            hasSetClause = true;
        }

        // Add COMMENT
        if (statement.getComment() != null) {
            sql.append(" COMMENT = '").append(statement.getComment().replace("'", "''")).append("'");
            hasSetClause = true;
        }

        // Validation should have caught this, but double-check
        if (!hasSetClause) {
            throw new RuntimeException("No SET clause generated for ALTER SEQUENCE");
        }

        return new Sql[]{new UnparsedSql(sql.toString(), getAffectedSequence(statement))};
    }

    private liquibase.structure.core.Sequence getAffectedSequence(AlterSequenceStatementSnowflake statement) {
        return new liquibase.structure.core.Sequence()
            .setName(statement.getSequenceName())
            .setSchema(statement.getCatalogName(), statement.getSchemaName());
    }
}