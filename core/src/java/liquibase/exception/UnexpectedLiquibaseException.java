package liquibase.exception;

public class UnexpectedLiquibaseException extends RuntimeException {
    public UnexpectedLiquibaseException(String message) {
        super(message);
    }
}
