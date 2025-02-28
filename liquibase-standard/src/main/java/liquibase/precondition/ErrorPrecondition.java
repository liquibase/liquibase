package liquibase.precondition;

import liquibase.changelog.DatabaseChangeLog;

public class ErrorPrecondition {
    private final Throwable cause;
    private final Precondition precondition;
    private final DatabaseChangeLog changeLog;


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
        Throwable cause = this.cause;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        String causeMessage = cause.getMessage();
        if (causeMessage == null) {
            causeMessage = this.cause.getMessage();
        }
        if (changeLog == null) {
            return causeMessage;
        } else {
            return changeLog +" : "+ precondition.toString()+" : "+causeMessage;
        }
    }
}
