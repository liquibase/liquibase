package liquibase.statementlogic;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.statement.Statement;

import java.util.Iterator;
import java.util.SortedSet;

/**
 * Implementation of chain pattern for {@link liquibase.statementlogic.StatementLogic} implementations.
 *
 * @see liquibase.statementlogic.StatementLogicFactory
 */
public class StatementLogicChain {
    private Iterator<StatementLogic> statementLogicIterator;

    public StatementLogicChain(SortedSet<StatementLogic> statementLogic) {
        if (statementLogic != null) {
            this.statementLogicIterator = statementLogic.iterator();
        }
    }

    /**
     * Generate Actions for the given statement using the defined collection of StatementLogic implementations.
     * If an empty or null collection was configured, returns an empty Action array.
     */
    public Action[] generateActions(Statement statement, ExecutionEnvironment env) {
        if (statementLogicIterator == null) {
            return null;
        }

        if (!statementLogicIterator.hasNext()) {
            return new Action[0];
        }

        return statementLogicIterator.next().generateActions(statement, env, this);
    }

    /**
     * Generate warnings for the given statement using the defined collection of StatementLogic implementations.
     * If an empty or null collection was configured, returns an empty Warnings object.
     */
    public Warnings warn(Statement statement, ExecutionEnvironment env) {
        if (statementLogicIterator == null || !statementLogicIterator.hasNext()) {
            return new Warnings();
        }

        return statementLogicIterator.next().warn(statement, env, this);
    }

    /**
     * Validate the given statement using the defined collection of StatementLogic implementations.
     * If an empty or null collection was configured, returns an empty ValidationErrors object.
     */
    public ValidationErrors validate(Statement statement, ExecutionEnvironment env) {
        if (statementLogicIterator == null || !statementLogicIterator.hasNext()) {
            return new ValidationErrors();
        }

        return statementLogicIterator.next().validate(statement, env, this);
    }

    /**
     * Mark a particular StatementLogic class as blocked in the chain.
     * If/when the chain reaches a StatementLogic implementation exactly equal to the passed class, it will skip it and move on to the next.
     * It will only block exact class matches. Subclasses need to be blocked on their own.
     */
    public void block(Class<? extends StatementLogic> logicClass) {

    }
}
