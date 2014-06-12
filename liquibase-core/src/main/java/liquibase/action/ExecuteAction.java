package liquibase.action;

import liquibase.exception.DatabaseException;
import liquibase.executor.ExecuteResult;
import liquibase.executor.ExecutionOptions;

public interface ExecuteAction extends Action {
    ExecuteResult execute(ExecutionOptions options) throws DatabaseException;
}
