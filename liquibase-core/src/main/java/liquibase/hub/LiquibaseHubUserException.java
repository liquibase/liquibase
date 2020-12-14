package liquibase.hub;

import liquibase.exception.LiquibaseException;

public class LiquibaseHubUserException extends LiquibaseHubException {

    public LiquibaseHubUserException() {
    }

    public LiquibaseHubUserException(String message) {
        super(message);
    }

    public LiquibaseHubUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public LiquibaseHubUserException(Throwable cause) {
        super(cause);
    }
}
