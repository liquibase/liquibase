package liquibase.executor.jvm;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcValue;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.util.SqlUtil;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A wrapper utility class around the standard JdbcExecutor used to monitor and log sql from Jdbc queries
 */
public class ChangelogJdbcMdcListener {

    /**
     * Execute the given statement via the jdbc executor. Adds MDC of the statement sql and outcome to logging.
     *
     * @param database  the database to execute against
     * @param jdbcQuery the executor function to apply
     * @throws DatabaseException if there was a problem running the sql statement
     */
    public static void execute(Database database, ExecuteJdbc jdbcQuery) throws DatabaseException {
        try {
            AtomicReference<Sql[]> sqls = new AtomicReference<>(null);
            Scope.child(Collections.singletonMap(SqlGeneratorFactory.GENERATED_SQL_ARRAY_SCOPE_KEY, sqls), () -> {
                jdbcQuery.execute(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database));
            });
            addSqlMdc(sqls);
            addSqlToScope(sqls, database);
            logSuccess();
        } catch (Exception e) {
            Scope.getCurrentScope().addMdcValue(MdcKey.DATABASE_CHANGELOG_TABLE_OUTCOME, MdcValue.DATABASE_CHANGELOG_OUTCOME_FAILED);
            throw new DatabaseException(e);
        }
    }

    /**
     * Execute the given statement via the jdbc executor. Adds MDC of the statement sql and outcome to logging.
     *
     * @param database  the database to execute against
     * @param jdbcQuery the executor function to apply
     * @return the result of the executor function
     * @throws DatabaseException if there was a problem running the sql statement
     */
    public static <T> T query(Database database, QueryJdbc<T> jdbcQuery) throws DatabaseException {
        try {
            AtomicReference<Sql[]> sqls = new AtomicReference<>(null);
            T value = Scope.child(Collections.singletonMap(SqlGeneratorFactory.GENERATED_SQL_ARRAY_SCOPE_KEY, sqls), () -> jdbcQuery.execute(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)));
            addSqlMdc(sqls);
            addSqlToScope(sqls, database);
            logSuccess();
            return value;
        } catch (Exception e) {
            Scope.getCurrentScope().addMdcValue(MdcKey.DATABASE_CHANGELOG_TABLE_OUTCOME, MdcValue.DATABASE_CHANGELOG_OUTCOME_FAILED);
            throw new DatabaseException(e);
        }
    }

    private static void addSqlMdc(AtomicReference<Sql[]> sqlsRef) {
        if (sqlsRef != null) {
            Sql[] sqls = sqlsRef.get();
            if (sqls != null) {
                Scope.getCurrentScope().addMdcValue(MdcKey.DATABASE_CHANGELOG_SQL, SqlUtil.convertSqlArrayToString(sqls));
            }
        }
    }

    private static void addSqlToScope(AtomicReference<Sql[]> sqlsRef, Database database) {
        if (sqlsRef != null) {
            Sql[] sqls = sqlsRef.get();
            if (sqls != null) {
                database.addCompleteSqlToScope(SqlUtil.convertSqlArrayToString(sqls));
            }
        }
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
