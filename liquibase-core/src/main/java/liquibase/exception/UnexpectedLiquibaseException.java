package liquibase.exception;

public class UnexpectedLiquibaseException extends RuntimeException {
    public UnexpectedLiquibaseException(String message) {
        super(message);
    }

    public UnexpectedLiquibaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedLiquibaseException(Throwable cause) {
        super(cause);
    }
}
