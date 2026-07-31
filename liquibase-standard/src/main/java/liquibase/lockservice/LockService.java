package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.servicelocator.PrioritizedService;

public interface LockService extends PrioritizedService {

    boolean supports(Database database);

    void setDatabase(Database database);

    void setChangeLogLockWaitTime(long changeLogLockWaitTime);

    void setChangeLogLockRecheckTime(long changeLogLockRecheckTime);

    boolean hasChangeLogLock();

    void waitForLock() throws LockException;

    boolean acquireLock() throws LockException;

    void releaseLock() throws LockException;

    DatabaseChangeLogLock[] listLocks() throws LockException;

    /**
     * Releases whatever locks are on the database change log table.
     *
     * <p>ADR-0005 (INT-2205 phase 2): the clause is widened from {@code DatabaseException} to
     * {@code LiquibaseException} so a classified business/system failure can flow through this SPI.
     * {@code LockException} stays explicit for the documented lock-specific contract. Existing
     * implementors may keep a narrower {@code throws DatabaseException} override unchanged
     * (source-compatible); only strict {@code catch (DatabaseException)} callers must widen.
     */
    void forceReleaseLock() throws LockException, LiquibaseException;

    /**
     * Clears information the lock handler knows about the tables.  Should only be called by Liquibase internal calls
     */
    void reset();

    /** @throws LiquibaseException if the lock table cannot be created or initialized (ADR-0005: widened from {@code DatabaseException}). */
    void init() throws LiquibaseException;

    /** @throws LiquibaseException if the lock table cannot be dropped (ADR-0005: widened from {@code DatabaseException}). */
    void destroy() throws LiquibaseException;
}
