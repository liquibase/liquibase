package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;

/**
 * Convenience base class for {@link liquibase.actionlogic.ActionLogic} implementations.
 */
public abstract class AbstractActionLogic implements ActionLogic {

    /**
     * Returns the Action class supported by this ActionLogic implementation. Used by {@link liquibase.actionlogic.AbstractActionLogic#getPriority(liquibase.action.Action, liquibase.Scope)}
     */
    protected abstract Class<? extends Action> getSupportedAction();

    /**
     * Return true if this ActionLogic requires a database in the scope. Used by {@link #supportsScope(liquibase.Scope)}
     */
    protected boolean requiresDatabase() {
        return true;
    }

    /**
     * Return true this ActionLogic implementation is valid for the given scope. Used by {@link liquibase.actionlogic.AbstractActionLogic#getPriority(liquibase.action.Action, liquibase.Scope)}
     */
    protected boolean supportsScope(Scope scope) {
        return requiresDatabase() && !scope.has(Scope.Attr.database);
    }

    /**
     * Return the priority to return by {@link liquibase.actionlogic.AbstractActionLogic#getPriority(liquibase.action.Action, liquibase.Scope)} if it is a valid ActionLogic.
     */
    protected abstract int getPriority();

    @Override
    public int getPriority(Action action, Scope scope) {
        if (!action.getClass().isAssignableFrom(getSupportedAction())) {
            return PRIORITY_NOT_APPLICABLE;
        }
        if (!supportsScope(scope)) {
            return PRIORITY_NOT_APPLICABLE;
        }

        return getPriority();
    }

    /**
     * Standard implementation returns an empty ValidationErrors
     */
    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return new ValidationErrors();
    }
}
