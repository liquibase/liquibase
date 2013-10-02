package liquibase.exception;

import liquibase.changelog.ChangeSet;

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


    @Override
    public String getMessage() {
        String message = "Migration failed";
        if (failedChangeSet != null) {
            message += " for change set "+failedChangeSet.toString(false);
        }
        message += ":\n     Reason: "+super.getMessage();
//        Throwable cause = this.getCause();
//        while (cause != null) {
//            message += ":\n          Caused By: "+cause.getMessage();
//            cause = cause.getCause();
//        }

        return message;
    }
}
