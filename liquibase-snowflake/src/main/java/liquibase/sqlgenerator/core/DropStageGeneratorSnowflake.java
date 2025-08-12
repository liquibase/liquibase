package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.DropStageStatement;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtil;

/**
 * SQL Generator for DROP STAGE statements in Snowflake.
 * Simple implementation for straightforward drop operations.
 */
public class DropStageGeneratorSnowflake extends AbstractSqlGenerator<DropStageStatement> {

    @Override
    public boolean supports(DropStageStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(DropStageStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        
        if (StringUtil.isEmpty(statement.getStageName()) || 
            (statement.getStageName() != null && statement.getStageName().trim().isEmpty())) {
            validationErrors.addError("stageName is required for DROP STAGE");
        }
        
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropStageStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder();
        
        // Start with DROP STAGE
        sql.append("DROP STAGE");
        
        // IF EXISTS clause
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append(" IF EXISTS");
        }
        
        // Stage name with schema qualification
        sql.append(" ");
        if (!StringUtil.isEmpty(statement.getCatalogName()) && !StringUtil.isEmpty(statement.getSchemaName())) {
            sql.append(database.escapeObjectName(statement.getCatalogName(), Schema.class))
               .append(".")
               .append(database.escapeObjectName(statement.getSchemaName(), Schema.class))
               .append(".");
        } else if (!StringUtil.isEmpty(statement.getSchemaName())) {
            sql.append(database.escapeObjectName(statement.getSchemaName(), Schema.class))
               .append(".");
        }
        sql.append(database.escapeObjectName(statement.getStageName(), Schema.class));
        
        return new Sql[]{new UnparsedSql(sql.toString(), getAffectedTable(statement))};
    }
    
    protected String getAffectedTable(DropStageStatement statement) {
        return statement.getStageName();
    }
}