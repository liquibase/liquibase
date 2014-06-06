package liquibase.sql;

import liquibase.executor.ExecutionOptions;

public interface Executable {
    String toString(ExecutionOptions options);
}
