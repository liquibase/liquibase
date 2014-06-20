package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.UpdateExecutablePreparedStatement;

/**
 * Dummy SQL generator for <code>UpdateDataChange.ExecutableStatement</code><br>
 */
public class UpdateDataChangeGenerator extends AbstractSqlGenerator<UpdateExecutablePreparedStatement> {
    @Override
    public ValidationErrors validate(UpdateExecutablePreparedStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(UpdateExecutablePreparedStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        return new Action[0];
    }
}
