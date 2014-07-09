package liquibase.executor;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.exception.UnsupportedException;
import liquibase.statement.Statement;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.servicelocator.LiquibaseService;
import liquibase.statement.core.GetNextChangeSetSequenceValueStatement;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogLockStatement;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;
import liquibase.util.StreamUtil;

import java.io.IOException;
import java.io.Writer;

@LiquibaseService(skip = true)
public class LoggingExecutor extends AbstractExecutor {

    private Writer output;
    private Executor delegatedReadExecutor;

    public LoggingExecutor(Executor delegatedExecutor, Writer output, Database database) {
        this.output = output;
        this.delegatedReadExecutor = delegatedExecutor;
        setDatabase(database);
    }

    protected Writer getOutput() {
        return output;
    }

    @Override
    public ExecuteResult execute(Statement sql, ExecutionEnvironment env) throws DatabaseException, UnsupportedException {
        outputStatement(sql, env);
        return new ExecuteResult();
    }

    @Override
    public UpdateResult update(Statement sql, ExecutionEnvironment env) throws DatabaseException, UnsupportedException {
        if (sql instanceof LockDatabaseChangeLogStatement) {
            return new UpdateResult(1);
        } else if (sql instanceof UnlockDatabaseChangeLogStatement) {
            return new UpdateResult(1);
        }

        outputStatement(sql, env);

        return new UpdateResult(0);
    }

    @Override
    public void comment(String message) throws DatabaseException {
        try {
            output.write(database.getLineComment());
            output.write(" ");
            output.write(message);
            output.write(StreamUtil.getLineSeparator());
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }

    protected void outputStatement(Statement sql, ExecutionEnvironment env) throws DatabaseException {
        try {
            if (StatementLogicFactory.getInstance().generateActionsIsVolatile(sql, env)) {
                throw new DatabaseException(sql.getClass().getSimpleName()+" requires access to up to date database metadata which is not available in SQL output mode");
            }
            for (Action action : StatementLogicFactory.getInstance().generateActions(sql, env)) {
                if (action == null) {
                    continue;
                }

                output.write(action.describe());
                output.write(StreamUtil.getLineSeparator());
                output.write(StreamUtil.getLineSeparator());
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        } catch (UnsupportedException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public QueryResult query(Statement sql, ExecutionEnvironment env) throws DatabaseException, UnsupportedException {
        if (sql instanceof SelectFromDatabaseChangeLogLockStatement) {
            return new QueryResult(Boolean.FALSE);
        }
        try {
            return delegatedReadExecutor.query(sql, env);
        } catch (DatabaseException e) {
            if (sql instanceof GetNextChangeSetSequenceValueStatement) { //table probably does not exist
                return new QueryResult(0);
            }
            throw e;
        }
    }

    @Override
    public boolean updatesDatabase() {
        return false;
    }
}
