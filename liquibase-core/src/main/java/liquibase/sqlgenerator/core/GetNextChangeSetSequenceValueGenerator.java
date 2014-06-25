package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.core.GetNextChangeSetSequenceValueStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;

public class GetNextChangeSetSequenceValueGenerator extends AbstractSqlGenerator<GetNextChangeSetSequenceValueStatement> {

    @Override
    public ValidationErrors validate(GetNextChangeSetSequenceValueStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(GetNextChangeSetSequenceValueStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return StatementLogicFactory.getInstance().generateActions(new SelectFromDatabaseChangeLogStatement("MAX(ORDEREXECUTED)"), env);
    }
}
