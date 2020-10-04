package liquibase.lockservice;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.servicelocator.PrioritizedService;
import liquibase.statement.core.RawSqlStatement;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

public class PostgresAdvisoryLockService implements LockService {

    private static final Logger LOGGER = Logger.getLogger(PostgresAdvisoryLockService.class.getName());

    private static final int LOCK_NAMESPACE_ID = "liquibase".hashCode();
    private static final int LOCK_ID = "lockservice".hashCode();

    static final RawSqlStatement TRY_ACQUIRE_LOCK_SQL =
        new RawSqlStatement(String.format("select pg_try_advisory_lock(%d,%d)", LOCK_NAMESPACE_ID, LOCK_ID));

    static final RawSqlStatement RELEASE_LOCK_SQL =
        new RawSqlStatement(String.format("select pg_advisory_unlock(%d,%d)", LOCK_NAMESPACE_ID, LOCK_ID));

    private Database database;
    private boolean hasChangeLogLock;
    private long changeLogLockWaitTime;
    private long changeLogLocRecheckTime;

    public PostgresAdvisoryLockService() {
        this.changeLogLockWaitTime = LiquibaseConfiguration.getInstance()
            .getConfiguration(GlobalConfiguration.class)
            .getDatabaseChangeLogLockWaitTime();
        this.changeLogLocRecheckTime = LiquibaseConfiguration.getInstance()
            .getConfiguration(GlobalConfiguration.class)
            .getDatabaseChangeLogLockPollRate();
    }

    boolean tryAcquireLock(Executor executor) throws LockException {
        try {
            final Boolean tryLock = executor.queryForObject(TRY_ACQUIRE_LOCK_SQL, Boolean.class);
            if (tryLock) {
                LOGGER.info("Successfully acquired change log lock");
            }
            return tryLock;
        } catch (DatabaseException e) {
            throw new LockException(e);
        }
    }

    @Override
    public boolean supports(final Database database) {
        final boolean isPostgres = database instanceof PostgresDatabase;
        if (!isPostgres) {
            return false;
        }
        final Boolean useDbLock = LiquibaseConfiguration.getInstance()
            .getConfiguration(GlobalConfiguration.class)
            .getUseDbLock();
        try {
            // Only works with Postgres version >= 9.1
            return useDbLock && isAtLeastPostgres91(database);
        } catch (DatabaseException e) {
            return false;
        }
    }

    boolean isAtLeastPostgres91(final Database database) throws DatabaseException {
        return (database.getDatabaseMajorVersion() > 9) ||
            (database.getDatabaseMajorVersion() == 9 && database.getDatabaseMinorVersion() >= 1);
    }

    @Override
    public void setDatabase(final Database database) {
        this.database = database;
    }

    @Override
    public void setChangeLogLockWaitTime(final long changeLogLockWaitTime) {
        this.changeLogLockWaitTime = changeLogLockWaitTime;
    }

    public long getChangeLogLockWaitTime() {
        return changeLogLockWaitTime;
    }

    @Override
    public void setChangeLogLockRecheckTime(final long changeLogLocRecheckTime) {
        this.changeLogLocRecheckTime = changeLogLocRecheckTime;
    }

    public long getChangeLogLocRecheckTime() {
        return changeLogLocRecheckTime;
    }

    @Override
    public boolean hasChangeLogLock() {
        return hasChangeLogLock;
    }

    @Override
    public void waitForLock() throws LockException {
        boolean locked = false;
        long timeToGiveUp = Instant.now().plus(Duration.ofMinutes(changeLogLockWaitTime)).toEpochMilli();
        while (!locked && System.currentTimeMillis() < timeToGiveUp) {
            locked = acquireLock();
            if (!locked) {
                LOGGER.info("Waiting for changelog lock....");
                try {
                    Thread.sleep(Duration.ofSeconds(changeLogLocRecheckTime).toMillis());
                } catch (InterruptedException e) {
                }
            }
        }

        if (!locked) {
            throw new LockException("Could not acquire change log lock.");
        }
    }

    @Override
    public boolean acquireLock() throws LockException {
        return hasChangeLogLock = tryAcquireLock(getExecutor());
    }

    Executor getExecutor() {
        return ExecutorService.getInstance().getExecutor(database);
    }

    @Override
    public void releaseLock() throws LockException {
        try {
            getExecutor().queryForObject(RELEASE_LOCK_SQL, Boolean.class);
        } catch (DatabaseException e) {
            throw new LockException(e);
        }
    }

    @Override
    public DatabaseChangeLogLock[] listLocks() {
        return new DatabaseChangeLogLock[0];
    }

    @Override
    public void forceReleaseLock() {
    }

    @Override
    public void reset() {
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
    }

    /**
     * Prioritize this implementation over {@link StandardLockService}.
     */
    @Override
    public int getPriority() {
        return PrioritizedService.PRIORITY_DEFAULT + 1;
    }
}
