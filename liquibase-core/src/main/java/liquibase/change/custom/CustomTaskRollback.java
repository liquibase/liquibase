package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;

/**
 * Interface to implement that allows rollback of a custom task change.
 *
 * @see liquibase.change.custom.CustomTaskChange
 */
public interface CustomTaskRollback {

    /**
     * Method called to rollback the change.
     * @param database Database the change is being executed against.
     * @throws liquibase.exception.CustomChangeException an exception occurs while processing this rollback
     * @throws liquibase.exception.RollbackImpossibleException if rollback is not supported for this change
     */
    public void rollback(Database database) throws CustomChangeException, RollbackImpossibleException;
}
