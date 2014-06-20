package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.RuntimeStatement;

public class RuntimeGenerator extends AbstractSqlGenerator<RuntimeStatement> {

    @Override
    public ValidationErrors validate(RuntimeStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(RuntimeStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        return statement.generate(options);
    }
}
