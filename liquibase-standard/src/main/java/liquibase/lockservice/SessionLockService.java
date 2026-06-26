package liquibase.lockservice;

import java.sql.Connection;
import java.text.DateFormat;
import java.util.Date;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;

/**
 * Base class for {@link LockService} implementations that guard the changelog with a
 * <i>session-level</i> lock held on the database connection, rather than with the committed
 * {@code LOCKED} row in {@code DATABASECHANGELOGLOCK} used by {@link StandardLockService}.
 * <p>
 * The table-based lock is committed, so if the process dies mid-{@code update} (an OOM kill, a
 * container eviction, a startup probe firing, a {@code kill -9}) the row is left {@code LOCKED} and
 * every later run fails with <i>"Could not acquire change log lock"</i> until {@code releaseLocks}
 * is run by hand. A session-level lock lives on the connection instead, and the database releases
 * it automatically when that session ends, so a killed process never leaves a stale lock. The lock
 * is session-scoped (not transaction-scoped), so it survives the intermediate commits Liquibase
 * performs during a migration.
 * <p>
 * This base is not selected on its own; concrete subclasses gate themselves on
 * {@code liquibase.useSessionLock} via {@link #supports(Database)} so {@link StandardLockService}
 * remains the default and behaviour is unchanged unless the property is enabled.
 */
public abstract class SessionLockService implements LockService {

    protected Database database;
    protected boolean hasChangeLogLock;
    // Null means "not set explicitly", in which case the global configuration is consulted; an
    // explicit setChangeLogLock* call wins. Mirrors StandardLockService.
    protected Long changeLogLockWaitTimeMinutes;
    protected Long changeLogLockRecheckTimeSeconds;

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    /** {@inheritDoc} This implementation returns {@code false}; subclasses narrow it to their dialect. */
    @Override
    public boolean supports(Database database) {
        return false;
    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
        this.changeLogLockWaitTimeMinutes = changeLogLockWaitTime;
    }

    @Override
    public void setChangeLogLockRecheckTime(long changeLogLockRecheckTime) {
        this.changeLogLockRecheckTimeSeconds = changeLogLockRecheckTime;
    }

    /** Wait timeout in minutes: the explicit setter wins, otherwise the global configuration. */
    protected long getChangeLogLockWaitTimeMinutes() {
        if (changeLogLockWaitTimeMinutes != null) {
            return changeLogLockWaitTimeMinutes;
        }
        return GlobalConfiguration.CHANGELOGLOCK_WAIT_TIME.getCurrentValue();
    }

    /** Recheck interval in seconds: the explicit setter wins, otherwise the global configuration. */
    protected long getChangeLogLockRecheckTimeSeconds() {
        if (changeLogLockRecheckTimeSeconds != null) {
            return changeLogLockRecheckTimeSeconds;
        }
        return GlobalConfiguration.CHANGELOGLOCK_POLL_RATE.getCurrentValue();
    }

    @Override
    public boolean hasChangeLogLock() {
        return hasChangeLogLock;
    }

    @Override
    public void init() {
        // A session lock needs no DATABASECHANGELOGLOCK table.
    }

    @Override
    public boolean acquireLock() throws LockException {
        // The hasChangeLogLock guard means we acquire at most once per service instance. Session
        // advisory locks are re-entrant (a per-session reference count), so acquiring twice would
        // need two releases; acquiring once keeps the count at 1 and a single release frees it.
        if (hasChangeLogLock) {
            return true;
        }
        boolean acquired = acquireLock(getConnection());
        // The lock SQL runs inside Liquibase's implicit (autoCommit=false) transaction. A
        // session-level lock is unaffected by transaction boundaries, but leaving the transaction
        // open would hold catalog locks and start the migration's first DML mid-transaction. End
        // it now, mirroring StandardLockService which brackets its lock work with rollback().
        endTransaction();
        if (acquired) {
            hasChangeLogLock = true;
            Scope.getCurrentScope().getLog(getClass()).info("Successfully acquired change log lock");
            return true;
        }
        return false;
    }

    @Override
    public void releaseLock() throws LockException {
        if (!hasChangeLogLock) {
            return;
        }
        // Only clear the flag once the unlock actually succeeds; if it throws, we still hold the
        // session lock and must not let Liquibase think otherwise.
        releaseLock(getConnection());
        endTransaction();
        hasChangeLogLock = false;
        Scope.getCurrentScope().getLog(getClass()).info("Successfully released change log lock");
    }

    @Override
    public void waitForLock() throws LockException {
        boolean locked = acquireLock();
        long giveUpAtMillis = new Date().getTime() + (getChangeLogLockWaitTimeMinutes() * 60 * 1000);
        while (!locked && new Date().getTime() < giveUpAtMillis) {
            Scope.getCurrentScope().getLog(getClass()).info("Waiting for changelog lock....");
            sleepRecheckInterval();
            locked = acquireLock();
        }
        if (!locked) {
            throw new LockException("Could not acquire change log lock.  Currently locked by " + describeCurrentLock());
        }
    }

    @Override
    public void forceReleaseLock() throws LockException {
        releaseLock(getConnection());
        endTransaction();
        hasChangeLogLock = false;
    }

    @Override
    public void reset() {
        if (hasChangeLogLock) {
            try {
                forceReleaseLock();
            } catch (LockException e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Could not release change log lock on reset", e);
            }
        }
    }

    @Override
    public void destroy() {
        reset();
    }

    /**
     * Acquires the session lock on the given connection.
     *
     * @return {@code true} if the lock was obtained, {@code false} if it is held by another session.
     */
    protected abstract boolean acquireLock(Connection connection) throws LockException;

    /** Releases the session lock previously obtained by {@link #acquireLock(Connection)}. */
    protected abstract void releaseLock(Connection connection) throws LockException;

    protected Connection getConnection() throws LockException {
        DatabaseConnection databaseConnection = database.getConnection();
        if (databaseConnection instanceof JdbcConnection) {
            return ((JdbcConnection) databaseConnection).getUnderlyingConnection();
        }
        throw new LockException("Expected a JdbcConnection but was " + databaseConnection);
    }

    private String describeCurrentLock() throws LockException {
        DatabaseChangeLogLock[] locks = listLocks();
        if (locks.length == 0) {
            return "UNKNOWN";
        }
        DatabaseChangeLogLock currentLock = locks[0];
        String grantedAt = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(currentLock.getLockGranted());
        return currentLock.getLockedBy() + " since " + grantedAt;
    }

    /**
     * Ends Liquibase's implicit transaction so a session-lock acquire/release does not leave a
     * transaction open. The session-level lock itself is unaffected by the rollback.
     */
    private void endTransaction() throws LockException {
        try {
            database.rollback();
        } catch (DatabaseException e) {
            throw new LockException(e);
        }
    }

    private void sleepRecheckInterval() throws LockException {
        try {
            Thread.sleep(getChangeLogLockRecheckTimeSeconds() * 1000);
        } catch (InterruptedException e) {
            // Preserve the interrupt and abort the wait loop: otherwise the next sleep would throw
            // immediately and waitForLock() would hot-spin on lock polls until the full timeout.
            Thread.currentThread().interrupt();
            throw new LockException("Interrupted while waiting for change log lock", e);
        }
    }
}
