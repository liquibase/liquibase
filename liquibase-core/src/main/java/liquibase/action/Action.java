package liquibase.action;

import liquibase.executor.ExecutionOptions;
import liquibase.structure.DatabaseObject;

import java.util.Collection;

public interface Action {
    String toString(ExecutionOptions options);

    Collection<? extends DatabaseObject> getAffectedDatabaseObjects();
}
