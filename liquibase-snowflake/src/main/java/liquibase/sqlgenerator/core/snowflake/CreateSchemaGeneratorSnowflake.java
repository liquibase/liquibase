package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.CreateSchemaStatement;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class CreateSchemaGeneratorSnowflake extends AbstractSqlGenerator<CreateSchemaStatement> {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 10; // Higher priority to ensure this generator is used
    }

    @Override
    public boolean supports(CreateSchemaStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(CreateSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        errors.checkRequiredField("schemaName", statement.getSchemaName());
        
        // Validate that orReplace and ifNotExists are not both set
        if (Boolean.TRUE.equals(statement.getOrReplace()) && Boolean.TRUE.equals(statement.getIfNotExists())) {
            errors.addError("Cannot use both OR REPLACE and IF NOT EXISTS");
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(CreateSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        // Check validation first - prevent generating invalid SQL
        ValidationErrors errors = validate(statement, database, sqlGeneratorChain);
        if (errors.hasErrors()) {
            throw new RuntimeException("Validation failed for CreateSchema: " + errors.toString());
        }
        
        StringBuilder sql = new StringBuilder("CREATE ");
        
        // Add OR REPLACE if specified
        if (Boolean.TRUE.equals(statement.getOrReplace())) {
            sql.append("OR REPLACE ");
        }
        
        // Add TRANSIENT before SCHEMA (correct Snowflake syntax)
        if (statement.getTransient() != null && statement.getTransient()) {
            sql.append("TRANSIENT ");
        }
        
        sql.append("SCHEMA ");
        
        // IF NOT EXISTS must come after SCHEMA  
        if (statement.getIfNotExists() != null && statement.getIfNotExists()) {
            sql.append("IF NOT EXISTS ");
        }
        
        sql.append(database.escapeObjectName(statement.getSchemaName(), liquibase.structure.core.Schema.class));
        
        List<String> options = new ArrayList<>();
        
        if (statement.getManaged() != null && statement.getManaged()) {
            options.add("WITH MANAGED ACCESS");
        }
        
        if (statement.getDataRetentionTimeInDays() != null) {
            options.add("DATA_RETENTION_TIME_IN_DAYS = " + statement.getDataRetentionTimeInDays());
        }
        
        if (statement.getMaxDataExtensionTimeInDays() != null) {
            options.add("MAX_DATA_EXTENSION_TIME_IN_DAYS = " + statement.getMaxDataExtensionTimeInDays());
        }
        
        if (statement.getDefaultDdlCollation() != null) {
            options.add("DEFAULT_DDL_COLLATION = '" + statement.getDefaultDdlCollation() + "'");
        }
        
        if (statement.getPipeExecutionPaused() != null) {
            options.add("PIPE_EXECUTION_PAUSED = " + statement.getPipeExecutionPaused());
        }
        
        if (statement.getComment() != null) {
            options.add("COMMENT = '" + statement.getComment().replace("'", "''") + "'");
        }
        
        if (!options.isEmpty()) {
            sql.append(" ");
            sql.append(String.join(" ", options));
        }

        return new Sql[]{new UnparsedSql(sql.toString())};
    }
}