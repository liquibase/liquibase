package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.RuntimeStatement;

public class RuntimeGenerator extends AbstractSqlGenerator<RuntimeStatement> {

    @Override
    public ValidationErrors validate(RuntimeStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(RuntimeStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        return statement.generate(env);
    }
}
