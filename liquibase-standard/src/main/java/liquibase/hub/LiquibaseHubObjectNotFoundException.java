package liquibase.hub;

public class LiquibaseHubObjectNotFoundException extends LiquibaseHubException {


    private static final long serialVersionUID = -2323899007632706607L;

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
