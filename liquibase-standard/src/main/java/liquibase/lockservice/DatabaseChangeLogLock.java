package liquibase.lockservice;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Information about the database changelog lock which allows only one instance of Liquibase to attempt to
 * update a database at a time. Immutable class
 */
@NoArgsConstructor
public class DatabaseChangeLogLock {
    @Getter
    @Setter
    private int id;
    private Date lockGranted;
    @Getter
    @Setter
    private String lockedBy;

    public DatabaseChangeLogLock(int id, Date lockGranted, String lockedBy) {
        this.id = id;
        this.lockGranted = new Date(lockGranted.getTime());
        this.lockedBy = lockedBy;
    }

    public Date getLockGranted() {
        return (Date) lockGranted.clone();
    }

    public void setLockGranted(final Date lockGranted) {
        this.lockGranted = new Date(lockGranted.getTime());
    }

}
