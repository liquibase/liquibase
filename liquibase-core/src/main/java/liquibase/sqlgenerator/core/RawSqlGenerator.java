package liquibase.sqlgenerator.core;

import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.action.Sql;
import liquibase.action.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RawSqlStatement;

public class RawSqlGenerator extends AbstractSqlGenerator<RawSqlStatement> {

    @Override
    public ValidationErrors validate(RawSqlStatement rawSqlStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sql", rawSqlStatement.getSql());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(RawSqlStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new Sql[] {
           new UnparsedSql(statement.getSql(), statement.getEndDelimiter())
        };
    }
}
