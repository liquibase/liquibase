package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.core.RawSqlStatement;

public class RawSqlGenerator extends AbstractSqlGenerator<RawSqlStatement> {

    @Override
    public ValidationErrors validate(RawSqlStatement rawSqlStatement, ExecutionOptions options, ActionGeneratorChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sql", rawSqlStatement.getSql());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(RawSqlStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        return new Action[] {
           new UnparsedSql(statement.getSql(), statement.getEndDelimiter())
        };
    }
}
