package liquibase.action;

import liquibase.executor.ExecutionOptions;
import liquibase.structure.DatabaseObject;

import java.util.Collection;

/**
 * Base interface for all actions to perform against the outside environment.
 * See {@link liquibase.action.QueryAction}, {@link liquibase.action.ExecuteAction}, and {@link liquibase.action.UpdateAction} for types of Actions to implement.
 */
public interface Action {
    String toString(ExecutionOptions options);

    Collection<? extends DatabaseObject> getAffectedDatabaseObjects();
}
