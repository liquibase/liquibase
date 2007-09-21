package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.CustomChangeException;

public interface CustomTaskRollback {

    /**
     * Method called to rollback the change.
     * @param database Database the change is being executed against.
     * @throws liquibase.exception.CustomChangeException an exception occurs while processing this rollback
     * @throws liquibase.exception.UnsupportedChangeException if this change is not supported by the {@link liquibase.database.Database} passed as argument
     * @throws liquibase.exception.RollbackImpossibleException if rollback is not supported for this change
     */
    public void rollback(Database database) throws CustomChangeException, UnsupportedChangeException, RollbackImpossibleException;
}
