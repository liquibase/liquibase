package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.statement.SqlStatement;

/**
 * Interface to implement when creating a custom change that generates SQL.  When updating a database,
 * implementing this interface is preferred over CustomTaskChange because the SQL can either be executed
 * directly or saved to a text file for later use depending on the migration mode used.  To allow
 * the change to be rolled back, also implement the CustomSqlRollback interface.  If your change requires sql-based
 * logic and non-sql-based logic, it is best to create a change set that contains a mix of CustomSqlChange and CustomTaskChange calls.
 *
 * @see liquibase.change.custom.CustomSqlRollback
 * @see liquibase.change.custom.CustomTaskChange
  */
public interface CustomSqlChange extends CustomChange {
    /**
     * Generates the SQL statements required to run the change
     *
     * @param database the target {@link liquibase.database.Database} associated to this change's statements
     * @return an array of {@link SqlStatement}s with the statements
     * @throws liquibase.exception.CustomChangeException if an exception occurs while processing this change
     */
    public SqlStatement[] generateStatements(Database database) throws CustomChangeException;

}
