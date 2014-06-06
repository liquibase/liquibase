package liquibase.sql;

import liquibase.exception.DatabaseException;
import liquibase.executor.ExecuteResult;
import liquibase.executor.ExecutionOptions;

public interface ExecutableExecute extends Executable {
    ExecuteResult execute(ExecutionOptions options) throws DatabaseException;
}
