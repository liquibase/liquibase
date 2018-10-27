package liquibase.statement.core;

import java.util.Date;

import liquibase.statement.AbstractSqlStatement;

public class LockDatabaseChangeLogStatement extends AbstractSqlStatement {

    /** Whether this lock will be actively prolonged or not */
    private final boolean prolongedLock;

    /** The unique id of this instance, to make sure we are not fiddling round with sb elses lock. */
    private final String lockedById;

    /**
     * When lock will be actively prolonged, set the date (with time from database server, so we
     * can directly compare there using current_timestamp!) when the lock will expire.
     */
    private final Date lockExpiresOnServer;

    /**
     * For standard lock service: lock does not get prolonged actively, and we do not
     * have a locked-by id.
     */
    public static LockDatabaseChangeLogStatement forStandardLockService() {
        return new LockDatabaseChangeLogStatement(false, null, null);
    }

    /**
     * For prolonging lock service, sets field with timestamp when lock was prolonged the last time,
     * and id of the prolonging service
     *
     * @param lockedById the globally unique id of this service
     * @param lockExpiresOnServer
     * @return the statement
     */
    public static LockDatabaseChangeLogStatement forProlongingLockService(String lockedById,
                                                                          Date lockExpiresOnServer) {
        if (lockedById == null) {
            throw new IllegalArgumentException("lockedById cannot be NULL.");
        }

        if (lockExpiresOnServer == null) {
            throw new IllegalArgumentException("lockExpiresOnServer cannot be NULL.");
        }

        return new LockDatabaseChangeLogStatement(true, lockedById, lockExpiresOnServer);
    }

    private LockDatabaseChangeLogStatement(boolean prolongedLock,
                                           String lockedById,
                                           Date lockExpiresOnServer) {
        this.prolongedLock = prolongedLock;
        this.lockedById = lockedById;
        this.lockExpiresOnServer = lockExpiresOnServer;
    }

    public boolean isProlongedLock() {
        return prolongedLock;
    }

    public String getLockedById() {
        return lockedById;
    }

    public Date getLockExpiresOnServer() {
        return lockExpiresOnServer;
    }
}
