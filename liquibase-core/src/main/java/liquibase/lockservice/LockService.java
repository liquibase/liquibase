package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.servicelocator.PrioritizedService;

/**
 * @author John Sanda
 */
public interface LockService extends PrioritizedService {

    boolean supports(Database database);

    void setDatabase(Database database);

    void setChangeLogLockWaitTime(long changeLogLockWaitTime);

    void setChangeLogLockRecheckTime(long changeLogLocRecheckTime);

    /** For lock services that actively prolong the lock, specify the rate here.  */
    void setChangeLogLockProlongingRateInSeconds(long changeLogLockProlongingRateInSeconds);

    /**
     * For lock services that actively prolong the lock, specify after what time a stale lock
     * should be removed.
     *
     * Must be a good amount larger than the time set in {@link #setChangeLogLockProlongingRateInSeconds(long)},
     * otherwise you will remove a lock that another service is about to prolong.
     *
     * Ideally add 40 seconds to the prolonging rate for the staleChangeLogLockRemovalTime.
     */
    void setStaleChangeLogLockRemovalTimeInSeconds(Long staleChangeLogLockRemovalTimeInSeconds);

    boolean hasChangeLogLock();

    void waitForLock() throws LockException;

    boolean acquireLock() throws LockException;

    void releaseLock() throws LockException;

    DatabaseChangeLogLock[] listLocks() throws LockException;

    /**
     * Releases whatever locks are on the database change log table
     */
    void forceReleaseLock() throws LockException, DatabaseException;

    /**
     * Clears information the lock handler knows about the tables.  Should only be called by Liquibase internal calls
     */
    void reset();

    void init() throws DatabaseException;

    void destroy() throws DatabaseException;
}
