package liquibase.statementlogic;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.exception.UnsupportedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.statement.Statement;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;

/**
 * Implementation of chain pattern for {@link liquibase.statementlogic.StatementLogic} implementations.
 *
 * @see liquibase.statementlogic.StatementLogicFactory
 */
public class StatementLogicChain {

    private Set<Class> blocked = new HashSet<Class>();

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
    public Action[] generateActions(Statement statement, ExecutionEnvironment env) throws UnsupportedException {
        StatementLogic next = getNext();
        if (next == null) {
            return new Action[0];
        }
        return next.generateActions(statement, env, this);
    }

    /**
     * Generate warnings for the given statement using the defined collection of StatementLogic implementations.
     * If an empty or null collection was configured, returns an empty Warnings object.
     */
    public Warnings warn(Statement statement, ExecutionEnvironment env) {
        StatementLogic next = getNext();
        if (next == null) {
            return new Warnings();
        }

        return next.warn(statement, env, this);
    }

    /**
     * Validate the given statement using the defined collection of StatementLogic implementations.
     * If an empty or null collection was configured, returns an empty ValidationErrors object.
     */
    public ValidationErrors validate(Statement statement, ExecutionEnvironment env) {
        StatementLogic next = getNext();
        if (next == null) {
            return new ValidationErrors();
        }
        return next.validate(statement, env, this);
    }

    /**
     * Mark a particular StatementLogic class as blocked in the chain.
     * If/when the chain reaches a StatementLogic implementation exactly equal to the passed class, it will skip it and move on to the next.
     * It will only block exact class matches. Subclasses need to be blocked on their own.
     */
    public void block(Class<? extends StatementLogic> logicClass) {
        blocked.add(logicClass);
    }

    protected StatementLogic getNext() {
        if (statementLogicIterator == null || !statementLogicIterator.hasNext()) {
            return null;
        }

        StatementLogic next = statementLogicIterator.next();
        if (blocked.contains(next.getClass())) {
            return getNext();
        } else {
            return next;
        }
    }
}
