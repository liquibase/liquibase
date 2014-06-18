package liquibase.action;

import liquibase.action.Action;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.UpdateResult;

public interface UpdateAction extends Action {
    UpdateResult update(ExecutionOptions options) throws DatabaseException;
}
