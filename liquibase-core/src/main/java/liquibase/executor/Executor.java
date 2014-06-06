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

    QueryResult query(SqlStatement sql) throws DatabaseException;

    QueryResult query(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    /** Write methods */
    void execute(Change change) throws DatabaseException;

    void execute(Change change, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    void execute(SqlStatement sql) throws DatabaseException;

    void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    UpdateResult update(SqlStatement sql) throws DatabaseException;

    UpdateResult update(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws DatabaseException;

    /**
     * Adds a comment to the database.  Currently does nothing but is over-ridden in the output JDBC template
     * @param message
     * @throws liquibase.exception.DatabaseException
     */
    void comment(String message) throws DatabaseException;

    boolean updatesDatabase();
}
