package liquibase.lockservice;

import java.util.Date;

/**
 * Information about the database changelog lock which allows only one instance of Liquibase to
 * attempt to
 * update a database at a time. Immutable class
 */
public class DatabaseChangeLogLock {
    private final int id;
    private final Date lockGranted;
    private final Date lockExpires;

    /** Some information about who locked (e.g. host name) */
    private final String lockedBy;

    /**
     * Globally unique ID of the service instance (unique per JVM) that holds the lock, in case
     * we use the prolonging lock service.
     */
    private final String lockedById;

    public DatabaseChangeLogLock(int id, Date lockGranted, Date lockExpires, String lockedBy,
                                 String lockedById) {
        this.id = id;
        this.lockGranted = new Date(lockGranted.getTime());
        this.lockExpires = lockExpires;
        this.lockedBy = lockedBy;
        this.lockedById = lockedById;
    }

    public int getId() {
        return id;
    }

    public Date getLockGranted() {
        return (Date) lockGranted.clone();
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public Date getLockExpires() {
        return lockExpires;
    }

    public String getLockedById() {
        return lockedById;
    }
}
