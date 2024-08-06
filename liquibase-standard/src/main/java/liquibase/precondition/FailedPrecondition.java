package liquibase.precondition;

import liquibase.changelog.DatabaseChangeLog;
import lombok.Getter;

public class FailedPrecondition {
    @Getter
    private final String message;
    @Getter
    private final Precondition precondition;
    private final DatabaseChangeLog changeLog;


    public FailedPrecondition(String message, DatabaseChangeLog changeLog, Precondition precondition) {
        this.message = message;
        this.changeLog = changeLog;
        this.precondition = precondition;
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
