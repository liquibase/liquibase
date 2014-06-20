package liquibase.actiongenerator;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutionOptions;
import liquibase.servicelocator.LiquibaseService;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.statement.SqlStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@LiquibaseService(skip = true)
public class MockActionGenerator implements ActionGenerator {
    private int priority;
    private boolean supports;
    private ValidationErrors errors = new ValidationErrors();
    private String[] returnSql;

    public MockActionGenerator(int priority, String... returnSql) {
        this(priority, true, returnSql);
    }

    public MockActionGenerator(int priority, boolean supports, String... returnSql) {
        this.priority = priority;
        this.supports = supports;
        this.returnSql = returnSql;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean supports(SqlStatement statement, ExecutionOptions options) {
        return supports;
    }

    @Override
    public boolean generateStatementsIsVolatile(ExecutionOptions options) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(ExecutionOptions options) {
        return false;
    }

    public MockActionGenerator addValidationError(String message) {
        errors.addError(message);

        return this;
    }

    @Override
    public Warnings warn(SqlStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(SqlStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        ValidationErrors validationErrors = chain.validate(statement, options);
        validationErrors.addAll(errors);
        return validationErrors;
    }

    @Override
    public Action[] generateActions(SqlStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        List<Action> actions = new ArrayList<Action>();
        for (String returnSql  : this.returnSql) {
            actions.add(new UnparsedSql(returnSql));
        }

        actions.addAll(Arrays.asList(chain.generateActions(statement, options)));

        return actions.toArray(new Action[actions.size()]);
    }
}
