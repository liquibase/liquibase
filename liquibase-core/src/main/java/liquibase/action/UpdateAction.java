package liquibase.action;

import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.UpdateResult;

/**
 * An interface for {@link liquibase.action.Action}s that updates data. This action can update data in a database, or any other location.
 * Implementations should only perform outside interaction from within the {@link #update(liquibase.executor.ExecutionOptions)} method.
 */
public interface UpdateAction extends Action {
    UpdateResult update(ExecutionOptions options) throws DatabaseException;
}
