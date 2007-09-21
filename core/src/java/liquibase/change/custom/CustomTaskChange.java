package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.UnsupportedChangeException;

/**
 * Interface to implement when creating a custom change that does not actually generate SQL.
 * If you are updating a database through SQL, implementing CustomSqlChange is preferred because the SQL can either be executed
 * directly or saved to a text file for later use depending on the migration mode used.
 * To allow the change to be rolled back, also implement the CustomTaskRollback interface.
 * If your change requires sql-based logic and non-sql-based logic, it is best to create a change set that contains a mix of CustomSqlChange and CustomTaskChange calls.
 *
 * @see liquibase.change.custom.CustomTaskRollback
 * @see liquibase.change.custom.CustomSqlChange
  */
public interface CustomTaskChange extends CustomChange {
    
    /**
     * Method called to run the change logic.
     * @param database
     * @throws liquibase.exception.CustomChangeException an exception occurs while processing this change
     * @throws liquibase.exception.UnsupportedChangeException if this change is not supported by the {@link liquibase.database.Database} passed as argument
     */
    public void execute(Database database) throws CustomChangeException, UnsupportedChangeException;
}
