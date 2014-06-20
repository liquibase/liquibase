package liquibase.sqlgenerator;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutionOptions;
import liquibase.servicelocator.LiquibaseService;
import liquibase.statement.SqlStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@LiquibaseService(skip = true)
public class MockSqlGenerator implements SqlGenerator {
    private int priority;
    private boolean supports;
    private ValidationErrors errors = new ValidationErrors();
    private String[] returnSql;

    public MockSqlGenerator(int priority, String... returnSql) {
        this(priority, true, returnSql);
    }

    public MockSqlGenerator(int priority, boolean supports, String... returnSql) {
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

    public MockSqlGenerator addValidationError(String message) {
        errors.addError(message);

        return this;
    }

    @Override
    public Warnings warn(SqlStatement sqlStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        return new Warnings();
    }

    @Override
    public Warnings warn(SqlStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        return warn(statement, options, new SqlGeneratorChain(chain));
    }

    @Override
    public ValidationErrors validate(SqlStatement statement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = sqlGeneratorChain.validate(statement, options);
        validationErrors.addAll(errors);
        return validationErrors;
    }

    @Override
    public ValidationErrors validate(SqlStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        return validate(statement, options, new SqlGeneratorChain(chain));
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
