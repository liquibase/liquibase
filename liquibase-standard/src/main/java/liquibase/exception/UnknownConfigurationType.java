package liquibase.exception;

public class UnknownConfigurationType extends UnexpectedLiquibaseException {
    public UnknownConfigurationType(String message) {
        super(message);
    }

    public UnknownConfigurationType(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownConfigurationType(Throwable cause) {
        super(cause);
    }
}
