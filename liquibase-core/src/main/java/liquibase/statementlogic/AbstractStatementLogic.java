package liquibase.statementlogic;

import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.Statement;

/**
 * Convenience base class for {@link StatementLogic} implementations.
 */
public abstract class AbstractStatementLogic<StatementType extends Statement> implements StatementLogic<StatementType> {

    /**
     * Default implementation returns PRIORITY_DEFAULT
     */
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    /**
     * Default implementation returns true
     */
    @Override
    public boolean supports(StatementType statement, ExecutionEnvironment env) {
        return true;
    }

    /**
     * Default implementation simply calls {@link liquibase.statementlogic.StatementLogicChain#validate(liquibase.statement.Statement, liquibase.ExecutionEnvironment)}
     */
    @Override
    public ValidationErrors validate(StatementType statement, ExecutionEnvironment env, StatementLogicChain chain) {
        return chain.validate(statement, env);
    }

    /**
     * Default implementation simply calls {@link liquibase.statementlogic.StatementLogicChain#warn(liquibase.statement.Statement, liquibase.ExecutionEnvironment)}
     */
    @Override
    public Warnings warn(StatementType statementType, ExecutionEnvironment env, StatementLogicChain chain) {
        return chain.warn(statementType, env);
    }

    /**
     * Default implementation returns false.
     */
    @Override
    public boolean generateActionsIsVolatile(ExecutionEnvironment env) {
        return false;
    }
}
