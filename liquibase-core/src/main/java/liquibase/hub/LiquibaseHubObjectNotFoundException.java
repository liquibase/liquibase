package liquibase.hub;

import liquibase.exception.LiquibaseException;

public class LiquibaseHubObjectNotFoundException extends LiquibaseException {

    public LiquibaseHubObjectNotFoundException() {
    }

    public LiquibaseHubObjectNotFoundException(String message) {
        super(message);
    }

    public LiquibaseHubObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LiquibaseHubObjectNotFoundException(Throwable cause) {
        super(cause);
    }
}
