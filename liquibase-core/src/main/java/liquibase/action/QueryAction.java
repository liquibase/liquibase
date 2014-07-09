package liquibase.action;

import liquibase.exception.DatabaseException;
import  liquibase.ExecutionEnvironment;
import liquibase.executor.QueryResult;

/**
 * An interface for {@link liquibase.action.Action}s that fetches data. This action can fetch data from a database, or any other location.
 * Implementations should only perform outside interaction from within the {@link #query( liquibase.ExecutionEnvironment)} method.
 */
public interface QueryAction extends Action {

    /**
     * Performs the actual query
     */
    QueryResult query(ExecutionEnvironment env) throws DatabaseException;
}
