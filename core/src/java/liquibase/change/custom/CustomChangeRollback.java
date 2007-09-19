package liquibase.change.custom;

import liquibase.database.sql.SqlStatement;
import liquibase.database.Database;
import liquibase.exception.UnsupportedChangeException;
import liquibase.exception.RollbackImpossibleException;

public interface CustomChangeRollback {

    /**
     * Generates the SQL statements required to roll back the change
     *
     * @param database database databasethe target {@link liquibase.database.Database} associated to this change's rollback statements
     * @return an array of {@link String}s with the rollback statements
     * @throws liquibase.exception.UnsupportedChangeException if this change is not supported by the {@link liquibase.database.Database} passed as argument
     * @throws liquibase.exception.RollbackImpossibleException if rollback is not supported for this change
     */
    public SqlStatement[] generateRollbackStatements(Database database) throws UnsupportedChangeException, RollbackImpossibleException;

}
