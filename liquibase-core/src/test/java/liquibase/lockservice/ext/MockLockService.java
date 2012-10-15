package liquibase.lockservice.ext;

import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.LockService;

/**
 * @author John Sanda
 */
public class MockLockService implements LockService {
    public boolean supports(Database database) {
        return database instanceof MockDatabase;
    }

    public void setDatabase(Database database) {
    }

    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
    }

    public void setChangeLogLockRecheckTime(long changeLogLocRecheckTime) {
    }

    public boolean hasChangeLogLock() {
        return false;
    }

    public void waitForLock() throws LockException {
    }

    public boolean acquireLock() throws LockException {
        return false;
    }

    public void releaseLock() throws LockException {
    }

    public DatabaseChangeLogLock[] listLocks() throws LockException {
        return new DatabaseChangeLogLock[0];
    }

    public void forceReleaseLock() throws LockException, DatabaseException {
    }

    public void reset() {
    }

    public int getPriority() {
        return PRIORITY_DATABASE;
    }
}
