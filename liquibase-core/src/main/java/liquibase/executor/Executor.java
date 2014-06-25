package liquibase.executor;

import liquibase.ExecutionEnvironment;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.statement.Statement;

/**
 * Interface for interacting with the database.
 */
public interface Executor {

    void setDatabase(Database database);

    /**
     * Query the database using default ExecutionOptions.
     */
    QueryResult query(Statement sql) throws DatabaseException;

    /**
     * Perform a query operation against the database
     */
    QueryResult query(Statement sql, ExecutionEnvironment env) throws DatabaseException;

    /**
     * Execute statement against the database using default ExecutionOptions.
     */
    ExecuteResult execute(Statement sql) throws DatabaseException;

    /**
     * Perform an execute operation against the database
     */
    ExecuteResult execute(Statement sql, ExecutionEnvironment env) throws DatabaseException;

    /**
     * Update using default ExecutionOptions.
     */
    UpdateResult update(Statement sql) throws DatabaseException;

    /**
     * Update data in the database
     */
    UpdateResult update(Statement sql, ExecutionEnvironment env) throws DatabaseException;

    /**
     * Adds a comment to the database.  Currently does nothing but is over-ridden in the output JDBC template
     * @param message
     * @throws liquibase.exception.DatabaseException
     */
    void comment(String message) throws DatabaseException;

    boolean updatesDatabase();
}
