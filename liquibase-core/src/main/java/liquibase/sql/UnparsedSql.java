package liquibase.sql;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecuteResult;
import liquibase.executor.ExecutionOptions;
import liquibase.executor.QueryResult;
import liquibase.executor.UpdateResult;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.util.*;

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

    @Override
    public String toString() {
        return toSql()+getEndDelimiter();
    }

    @Override
    public String describe() {
        StringBuilder out = new StringBuilder(toFinalSql(options));

        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        if (database instanceof MSSQLDatabase || database instanceof SybaseDatabase || database instanceof SybaseASADatabase) {
            out.append(StreamUtil.getLineSeparator());
            out.append("GO");
        } else {
            String endDelimiter = getEndDelimiter();
            if (!out.toString().endsWith(endDelimiter)) {
                out.append(endDelimiter);
            }
        }

        return out.toString();
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
            return ((JdbcConnection) conn).query(toFinalSql(options));
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
            return ((JdbcConnection) conn).execute(toFinalSql(options));
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
            return ((JdbcConnection) conn).update(toFinalSql(options));
        } else {
            throw new DatabaseException("Cannot execute SQL against a " + conn.getClass().getName() + " connection");
        }
    }

    protected String toFinalSql(ExecutionOptions options) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        String finalSql = sql;
        List<SqlVisitor> sqlVisitors = options.getSqlVisitors();
        if (sqlVisitors != null) {
            for (SqlVisitor visitor : sqlVisitors) {
                finalSql = visitor.modifySql(finalSql, database);
            }
        }
        return finalSql;
    }


}
