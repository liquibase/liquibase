package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.plugin.Plugin;

public class OfflineLockService extends AbstractLockService {

    @Override
    public int getPriority() {
        return Plugin.PRIORITY_SPECIALIZED;
    }

    @Override
    public boolean supports(Database database) {
        final DatabaseConnection connection = database.getConnection();

        return connection == null || connection instanceof OfflineConnection;
    }

    @Override
    public boolean acquireLock() throws LockException {
        return true;
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

}
