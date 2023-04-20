package liquibase.hub;

public class LiquibaseHubSecurityException extends LiquibaseHubException {


    private static final long serialVersionUID = -2073029561664028010L;

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
