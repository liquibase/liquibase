package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.AbstractAction;
import liquibase.action.Action;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;

/**
 * Convenience base class for {@link liquibase.actionlogic.ActionLogic} implementations.
 */
public abstract class AbstractActionLogic<T extends Action> implements ActionLogic<T> {

    /**
     * Returns the Action class supported by this ActionLogic implementation. Used by {@link AbstractActionLogic#getPriority(liquibase.action.Action, liquibase.Scope)}
     */
    protected abstract Class<? extends T> getSupportedAction();

    /**
     * Return true if this ActionLogic requires a database in the scope. Used by {@link #supportsScope(liquibase.Scope)}
     */
    protected Class<? extends Database> getRequiredDatabase() {
        return Database.class;
    }

    /**
     * Return true this ActionLogic implementation is valid for the given scope. Used by {@link AbstractActionLogic#getPriority(liquibase.action.Action, liquibase.Scope)}
     */
    protected boolean supportsScope(Scope scope) {
        Class<? extends Database> requiredDatabase = getRequiredDatabase();
        if (requiredDatabase != null) {
            Database database = scope.getDatabase();
            return database != null && requiredDatabase.isAssignableFrom(database.getClass());
        }

        return true;
    }

    @Override
    public int getPriority(T action, Scope scope) {
        if (!action.getClass().isAssignableFrom(getSupportedAction())) {
            return PRIORITY_NOT_APPLICABLE;
        }
        if (!supportsScope(scope)) {
            return PRIORITY_NOT_APPLICABLE;
        }

        Class<? extends Database> requiredDatabase = getRequiredDatabase();
        if (requiredDatabase == null || requiredDatabase.equals(Database.class)) {
            return PRIORITY_DEFAULT;
        } else {
            return PRIORITY_SPECIALIZED;
        }
    }

    /**
     * Standard implementation returns an empty ValidationErrors
     */
    @Override
    public ValidationErrors validate(T action, Scope scope) {
        return new ValidationErrors();
    }
}
