package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;

public class OfflineLockService implements LockService {

    private Database database;
    private boolean hasChangeLogLock;

    @Override
    public int getPriority() {
        return 5000;
    }

    @Override
    public boolean supports(Database database) {
        return (database.getConnection() != null) && (database.getConnection() instanceof OfflineConnection);
    }

    @Override
    public void init() throws DatabaseException {

    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {

    }

    @Override
    public void setChangeLogLockRecheckTime(long changeLogLocRecheckTime) {

    }

    @Override
    public boolean hasChangeLogLock() {
        return this.hasChangeLogLock;
    }

    @Override
    public void waitForLock() throws LockException {

    }

    @Override
    public boolean acquireLock() throws LockException {
        this.hasChangeLogLock = true;
        return true;
    }

    @Override
    public void releaseLock() throws LockException {
        this.hasChangeLogLock = false;
    }

    @Override
    public DatabaseChangeLogLock[] listLocks() throws LockException {
        return new DatabaseChangeLogLock[0];
    }

    @Override
    public void forceReleaseLock() throws LockException, DatabaseException {
        this.hasChangeLogLock = false;
    }

    @Override
    public void reset() {
        this.hasChangeLogLock = false;
    }

    @Override
    public void destroy() throws DatabaseException {
        //nothign to do
    }
}
