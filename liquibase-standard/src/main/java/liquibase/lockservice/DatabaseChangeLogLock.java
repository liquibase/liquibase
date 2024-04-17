package liquibase.lockservice;

import lombok.Getter;

import java.util.Date;

/**
 * Information about the database changelog lock which allows only one instance of Liquibase to attempt to
 * update a database at a time. Immutable class
 */
public class DatabaseChangeLogLock {
    @Getter
    private final int id;
    private final Date lockGranted;
    @Getter
    private final String lockedBy;

    public DatabaseChangeLogLock(int id, Date lockGranted, String lockedBy) {
        this.id = id;
        this.lockGranted = new Date(lockGranted.getTime());
        this.lockedBy = lockedBy;
    }

    public Date getLockGranted() {
        return (Date) lockGranted.clone();
    }

}
