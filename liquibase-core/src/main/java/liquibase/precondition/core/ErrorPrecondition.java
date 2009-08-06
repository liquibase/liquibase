package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.precondition.Precondition;

public class ErrorPrecondition {
    private Throwable cause;
    private Precondition precondition;
    private DatabaseChangeLog changeLog;


    public ErrorPrecondition(Throwable exception, DatabaseChangeLog changeLog, Precondition precondition) {
        this.cause = exception;
        this.changeLog = changeLog;
        this.precondition = precondition;
    }


    public Throwable getCause() {
        return cause;
    }

    public Precondition getPrecondition() {
        return precondition;
    }


    @Override
    public String toString() {
        if (changeLog == null) {
            return cause.getMessage();
        } else {
            return changeLog.toString()+" : "+getCause().getMessage();
        }
    }
}