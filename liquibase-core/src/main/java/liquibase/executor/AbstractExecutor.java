package liquibase.executor;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.sql.Sql;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;

import java.util.List;
import java.util.Set;

/**
 * Convenience base class for Executor implementations.
 */
public abstract class AbstractExecutor implements Executor {
    protected Database database;

    public void setDatabase(Database database) {
        this.database = database;
    }

    /**
     * Default implementation delegates to {@link #query(liquibase.statement.SqlStatement, ExecutionOptions)}
     */
    @Override
    public QueryResult query(SqlStatement sql) throws DatabaseException {
        return query(sql, createDefaultExecutionOptions());
    }

    /**
     * Default implementation delegates to {@link #execute(liquibase.statement.SqlStatement, ExecutionOptions)}
     */
    @Override
    public ExecuteResult execute(SqlStatement sql) throws DatabaseException {
        return execute(sql, createDefaultExecutionOptions());
    }

    /**
     * Default implementation delegates to {@link #update(liquibase.statement.SqlStatement, ExecutionOptions)}
     */
    @Override
    public UpdateResult update(SqlStatement sql) throws DatabaseException {
        return update(sql, createDefaultExecutionOptions());
    }

    /**
     * Create ExecutionOptions to use in single-parameter execute/update/query calls.
     */
    protected ExecutionOptions createDefaultExecutionOptions() {
        return new ExecutionOptions();
    }


    protected String[] applyVisitors(SqlStatement statement, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        if (sql == null) {
            return new String[0];
        }
        String[] returnSql = new String[sql.length];

        for (int i=0; i<sql.length; i++) {
            if (sql[i] == null) {
                continue;
            }
            returnSql[i] = sql[i].toSql();
            if (sqlVisitors != null) {
                for (SqlVisitor visitor : sqlVisitors) {
                    returnSql[i] = visitor.modifySql(returnSql[i], database);
                }
            }

        }
        return returnSql;
    }

}
