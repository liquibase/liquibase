package liquibase;

import liquibase.migrator.Migrator;
import liquibase.preconditions.AndPrecondition;

/**
 * Encapsulates the information stored in the change log XML file.
 */
public class DatabaseChangeLog implements Comparable<DatabaseChangeLog> {
    private Migrator migrator;
    private AndPrecondition preconditions;
    private String physicalFilePath;
    private String logicalFilePath;

    public DatabaseChangeLog(Migrator migrator, String physicalFilePath) {
        this.migrator = migrator;
        this.physicalFilePath = physicalFilePath;
    }

    public Migrator getMigrator() {
        return migrator;
    }

    public AndPrecondition getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(AndPrecondition precond) {
        preconditions = precond;
    }

    public String getPhysicalFilePath() {
        return physicalFilePath;
    }

    public void setPhysicalFilePath(String physicalFilePath) {
        this.physicalFilePath = physicalFilePath;
    }

    public String getLogicalFilePath() {
        return logicalFilePath;
    }

    public void setLogicalFilePath(String logicalFilePath) {
        this.logicalFilePath = logicalFilePath;
    }

    public String getFilePath() {
        if (logicalFilePath == null) {
            return physicalFilePath;
        } else {
            return logicalFilePath;
        }
    }

    public String toString() {
        return getFilePath();
    }

    public int compareTo(DatabaseChangeLog o) {
        return getFilePath().compareTo(o.getFilePath());
    }
}
