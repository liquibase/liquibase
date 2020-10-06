package liquibase.lockservice;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.plugin.Plugin;

import java.util.Date;

/**
 * A no-op lock service. It will always say the lock has been granted.
 * Should only be used if you are understanding the implications.
 * The service is used when liquibase.changeLogLockEnabled = false
 */
public class NoLockService extends AbstractLockService {

    @Override
    public int getPriority() {
        if (!LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getDatabaseChangeLogLockEnabled()) {
            return Plugin.PRIORITY_SPECIALIZED + 100;
        }
        return Plugin.PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public boolean supports(Database database) {
        return true;
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
        return new DatabaseChangeLogLock[] {
                new DatabaseChangeLogLock(1, new Date(), "Lock service disabled")
        };
    }

    @Override
    public void forceReleaseLock() throws LockException, DatabaseException {
    }

}
