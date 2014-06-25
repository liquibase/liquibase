package liquibase.statementlogic;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import  liquibase.ExecutionEnvironment;
import liquibase.servicelocator.LiquibaseService;
import liquibase.statement.SqlStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@LiquibaseService(skip = true)
public class MockStatementLogic implements StatementLogic {
    private int priority;
    private boolean supports;
    private ValidationErrors errors = new ValidationErrors();
    private String[] returnSql;

    public MockStatementLogic(int priority, String... returnSql) {
        this(priority, true, returnSql);
    }

    public MockStatementLogic(int priority, boolean supports, String... returnSql) {
        this.priority = priority;
        this.supports = supports;
        this.returnSql = returnSql;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean supports(SqlStatement statement, ExecutionEnvironment env) {
        return supports;
    }

    @Override
    public boolean generateStatementsIsVolatile(ExecutionEnvironment env) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(ExecutionEnvironment env) {
        return false;
    }

    public MockStatementLogic addValidationError(String message) {
        errors.addError(message);

        return this;
    }

    @Override
    public Warnings warn(SqlStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(SqlStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = chain.validate(statement, env);
        validationErrors.addAll(errors);
        return validationErrors;
    }

    @Override
    public Action[] generateActions(SqlStatement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        List<Action> actions = new ArrayList<Action>();
        for (String returnSql  : this.returnSql) {
            actions.add(new UnparsedSql(returnSql));
        }

        actions.addAll(Arrays.asList(chain.generateActions(statement, env)));

        return actions.toArray(new Action[actions.size()]);
    }
}
