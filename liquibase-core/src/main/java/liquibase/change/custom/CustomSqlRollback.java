package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.statement.SqlStatement;

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
     * @throws liquibase.exception.RollbackImpossibleException if rollback is not supported for this change
     */
    public SqlStatement[] generateRollbackStatements(Database database) throws CustomChangeException, RollbackImpossibleException;

}
