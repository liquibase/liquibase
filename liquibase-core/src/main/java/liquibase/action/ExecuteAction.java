package liquibase.action;

import liquibase.exception.DatabaseException;
import liquibase.executor.ExecuteResult;
import  liquibase.ExecutionEnvironment;

/**
 * An interface for {@link liquibase.action.Action}s that performs an arbitrary change.
 * If the action better fits a {@link liquibase.action.QueryAction} or {@link liquibase.action.UpdateAction} implement those more specific interfaces instead.
 * Implementations should only perform outside interaction from within the {@link #execute( liquibase.ExecutionEnvironment)} method.
 */
public interface ExecuteAction extends Action {

    /**
     * Execute the action.
     */
    ExecuteResult execute(ExecutionEnvironment env) throws DatabaseException;
}
