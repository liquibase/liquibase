package liquibase.actionlogic;

import liquibase.Scope;
import liquibase.action.AbstractAction;
import liquibase.action.Action;
import liquibase.action.ActionStatus;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
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
     * Return the type of {@link Database} this this ActionLogic requires.
     * Return liquibase.database.Database if any database is supported, but one is required (default).
     * Return null if no database is required.
     * Used by {@link #supportsScope(liquibase.Scope)}. If more complex logic is required than just one Database subclass, override supportsScope(Scope).
     */
    protected Class<? extends Database> getRequiredDatabase() {
        return Database.class;
    }

    /**
     * Return the type of {@link liquibase.database.DatabaseConnection} this this ActionLogic requires.
     * Return liquibase.database.DatabaseConnection if any database is supported, but one is required (default).
     * Return null if no database is required.
     * Used by {@link #supportsScope(liquibase.Scope)}. If more complex logic is required than just one DatabaseConnection subclass, override supportsScope(Scope).
     */
    protected Class<? extends DatabaseConnection> getRequiredConnection() {
        return DatabaseConnection.class;
    }

    /**
     * Return true this ActionLogic implementation is valid for the given scope. Used by {@link AbstractActionLogic#getPriority(liquibase.action.Action, liquibase.Scope)}
     */
    protected boolean supportsScope(Scope scope) {
        Class<? extends Database> requiredDatabase = getRequiredDatabase();
        if (requiredDatabase != null) {
            Database database = scope.getDatabase();
            boolean databaseCorrect = database != null && requiredDatabase.isAssignableFrom(database.getClass());

            if (databaseCorrect) {
                Class<? extends DatabaseConnection> requiredConnection = getRequiredConnection();

                if (requiredConnection != null) {
                    DatabaseConnection connection = database.getConnection();
                    databaseCorrect = connection != null && requiredConnection.isAssignableFrom(connection.getClass());
                }
            }
            return databaseCorrect;
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

    /**
     * Default implementation of {@link ActionLogic#checkStatus(Action, Scope)} returns an {@link liquibase.action.ActionStatus.Status#unknown}.
     */
    @Override
    public ActionStatus checkStatus(T action, Scope scope) {
        return new ActionStatus().unknown("No checkStatus logic defined");
    }


}
