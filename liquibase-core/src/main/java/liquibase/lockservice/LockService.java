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
