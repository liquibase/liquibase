package liquibase.precondition;

import liquibase.changelog.DatabaseChangeLog;

public class FailedPrecondition {
    private final String message;
    private final Precondition precondition;
    private final DatabaseChangeLog changeLog;


    public FailedPrecondition(String message, DatabaseChangeLog changeLog, Precondition precondition) {
        this.message = message;
        this.changeLog = changeLog;
        this.precondition = precondition;
    }


    public String getMessage() {
        return message;
    }

    public Precondition getPrecondition() {
        return precondition;
    }


    @Override
    public String toString() {
        if (changeLog == null) {
            return message;
        } else {
            return changeLog +" : "+message;
        }
    }
}
