package liquibase.hub;

import liquibase.exception.LiquibaseException;

public class LiquibaseHubException extends LiquibaseException {


    private static final long serialVersionUID = 5346735633422612208L;

    public LiquibaseHubException() {
    }

    public LiquibaseHubException(String message) {
        super(message);
    }

    public LiquibaseHubException(String message, Throwable cause) {
        super(message, cause);
    }

    public LiquibaseHubException(Throwable cause) {
        super(cause);
    }
}
