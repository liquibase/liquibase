package liquibase.executor;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.servicelocator.LiquibaseService;
import liquibase.sql.Executable;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
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
    public ExecuteResult execute(SqlStatement sql, ExecutionOptions options) throws DatabaseException {
        outputStatement(sql, options);
        return new ExecuteResult();
    }

    @Override
    public UpdateResult update(SqlStatement sql, ExecutionOptions options) throws DatabaseException {
        if (sql instanceof LockDatabaseChangeLogStatement) {
            return new UpdateResult(1);
        } else if (sql instanceof UnlockDatabaseChangeLogStatement) {
            return new UpdateResult(1);
        }

        outputStatement(sql, options);

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

    protected void outputStatement(SqlStatement sql, ExecutionOptions options) throws DatabaseException {
        try {
            if (SqlGeneratorFactory.getInstance().generateStatementsVolatile(sql, database)) {
                throw new DatabaseException(sql.getClass().getSimpleName()+" requires access to up to date database metadata which is not available in SQL output mode");
            }
            for (Executable executable : generateExecutables(sql)) {
                if (executable == null) {
                    continue;
                }

                output.write(executable.toString(options));
                output.write(StreamUtil.getLineSeparator());
                output.write(StreamUtil.getLineSeparator());
            }
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public QueryResult query(SqlStatement sql, ExecutionOptions options) throws DatabaseException {
        if (sql instanceof SelectFromDatabaseChangeLogLockStatement) {
            return new QueryResult(Boolean.FALSE);
        }
        try {
            return delegatedReadExecutor.query(sql, options);
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
