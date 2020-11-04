package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.LockService;
import liquibase.database.core.MockDatabase;

/**
 * @author John Sanda
 */
public class MockLockService implements LockService {
    @Override
    public boolean supports(Database database) {
        return database instanceof MockDatabase;
    }

    @Override
    public void init() throws DatabaseException {

    }

    @Override
    public void setDatabase(Database database) {
    }

    @Override
    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
    }

    @Override
    public void setChangeLogLockRecheckTime(long changeLogLocRecheckTime) {
    }

    @Override
    public boolean hasChangeLogLock() {
        return false;
    }

    @Override
    public void waitForLock() throws LockException {
    }

    @Override
    public boolean acquireLock() throws LockException {
        return false;
    }

    @Override
    public void releaseLock() throws LockException {
    }

    @Override
    public DatabaseChangeLogLock[] listLocks() throws LockException {
        return new DatabaseChangeLogLock[0];
    }

    @Override
    public void forceReleaseLock() throws LockException, DatabaseException {
    }

    @Override
    public void reset() {
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public void destroy() throws DatabaseException {

    }
}
