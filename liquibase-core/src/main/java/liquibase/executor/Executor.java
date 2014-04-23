package liquibase.executor;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.CallableSqlStatement;
import liquibase.statement.SqlStatement;

import java.util.List;
import java.util.Map;

public interface Executor {

    void setDatabase(Database database);

    /** Read methods */
    <T>  T queryForObject(SqlStatement sql, Class<T> requiredType) throws DatabaseException;

    <T> T queryForObject(SqlStatement sql, Class<T> requiredType, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    long queryForLong(SqlStatement sql) throws DatabaseException;

    long queryForLong(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    int queryForInt(SqlStatement sql) throws DatabaseException;

    int queryForInt(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    List queryForList(SqlStatement sql, Class elementType) throws DatabaseException;

    List queryForList(SqlStatement sql, Class elementType, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    List<Map<String, ?>> queryForList(SqlStatement sql) throws DatabaseException;

    List<Map<String, ?>> queryForList(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;


    /** Write methods */
    void execute(Change change) throws DatabaseException;

    void execute(Change change, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    void execute(SqlStatement sql) throws DatabaseException;

    void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    int update(SqlStatement sql) throws DatabaseException;

    int update(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    /**
     * Adds a comment to the database.  Currently does nothing but is over-ridden in the output JDBC template
     * @param message
     * @throws liquibase.exception.DatabaseException
     */
    void comment(String message) throws DatabaseException;

    boolean updatesDatabase();
}
