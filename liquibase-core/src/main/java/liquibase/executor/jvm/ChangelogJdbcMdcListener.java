package liquibase.executor.jvm;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcValue;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.util.SqlUtil;

/**
 * A wrapper utility class around the standard JdbcExecutor used to monitor and log sql from Jdbc queries
 */
public class ChangelogJdbcMdcListener {

    /**
     * Execute the given statement via the jdbc executor. Adds MDC of the statement sql and outcome to logging.
     *
     * @param statement the statement to execute
     * @param database  the database to execute against
     * @param jdbcQuery the executor function to apply
     * @throws DatabaseException if there was a problem running the sql statement
     */
    public static void execute(SqlStatement statement, Database database, ExecuteJdbc jdbcQuery) throws DatabaseException {
        if (!(statement instanceof MarkChangeSetRanStatement)) {
            addSqlMdc(statement, database);
        }
        try {
            jdbcQuery.execute(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database));
            logSuccess();
        } catch (DatabaseException e) {
            Scope.getCurrentScope().addMdcValue(MdcKey.DATABASE_CHANGELOG_TABLE_OUTCOME, MdcValue.DATABASE_CHANGELOG_OUTCOME_FAILED);
            throw new DatabaseException(e);
        }
    }

    /**
     * Execute the given statement via the jdbc executor. Adds MDC of the statement sql and outcome to logging.
     *
     * @param statement the statement to execute
     * @param database  the database to execute against
     * @param jdbcQuery the executor function to apply
     * @return the result of the executor function
     * @throws DatabaseException if there was a problem running the sql statement
     */
    public static <T> T query(SqlStatement statement, Database database, QueryJdbc<T> jdbcQuery) throws DatabaseException {
        addSqlMdc(statement, database);
        try {
            T value = jdbcQuery.execute(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database));
            logSuccess();
            return value;
        } catch (DatabaseException e) {
            Scope.getCurrentScope().addMdcValue(MdcKey.DATABASE_CHANGELOG_TABLE_OUTCOME, MdcValue.DATABASE_CHANGELOG_OUTCOME_FAILED);
            throw new DatabaseException(e);
        }
    }

    private static void addSqlMdc(SqlStatement statement, Database database) {
        Scope.getCurrentScope().addMdcValue(MdcKey.DATABASE_CHANGELOG_SQL, SqlUtil.getSqlString(statement, SqlGeneratorFactory.getInstance(), database));
    }

    private static void logSuccess() {
        Scope.getCurrentScope().addMdcValue(MdcKey.DATABASE_CHANGELOG_TABLE_OUTCOME, MdcValue.DATABASE_CHANGELOG_OUTCOME_SUCCESS);
        Scope.getCurrentScope().getLog(ChangelogJdbcMdcListener.class).fine("Changelog query completed.");
        Scope.getCurrentScope().getMdcManager().remove(MdcKey.DATABASE_CHANGELOG_TABLE_OUTCOME);
        Scope.getCurrentScope().getMdcManager().remove(MdcKey.DATABASE_CHANGELOG_SQL);
    }

    public interface QueryJdbc<T> {
        T execute(Executor executor) throws DatabaseException;
    }

    public interface ExecuteJdbc {
        void execute(Executor executor) throws DatabaseException;
    }
}
