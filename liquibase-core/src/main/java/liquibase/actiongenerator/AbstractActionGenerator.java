package liquibase.actiongenerator;

import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutionOptions;
import liquibase.statement.SqlStatement;

public abstract class AbstractActionGenerator<StatementType extends SqlStatement> implements ActionGenerator<StatementType> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(StatementType statement, ExecutionOptions options) {
        return true;
    }

    @Override
    public ValidationErrors validate(StatementType statement, ExecutionOptions options, ActionGeneratorChain chain) {
        return chain.validate(statement, options);
    }

    @Override
    public Warnings warn(StatementType statementType, ExecutionOptions options, ActionGeneratorChain chain) {
        return chain.warn(statementType, options);
    }
}
