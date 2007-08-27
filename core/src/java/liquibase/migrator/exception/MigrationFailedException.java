package liquibase.migrator.exception;

import liquibase.migrator.ChangeSet;

public class MigrationFailedException extends LiquibaseException {

    private static final long serialVersionUID = 1L;
    private ChangeSet failedChangeSet;
    
    public MigrationFailedException() {
    }

    public MigrationFailedException(ChangeSet failedChangeSet, String message) {
        super(message);
        this.failedChangeSet = failedChangeSet;
    }


    public MigrationFailedException(ChangeSet failedChangeSet, String message, Throwable cause) {
        super(message, cause);
        this.failedChangeSet = failedChangeSet;
    }

    public MigrationFailedException(ChangeSet failedChangeSet, Throwable cause) {
        super(cause);
        this.failedChangeSet = failedChangeSet;
    }
}
