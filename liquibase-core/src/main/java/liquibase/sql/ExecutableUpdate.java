package liquibase.sql;

import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.UpdateResult;

public interface ExecutableUpdate extends Executable {
    UpdateResult update(ExecutionOptions options) throws DatabaseException;
}
