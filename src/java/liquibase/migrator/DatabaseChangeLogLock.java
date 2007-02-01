package liquibase.migrator;

import java.util.Date;

public class DatabaseChangeLogLock {
    private int id;
    private Date lockGranted;
    private String lockedBy;

    public DatabaseChangeLogLock(int id, Date lockGranted, String lockedBy) {
        this.id = id;
        this.lockGranted = lockGranted;
        this.lockedBy = lockedBy;
    }

    public int getId() {
        return id;
    }

    public Date getLockGranted() {
        return lockGranted;
    }

    public String getLockedBy() {
        return lockedBy;
    }
}
