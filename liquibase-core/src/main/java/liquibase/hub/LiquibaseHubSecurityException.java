package liquibase.hub;

public class LiquibaseHubSecurityException extends LiquibaseHubException {

    public LiquibaseHubSecurityException() {
    }

    public LiquibaseHubSecurityException(String message) {
        super(message);
    }

    public LiquibaseHubSecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    public LiquibaseHubSecurityException(Throwable cause) {
        super(cause);
    }
}
