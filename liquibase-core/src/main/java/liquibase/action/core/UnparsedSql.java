package liquibase.action.core;

import liquibase.action.Sql;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecuteResult;
import  liquibase.ExecutionEnvironment;
import liquibase.executor.QueryResult;
import liquibase.executor.UpdateResult;
import liquibase.util.StringUtils;

/**
 * Action implementation for executing a SQL statement. No attempt is made to parse the passed SQL, it is simply sent on directly to the database.
 * Requires a database with a JDBCConnection.
 */
public class UnparsedSql implements Sql {

    private String sql;
    private String endDelimiter;

    public UnparsedSql(String sql) {
        this(sql, ";");
    }

    public UnparsedSql(String sql, String endDelimiter) {
        this.sql = StringUtils.trimToEmpty(sql.trim());
        this.endDelimiter = endDelimiter;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return describe();
    }

    @Override
    public String describe() {
        String delimiter = getEndDelimiter();
        if (delimiter == null) {
            delimiter = "";
        }
        return getSql()+ delimiter;
    }

    @Override
    public String getEndDelimiter() {
        return endDelimiter;
    }

    @Override
    public QueryResult query(ExecutionEnvironment env) throws DatabaseException {
        Database database = env.getTargetDatabase();
        DatabaseConnection conn = database.getConnection();

        if (conn instanceof JdbcConnection) {
            return ((JdbcConnection) conn).query(getSql());
        } else {
            throw new DatabaseException("Cannot execute SQL against a "+conn.getClass().getName()+" connection");
        }
    }

    @Override
    public ExecuteResult execute(ExecutionEnvironment env) throws DatabaseException {
//TODO        if(sql instanceof ExecutablePreparedStatement) {
//            ((ExecutablePreparedStatement) sql).execute(new PreparedStatementFactory((JdbcConnection)database.getConnection()));
//            return new ExecuteResult();
//        }

        Database database = env.getTargetDatabase();
        DatabaseConnection conn = database.getConnection();
        if (conn instanceof JdbcConnection) {
            return ((JdbcConnection) conn).execute(getSql());
        } else {
            throw new DatabaseException("Cannot execute SQL against a " + conn.getClass().getName() + " connection");
        }
    }

    @Override
    public UpdateResult update(ExecutionEnvironment env) throws DatabaseException {
//        if (sql instanceof CallableSqlStatement) {
//            throw new DatabaseException("Direct update using CallableSqlStatement not currently implemented");
//        }

        Database database = env.getTargetDatabase();
        DatabaseConnection conn = database.getConnection();
        if (conn instanceof JdbcConnection) {
            return ((JdbcConnection) conn).update(getSql());
        } else {
            throw new DatabaseException("Cannot execute SQL against a " + conn.getClass().getName() + " connection");
        }
    }

}
