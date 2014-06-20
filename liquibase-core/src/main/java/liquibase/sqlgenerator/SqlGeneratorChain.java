package liquibase.sqlgenerator;

import liquibase.action.Action;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.SqlStatement;

public class SqlGeneratorChain {

    private ActionGeneratorChain delegate;

    public SqlGeneratorChain(ActionGeneratorChain actionGeneratorChain) {
        this.delegate = actionGeneratorChain;
    }

    public Action[] generateSql(SqlStatement statement, ExecutionOptions options) {
        return delegate.generateActions(statement, options);
    }

    public Warnings warn(SqlStatement statement, ExecutionOptions options) {
        return delegate.warn(statement, options);
    }

    public ValidationErrors validate(SqlStatement statement, ExecutionOptions options) {
        return delegate.validate(statement, options);
    }
}
