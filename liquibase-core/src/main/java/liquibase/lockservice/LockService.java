package liquibase.lockservice;

import liquibase.ExtensibleObject;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.plugin.Plugin;
import liquibase.servicelocator.PrioritizedService;

public interface LockService extends Plugin, ExtensibleObject, PrioritizedService, AutoCloseable {

    boolean supports(Database database);

    void setDatabase(Database database);

    void setChangeLogLockWaitTime(long changeLogLockWaitTime);

    void setChangeLogLockRecheckTime(long changeLogLocRecheckTime);

    void waitForLock() throws LockException;

    boolean acquireLock() throws LockException;

    void releaseLock() throws LockException;

    DatabaseChangeLogLock[] listLocks() throws LockException;

    /**
     * Releases whatever locks are on the database change log table
     */
    void forceReleaseLock() throws LockException, DatabaseException;

    /**
     * Performs any preparation needed prior to using this lock service.
     */
    void init() throws DatabaseException;

    /**
     * Clears information the lock handler knows about the tables.  Should only be called by Liquibase internal calls
     */
    void reset();


    /**
     * Closes any resources used by this lock service.
     */
    void close() throws DatabaseException;
}
