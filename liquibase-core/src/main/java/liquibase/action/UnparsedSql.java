package liquibase.action;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecuteResult;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;
import liquibase.executor.UpdateResult;
import liquibase.util.StringUtils;

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

    @Override
    public String toSql() {
        return sql;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        return toSql()+getEndDelimiter();
    }

    @Override
    public String describe() {
        return this.toSql()+getEndDelimiter();
    }

    @Override
    public String getEndDelimiter() {
        return endDelimiter;
    }

    @Override
    public QueryResult query(ExecutionOptions options) throws DatabaseException {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        DatabaseConnection conn = database.getConnection();

        if (conn instanceof OfflineConnection) {
            throw new DatabaseException("Cannot execute commands against an offline database");
        } else if (conn instanceof JdbcConnection) {
            return ((JdbcConnection) conn).query(getSql());
        } else {
            throw new DatabaseException("Cannot execute SQL against a "+conn.getClass().getName()+" connection");
        }
    }

    @Override
    public ExecuteResult execute(ExecutionOptions options) throws DatabaseException {
//TODO        if(sql instanceof ExecutablePreparedStatement) {
//            ((ExecutablePreparedStatement) sql).execute(new PreparedStatementFactory((JdbcConnection)database.getConnection()));
//            return new ExecuteResult();
//        }

        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        DatabaseConnection conn = database.getConnection();
        if (conn instanceof OfflineConnection) {
            throw new DatabaseException("Cannot execute commands against an offline database");
        } else if (conn instanceof JdbcConnection) {
            return ((JdbcConnection) conn).execute(getSql());
        } else {
            throw new DatabaseException("Cannot execute SQL against a " + conn.getClass().getName() + " connection");
        }
    }

    @Override
    public UpdateResult update(ExecutionOptions options) throws DatabaseException {
//        if (sql instanceof CallableSqlStatement) {
//            throw new DatabaseException("Direct update using CallableSqlStatement not currently implemented");
//        }

        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        DatabaseConnection conn = database.getConnection();
        if (conn instanceof OfflineConnection) {
            throw new DatabaseException("Cannot execute commands against an offline database");
        } else if (conn instanceof JdbcConnection) {
            return ((JdbcConnection) conn).update(getSql());
        } else {
            throw new DatabaseException("Cannot execute SQL against a " + conn.getClass().getName() + " connection");
        }
    }

}
