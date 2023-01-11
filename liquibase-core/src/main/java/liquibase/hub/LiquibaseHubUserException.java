package liquibase.hub;

public class LiquibaseHubUserException extends LiquibaseHubException {


    private static final long serialVersionUID = 4712210338853061380L;

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
