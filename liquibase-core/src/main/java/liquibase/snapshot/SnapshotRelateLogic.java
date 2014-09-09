package liquibase.snapshot;

import liquibase.ExecutionEnvironment;

public interface SnapshotRelateLogic {

    boolean supports(ExecutionEnvironment environment);

    void relate(NewDatabaseSnapshot snapshot);
}
