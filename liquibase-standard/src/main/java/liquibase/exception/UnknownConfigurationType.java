package liquibase.exception;

public class UnknownConfigurationType extends UnexpectedLiquibaseException {

    private static final long serialVersionUID = 6670759711884291311L;

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
