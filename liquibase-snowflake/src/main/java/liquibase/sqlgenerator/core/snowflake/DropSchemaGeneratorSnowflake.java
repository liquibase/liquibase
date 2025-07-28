package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.snowflake.DropSchemaStatement;
import liquibase.structure.core.Schema;

public class DropSchemaGeneratorSnowflake extends AbstractSqlGenerator<DropSchemaStatement> {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(DropSchemaStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(DropSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("schemaName", statement.getSchemaName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropSchemaStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        StringBuilder sql = new StringBuilder("DROP SCHEMA ");
        
        if (Boolean.TRUE.equals(statement.getIfExists())) {
            sql.append("IF EXISTS ");
        }
        
        if (statement.getDatabaseName() != null) {
            sql.append(database.escapeObjectName(statement.getDatabaseName(), null)).append(".");
        }
        sql.append(database.escapeObjectName(statement.getSchemaName(), Schema.class));
        
        if (Boolean.TRUE.equals(statement.getCascade())) {
            sql.append(" CASCADE");
        }
        
        return new Sql[]{new UnparsedSql(sql.toString())};
    }
}