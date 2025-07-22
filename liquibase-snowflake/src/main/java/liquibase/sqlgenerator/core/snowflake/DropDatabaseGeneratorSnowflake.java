package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.DropDatabaseStatement;
import liquibase.structure.core.Table;

public class DropDatabaseGeneratorSnowflake extends AbstractSqlGenerator<DropDatabaseStatement> {

    @Override
    public boolean supports(DropDatabaseStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(DropDatabaseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = new ValidationErrors();
        
        if (statement.getDatabaseName() == null || statement.getDatabaseName().trim().isEmpty()) {
            errors.addError("databaseName is required");
        }
        
        if (statement.getCascade() != null && statement.getCascade() && 
            statement.getRestrict() != null && statement.getRestrict()) {
            errors.addError("Cannot specify both CASCADE and RESTRICT");
        }
        
        return errors;
    }

    @Override
    public Sql[] generateSql(DropDatabaseStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder("DROP DATABASE ");
        
        if (statement.getIfExists() != null && statement.getIfExists()) {
            sql.append("IF EXISTS ");
        }
        
        sql.append(database.escapeObjectName(statement.getDatabaseName(), Table.class));
        
        if (statement.getCascade() != null && statement.getCascade()) {
            sql.append(" CASCADE");
        } else if (statement.getRestrict() != null && statement.getRestrict()) {
            sql.append(" RESTRICT");
        }

        return new Sql[]{new UnparsedSql(sql.toString())};
    }
}