package liquibase.executor;

import liquibase.sql.visitor.SqlVisitor;
import liquibase.exception.JDBCException;
import liquibase.statement.SqlStatement;
import liquibase.statement.CallableSqlStatement;

import java.util.List;
import java.util.Map;

public interface WriteExecutor {
    boolean executesStatements();

    Object execute(StatementCallback action, List<SqlVisitor> sqlVisitors) throws JDBCException;

    void execute(SqlStatement sql) throws JDBCException;

    void execute(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws JDBCException;

    int update(SqlStatement sql) throws JDBCException;

    int update(SqlStatement sql, List<SqlVisitor> sqlVisitors) throws JDBCException;

    Object execute(CallableSqlStatement csc, CallableStatementCallback action, List<SqlVisitor> sqlVisitors) throws JDBCException;

    Map call(CallableSqlStatement csc, List declaredParameters, List<SqlVisitor> sqlVisitors) throws JDBCException;

    /**
     * Adds a comment to the database.  Currently does nothing but is over-ridden in the output JDBC template
     * @param message
     * @throws liquibase.exception.JDBCException
     */
    void comment(String message) throws JDBCException;
}
