package liquibase.executor;

import liquibase.ExecutionEnvironment;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnsupportedException;
import liquibase.statement.Statement;

/**
 * Convenience base class for Executor implementations.
 */
public abstract class AbstractExecutor implements Executor {
    protected Database database;

    public void setDatabase(Database database) {
        this.database = database;
    }

    /**
     * Default implementation delegates to {@link #query(liquibase.statement.Statement, liquibase.ExecutionEnvironment)}
     */
    @Override
    public QueryResult query(Statement sql) throws DatabaseException, UnsupportedException {
        return query(sql, createDefaultExecutionOptions());
    }

    /**
     * Default implementation delegates to {@link #execute(liquibase.statement.Statement, liquibase.ExecutionEnvironment)}
     */
    @Override
    public ExecuteResult execute(Statement sql) throws DatabaseException, UnsupportedException {
        return execute(sql, createDefaultExecutionOptions());
    }

    /**
     * Default implementation delegates to {@link #update(liquibase.statement.Statement, liquibase.ExecutionEnvironment)}
     */
    @Override
    public UpdateResult update(Statement sql) throws DatabaseException, UnsupportedException {
        return update(sql, createDefaultExecutionOptions());
    }

    /**
     * Create ExecutionOptions to use in single-parameter execute/update/query calls.
     */
    protected ExecutionEnvironment createDefaultExecutionOptions() {
        return new ExecutionEnvironment(database);
    }
}
