package liquibase.executor;

import liquibase.ExecutionEnvironment;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.statement.SqlStatement;

/**
 * Interface for interacting with the database.
 */
public interface Executor {

    void setDatabase(Database database);

    /**
     * Query the database using default ExecutionOptions.
     */
    QueryResult query(SqlStatement sql) throws DatabaseException;

    /**
     * Perform a query operation against the database
     */
    QueryResult query(SqlStatement sql, ExecutionEnvironment env) throws DatabaseException;

    /**
     * Execute statement against the database using default ExecutionOptions.
     */
    ExecuteResult execute(SqlStatement sql) throws DatabaseException;

    /**
     * Perform an execute operation against the database
     */
    ExecuteResult execute(SqlStatement sql, ExecutionEnvironment env) throws DatabaseException;

    /**
     * Update using default ExecutionOptions.
     */
    UpdateResult update(SqlStatement sql) throws DatabaseException;

    /**
     * Update data in the database
     */
    UpdateResult update(SqlStatement sql, ExecutionEnvironment env) throws DatabaseException;

    /**
     * Adds a comment to the database.  Currently does nothing but is over-ridden in the output JDBC template
     * @param message
     * @throws liquibase.exception.DatabaseException
     */
    void comment(String message) throws DatabaseException;

    boolean updatesDatabase();
}
