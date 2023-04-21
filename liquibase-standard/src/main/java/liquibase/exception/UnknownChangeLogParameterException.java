package liquibase.exception;

public class UnknownChangeLogParameterException extends UnexpectedLiquibaseException {

    public UnknownChangeLogParameterException(String message) {
        super(message);
    }

    public UnknownChangeLogParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownChangeLogParameterException(Throwable cause) {
        super(cause);
    }
}
