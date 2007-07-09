package liquibase.migrator.preconditions;

import liquibase.migrator.DatabaseChangeLog;

public class FailedPrecondition {
    private String message;
    private Precondition precondition;
    private DatabaseChangeLog changeLog;


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


    public String toString() {
        return changeLog.toString()+" : "+message;
    }
}
