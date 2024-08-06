package liquibase.precondition;

import liquibase.changelog.DatabaseChangeLog;
import lombok.Getter;

public class ErrorPrecondition {
    @Getter
    private final Throwable cause;
    @Getter
    private final Precondition precondition;
    private final DatabaseChangeLog changeLog;


    public ErrorPrecondition(Throwable exception, DatabaseChangeLog changeLog, Precondition precondition) {
        this.cause = exception;
        this.changeLog = changeLog;
        this.precondition = precondition;
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
