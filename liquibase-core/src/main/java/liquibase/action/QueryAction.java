package liquibase.action;

import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;

public interface QueryAction extends Action {
    QueryResult query(ExecutionOptions options) throws DatabaseException;
}
