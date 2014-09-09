package liquibase.snapshot;

import liquibase.ExecutionEnvironment;

/**
 * Implementations of this interface contain logic to retrieve additional details about objects that have been found in the database.
 * Examples include finding the number of rows in tables.
 */
public interface SnapshotDetailsLogic {

    boolean supports(ExecutionEnvironment environment);

    void addDetails(NewDatabaseSnapshot snapshot);
}
