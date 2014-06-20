package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.actiongenerator.ActionGeneratorFactory;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.GetNextChangeSetSequenceValueStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;

public class GetNextChangeSetSequenceValueGenerator extends AbstractSqlGenerator<GetNextChangeSetSequenceValueStatement> {

    @Override
    public ValidationErrors validate(GetNextChangeSetSequenceValueStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new ValidationErrors();
    }

    @Override
    public Action[] generateActions(GetNextChangeSetSequenceValueStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        return ActionGeneratorFactory.getInstance().generateActions(new SelectFromDatabaseChangeLogStatement("MAX(ORDEREXECUTED)"), options);
    }
}
