package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;

/**
 * Interface to implement when creating a custom change that does not actually generate SQL.
 * If you are updating a database through SQL, implementing CustomSqlChange is preferred because the SQL can either be executed
 * directly or saved to a text file for later use depending on the migration mode used.
 * To allow the change to be rolled back, also implement the CustomTaskRollback interface.
 * If your change requires sql-based logic and non-sql-based logic, it is best to create a changeset that contains a mix of CustomSqlChange and CustomTaskChange calls.
 *
 * @see liquibase.change.custom.CustomTaskRollback
 * @see liquibase.change.custom.CustomSqlChange
  */
public interface CustomTaskChange extends CustomChange {

    /**
     * Runs the change logic for this custom change.
     *
     * @param database the target database to apply the change to
     * @throws CustomChangeException if an exception occurs while processing the change
     */
    void execute(Database database) throws CustomChangeException;
}
