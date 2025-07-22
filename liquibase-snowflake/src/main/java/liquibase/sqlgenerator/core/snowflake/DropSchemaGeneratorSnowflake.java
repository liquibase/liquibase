package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.DropSchemaStatement;
import liquibase.structure.core.Table;

public class DropSchemaGeneratorSnowflake extends AbstractSqlGenerator<DropSchemaStatement> {

    @Override
    public boolean supports(DropSchemaStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(DropSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        if (statement.getSchemaName() == null || statement.getSchemaName().trim().isEmpty()) {
            errors.addError("schemaName is required");
        }
        
        if (statement.getCascade() != null && statement.getCascade() && 
            statement.getRestrict() != null && statement.getRestrict()) {
            errors.addError("Cannot specify both CASCADE and RESTRICT");
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(DropSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder("DROP SCHEMA ");
        
        if (statement.getIfExists() != null && statement.getIfExists()) {
            sql.append("IF EXISTS ");
        }
        
        sql.append(database.escapeObjectName(statement.getSchemaName(), Table.class));
        
        if (statement.getCascade() != null && statement.getCascade()) {
            sql.append(" CASCADE");
        } else if (statement.getRestrict() != null && statement.getRestrict()) {
            sql.append(" RESTRICT");
        }

        return new Sql[]{new UnparsedSql(sql.toString())};
    }
}