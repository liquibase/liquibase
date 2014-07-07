package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.exception.UnsupportedException;
import liquibase.statement.core.RawActionStatement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;

public class RuntimeGenerator extends AbstractSqlGenerator<RawActionStatement> {

    @Override
    public ValidationErrors validate(RawActionStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(RawActionStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        return statement.getActions();
    }
}
