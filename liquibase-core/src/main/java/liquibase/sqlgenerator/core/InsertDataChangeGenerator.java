package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.exception.ValidationErrors;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.InsertExecutablePreparedStatement;

/**
 * Dummy SQL generator for <code>InsertDataChange.ExecutableStatement</code><br>
 */
public class InsertDataChangeGenerator extends AbstractSqlGenerator<InsertExecutablePreparedStatement> {
    @Override
    public ValidationErrors validate(InsertExecutablePreparedStatement statement, ExecutionEnvironment env, ActionGeneratorChain chain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(InsertExecutablePreparedStatement statement, ExecutionEnvironment env, ActionGeneratorChain chain) {
        return new Action[0];
    }
}
