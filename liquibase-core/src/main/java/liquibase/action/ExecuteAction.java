package liquibase.action;

import liquibase.Scope;
import liquibase.exception.DatabaseException;

/**
 * An interface for {@link liquibase.action.Action}s that performs an arbitrary change.
 * If the action better fits a {@link liquibase.action.QueryAction} or {@link liquibase.action.UpdateAction} implement those more specific interfaces instead.
 * Implementations should only perform outside interaction from within the {@link #execute( liquibase.Scope)} method.
 */
public interface ExecuteAction extends Action {

    /**
     * Object returned from Executor.execute. Currently empty but in place for future updates.
     */
    public static class Result {

    }
}
