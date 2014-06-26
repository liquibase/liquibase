package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.exception.UnsupportedException;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.InsertExecutablePreparedStatement;

/**
 * Dummy SQL generator for <code>InsertDataChange.ExecutableStatement</code><br>
 */
public class InsertDataChangeGenerator extends AbstractSqlGenerator<InsertExecutablePreparedStatement> {
    @Override
    public ValidationErrors validate(InsertExecutablePreparedStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(InsertExecutablePreparedStatement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        return new Action[0];
    }
}
