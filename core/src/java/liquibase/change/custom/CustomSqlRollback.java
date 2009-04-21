package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.database.statement.SqlStatement;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.UnsupportedChangeException;

/**
 * Interface to implement that allows rollback of a custom sql change.
 *
 * @see liquibase.change.custom.CustomSqlChange
 */
public interface CustomSqlRollback {

    /**
     * Generates the SQL statements required to roll back the change
     *
     * @param database the target {@link liquibase.database.Database} associated to this change's rollback statements
     * @return an array of {@link SqlStatement}s with the rollback statements
     * @throws liquibase.exception.CustomChangeException if an exception occurs while processing this rollback
     * @throws liquibase.exception.UnsupportedChangeException if this change is not supported by the {@link liquibase.database.Database} passed as argument
     * @throws liquibase.exception.RollbackImpossibleException if rollback is not supported for this change
     */
    public SqlStatement[] generateRollbackStatements(Database database) throws CustomChangeException, UnsupportedChangeException, RollbackImpossibleException;

}
