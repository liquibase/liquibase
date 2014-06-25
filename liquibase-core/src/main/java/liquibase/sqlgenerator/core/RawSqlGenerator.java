package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.RawSqlStatement;

public class RawSqlGenerator extends AbstractSqlGenerator<RawSqlStatement> {

    @Override
    public ValidationErrors validate(RawSqlStatement rawSqlStatement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("sql", rawSqlStatement.getSql());
        return validationErrors;
    }

    @Override
    public Action[] generateActions(RawSqlStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new Action[] {
           new UnparsedSql(statement.getSql(), statement.getEndDelimiter())
        };
    }
}
