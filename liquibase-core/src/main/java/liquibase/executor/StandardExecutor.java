package liquibase.executor;

import liquibase.exception.DatabaseException;
import liquibase.executor.*;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.sql.*;
import liquibase.statement.*;

public class StandardExecutor extends AbstractExecutor {

    private Logger log = LogFactory.getLogger();

    @Override
    public boolean updatesDatabase() {
        return true;
    }

    @Override
    public QueryResult query(SqlStatement sql, ExecutionOptions options) throws DatabaseException {
        Executable[] executables = generateExecutables(sql);

        if (executables.length != 1) {
            throw new DatabaseException("Can only query with statements that return one executable");
        }

        Executable executable = executables[0];

        if (!(executable instanceof ExecutableQuery)) {
            throw new DatabaseException("Cannot query "+executable.getClass().getName());
        }

        return ((ExecutableQuery) executable).query(options);
    }


    @Override
    public ExecuteResult execute(SqlStatement sql, ExecutionOptions options) throws DatabaseException {
        Executable[] executables = generateExecutables(sql);
        for (Executable executable : executables) {
            if (!(executable instanceof ExecutableExecute)) {
                throw new DatabaseException("Cannot execute "+executable.getClass().getName());
            }
        }
        for (Executable executable : executables) {
            ((ExecutableExecute) executable).execute(options);
        }
        return new ExecuteResult();
    }


    @Override
    public UpdateResult update(SqlStatement sql, ExecutionOptions options) throws DatabaseException {
        Executable[] executables = generateExecutables(sql);

        if (executables.length != 1) {
            throw new DatabaseException("Can only update with statements that return one executable");
        }

        Executable executable = executables[0];

        if (!(executable instanceof ExecutableUpdate)) {
            throw new DatabaseException("Cannot update "+executable.getClass().getName());
        }

        return ((ExecutableUpdate) executable).update(options);
    }

    @Override
    public void comment(String message) throws DatabaseException {
        LogFactory.getLogger().debug(message);
    }
}
