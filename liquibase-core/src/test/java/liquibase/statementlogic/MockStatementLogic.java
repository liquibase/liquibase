package liquibase.statementlogic;

import liquibase.action.Action;
import liquibase.action.core.UnparsedSql;
import liquibase.exception.UnsupportedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import  liquibase.ExecutionEnvironment;
import liquibase.servicelocator.LiquibaseService;
import liquibase.statement.Statement;
import liquibase.statement.core.MockStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mock object for testing with StatementLogic.
 */
@LiquibaseService(skip = true)
public class MockStatementLogic implements StatementLogic {
    private int priority;

    private String supportsId;

    private ValidationErrors errors = new ValidationErrors();
    private Warnings warnings = new Warnings();

    private String[] returnSql;

    private ChainLogic chainLogic = ChainLogic.CALL_CHAIN_FIRST;
    private boolean generateActionsIsVolatile = false;

    /**
     * Create StatementLogic with the given priority. GenerateActions will return an array containing UnparsedSql Actions containing given strings.
     * Supports method always returns true.
     */
    public MockStatementLogic(int priority, String... returnSql) {
        this.priority = priority;
        this.returnSql = returnSql;
    }

    /**
     * ChainLogic controls what to do in the validate, warn and generateActions methods.
     * If ChainLogic.CALL_CHAIN_FIRST, then chain.METHOD is called before this object's logic.
     * If ChainLogic.CALL_CHAIN_LAST, then chain.METHOD is called after this object's logic.
     * If ChainLogic.CALL_CHAIN_NEVER, then chain.METHOD is never called.
     */
    public ChainLogic getChainLogic() {
        return chainLogic;
    }

    public MockStatementLogic setChainLogic(ChainLogic chainLogic) {
        this.chainLogic = chainLogic;
        return this;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean supports(Statement statement, ExecutionEnvironment env) {
        if (statement instanceof MockStatement) {
            String statementId = ((MockStatement) statement).getId();
            return supportsId == null || statementId == null || statementId.equals(supportsId);
        }
        return true;
    }

    /**
     * Set the MockStatement ids this should return "true" in {@link #supports(liquibase.statement.Statement, liquibase.ExecutionEnvironment)} for.
     * Set to null to support all
     */
    public MockStatementLogic setSupportsId(String id) {
        this.supportsId = id;
        return this;
    }

    /**
     * Returns false unless overwritten by setter.
     */
    @Override
    public boolean generateActionsIsVolatile(ExecutionEnvironment env) {
        return generateActionsIsVolatile;
    }

    public MockStatementLogic setGenerateActionsIsVolatile(boolean generateActionsIsVolatile) {
        this.generateActionsIsVolatile = generateActionsIsVolatile;
        return this;
    }

    /**
     * Add message to return in {@link #validate(liquibase.statement.Statement, liquibase.ExecutionEnvironment, StatementLogicChain)}
     */
    public MockStatementLogic addValidationError(String message) {
        errors.addError(message);

        return this;
    }

    /**
     * Add message to return in {@link #warn(liquibase.statement.Statement, liquibase.ExecutionEnvironment, StatementLogicChain)}
     */
    public MockStatementLogic addWarning(String message) {
        warnings.addWarning(message);
        return this;
    }



    @Override
    public Warnings warn(Statement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        Warnings warnings = new Warnings();
        if (chainLogic == ChainLogic.CALL_CHAIN_FIRST) {
            warnings.addAll(chain.warn(statement, env));
        }
        warnings.addAll(this.warnings);

        if (chainLogic == ChainLogic.CALL_CHAIN_LAST) {
            warnings.addAll(chain.warn(statement, env));
        }
        return warnings;
    }

    @Override
    public ValidationErrors validate(Statement statement, ExecutionEnvironment env, StatementLogicChain chain) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (chainLogic == ChainLogic.CALL_CHAIN_FIRST) {
            validationErrors.addAll(chain.validate(statement, env));
        }
        validationErrors.addAll(errors);

        if (chainLogic == ChainLogic.CALL_CHAIN_LAST) {
            validationErrors.addAll(chain.validate(statement, env));
        }
        return validationErrors;
    }

    @Override
    public Action[] generateActions(Statement statement, ExecutionEnvironment env, StatementLogicChain chain) throws UnsupportedException {
        List<Action> actions = new ArrayList<Action>();

        if (chainLogic == ChainLogic.CALL_CHAIN_FIRST) {
            actions.addAll(Arrays.asList(chain.generateActions(statement, env)));
        }

        generateMockActions(statement, env, actions);

        if (chainLogic == ChainLogic.CALL_CHAIN_LAST) {
            actions.addAll(Arrays.asList(chain.generateActions(statement, env)));
        }


        return actions.toArray(new Action[actions.size()]);
    }

    /**
     * Create the actions. Chain logic is handled in main generateActions method, so you should only override this method if you want to customize actions returned.
     */
    protected void generateMockActions(Statement statement, ExecutionEnvironment env, List<Action> actions) {
        for (String returnSql  : this.returnSql) {
            actions.add(new UnparsedSql(returnSql));
        }
    }

    public enum ChainLogic {
        CALL_CHAIN_NEVER,
        CALL_CHAIN_FIRST,
        CALL_CHAIN_LAST,
    }
}
