package liquibase.statement.core;

import java.util.Date;

import liquibase.statement.AbstractSqlStatement;

public class ProlongDatabaseChangeLogLockStatement extends AbstractSqlStatement {

    /** The unique id of this instance, to make sure we are not fiddling round with sb elses lock. */
    private final String lockedById;

    /**
     * When lock will be actively prolonged, set the date (with time from database server, so we
     * can directly compare there using current_timestamp!) when the lock will expire.
     */
    private final Date lockExpiresOnServer;

    public ProlongDatabaseChangeLogLockStatement(String lockedById,
                                                 Date lockExpiresOnServer) {
        this.lockedById = lockedById;
        this.lockExpiresOnServer = lockExpiresOnServer;
    }

    public Date getLockExpiresOnServer() {
        return lockExpiresOnServer;
    }

    public String getLockedById() {
        return lockedById;
    }
}
