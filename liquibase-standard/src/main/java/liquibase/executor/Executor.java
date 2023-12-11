package liquibase.executor;

import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.plugin.Plugin;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;

import java.util.List;
import java.util.Map;

/**
 * Interface for a class that is capable of executing statements/queries against a DBMS.
 */
public interface Executor extends Plugin {
    /**
     * Return the name of the Executor
     *
     * @return String   The Executor name
     */
    String getName();

    /**
     * Return the Executor priority
     *
     * @return int      The Executor priority
     */
    int getPriority();

    /**
     * Validate if the changeset can be executed by this Executor
     * If the ChangeSet can be executed return an empty ValidationErrors object
     * otherwise return the errors
     *
     * @param changeSet The changeset to validate
     * @return ValidationErrors  Any errors which occur during validation
     */
    ValidationErrors validate(ChangeSet changeSet);

    /**
     * Allow this Executor to make any needed changes to the changeset
     *
     * @param changeSet The changeset to operate on
     */
    void modifyChangeSet(ChangeSet changeSet);

    /**
     * Set a ResourceAccessor on this Executor to be used in file access
     *
     * @param resourceAccessor
     */
    void setResourceAccessor(ResourceAccessor resourceAccessor);

    /**
     * Configures the Executor for the Database to run statements/queries against.
     *
     * @param database The database
     */
    void setDatabase(Database database);

    /**
     * Execute a query that is expected to return a scalar (1 row, 1 column).
     * It is expected that the scalar can be cast into an object of type T.
     *
     * @param sql The query to execute
     * @return An object of type T, if successful. May also return null if no object is found.
     * @throws DatabaseException in case something goes wrong during the query execution
     */
    <T> T queryForObject(SqlStatement sql, Class<T> requiredType) throws DatabaseException;

    /**
     * Applies a number of SqlVisitors to the sql query.
     * Then, executes the (possibly modified) query.
     * The query is expected to return a scalar (1 row, 1 column).
     * That scalar is expected to return a single value that can be cast into an object of type T.
     *
     * @param sql The query to execute
     * @return An object of type T, if successful. May also return null if no object is found.
     * @throws DatabaseException in case something goes wrong during the query execution
     */
    <T> T queryForObject(SqlStatement sql, Class<T> requiredType, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    /**
     * Executes a query that is expected to return a scalar (1 row, 1 column).
     * It is expected that the scalar can be cast into a long.
     *
     * @param sql The query to execute
     * @return A long value, if successful
     * @throws DatabaseException in case something goes wrong during the query execution
     */
    long queryForLong(SqlStatement sql) throws DatabaseException;

    /**
     * Applies a number of SqlVisitors to the sql query.
     * Then, executes the (possibly modified) query.
     * The query is expected to return a scalar (1 row, 1 column), and that scalar is expected to be a long value.
     *
     * @param sql The query to execute
     * @return A long value, if successful
     * @throws DatabaseException in case something goes wrong during the query execution
     */
    long queryForLong(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    /**
     * Executes a query that is expected to return a scalar (1 row, 1 column).
     * It is expected that the scalar can be cast into an int.
     *
     * @param sql The query to execute
     * @return An integer, if successful
     * @throws DatabaseException in case something goes wrong during the query execution
     */
    int queryForInt(SqlStatement sql) throws DatabaseException;

    /**
     * Applies a number of SqlVisitors to the sql query.
     * Then, executes the (possibly modified) query.
     * The query is expected to return a scalar (1 row, 1 column), and that scalar is expected to be an int.
     *
     * @param sql The query to execute
     * @return An integer, if successful
     * @throws DatabaseException in case something goes wrong during the query execution
     */
    int queryForInt(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    List queryForList(SqlStatement sql, Class elementType) throws DatabaseException;

    List queryForList(SqlStatement sql, Class elementType, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    /**
     * Executes a given SQL statement and returns a List of rows. Each row is represented a a Map<String, ?>,
     * where the String is the column name and the value if the content of the column in the row (=cell).
     *
     * @param sql the SQL query to execute
     * @return a List of [Column name] -> [column value]-mapped rows.
     * @throws DatabaseException if an error occurs during SQL processing (e.g. the SQL is not valid for the database)
     */
    List<Map<String, ?>> queryForList(SqlStatement sql) throws DatabaseException;

    /**
     * Applies a list of SqlVisitors to the SQL query, then executes the (possibly modified) SQL query and lastly,
     * returns the list of rows. Each row is represented a a Map<String, ?>,
     * where the String is the column name and the value if the content of the column in the row (=cell).
     *
     * @param sql the SQL query to execute
     * @return a List of [Column name] -> [column value]-mapped rows.
     * @throws DatabaseException if an error occurs during SQL processing (e.g. the SQL is not valid for the database)
     */
    List<Map<String, ?>> queryForList(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;


    /**
     * Write methods
     */
    void execute(Change change) throws DatabaseException;

    void execute(Change change, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    void execute(SqlStatement sql) throws DatabaseException;

    void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    int update(SqlStatement sql) throws DatabaseException;

    int update(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    /**
     * Adds a comment to the database.  Currently does nothing but is overridden in the output JDBC template
     *
     * @param message
     * @throws liquibase.exception.DatabaseException
     */
    void comment(String message) throws DatabaseException;

    boolean updatesDatabase();

    /**
     * Default implementation for compatibility with a Database.
     * Requires default implementation in order to provide backward compatibility.
     * Method is used when ExecutorService is looking by a Executor implementation for a Database.
     * Can be overridden in Executor implementations with a higher priority to check against a Database.
     *
     * @param database the Database implementation opened from a url connection string
     * @return true if Database is supported by current Executor implementation.
     */
    default boolean supports(final Database database) {
        return true;
    }
}
