package liquibase.migrator;

import liquibase.migrator.preconditions.PreconditionSet;

/**
 * Encapsulates the information stored in the change log XML file.
 */
public class DatabaseChangeLog {
    private Migrator migrator;
    private PreconditionSet preconditions;
    private String physicalFilePath;
    private String logicalFilePath;

    public DatabaseChangeLog(Migrator migrator, String physicalFilePath) {
        this.migrator = migrator;
        this.physicalFilePath = physicalFilePath;
    }

    public Migrator getMigrator() {
        return migrator;
    }

    public PreconditionSet getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(PreconditionSet precond) {
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
        return getPhysicalFilePath();
    }
}
