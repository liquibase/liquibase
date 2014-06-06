package liquibase.sql;

import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;

public interface ExecutableQuery extends Executable {
    QueryResult query(ExecutionOptions options) throws DatabaseException;
}
