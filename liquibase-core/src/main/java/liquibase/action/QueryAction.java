package liquibase.action;

import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;

/**
 * An interface for {@link liquibase.action.Action}s that fetches data. This action can fetch data from a database, or any other location.
 * Implementations should only perform outside interaction from within the {@link #query(liquibase.executor.ExecutionOptions)} method.
 */
public interface QueryAction extends Action {

    /**
     * Performs the actual query
     */
    QueryResult query(ExecutionOptions options) throws DatabaseException;
}
