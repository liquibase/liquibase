package liquibase.lockservice;

import java.util.Date;

/**
 * Information about the database changelog lock which allows only one instance of Liquibase to attempt to
 * update a database at a time. Immutable class
 */
public class DatabaseChangeLogLock {
    private final int id;
    private final Date lockGranted;
    private final String lockedBy;

    public DatabaseChangeLogLock(int id, Date lockGranted, String lockedBy) {
        this.id = id;
        this.lockGranted = new Date(lockGranted.getTime());
        this.lockedBy = lockedBy;
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
}
