package liquibase.exception;

/**
 * Marks an internal error (runtime exception) that prevents this software from further processing. Should
 * only be thrown in "impossible" cases where the software suspects a bug in itself.
 */
public class UnexpectedLiquibaseException extends RuntimeException {
    private static final long serialVersionUID = 1570124571347160550L;
    
    /**
     * Constructs a new {@link UnexpectedLiquibaseException} with the given message
     * @param message a message describing what should never have happened
     */
    public UnexpectedLiquibaseException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link UnexpectedLiquibaseException} with the given message
     * and adds information about the {@link Throwable} cause of the problem.
     * @param message a message describing what should never have happened
     * @param cause The {@link Throwable} event that should never have happened
     */
    public UnexpectedLiquibaseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@link UnexpectedLiquibaseException} from a
     * {@link Throwable} event.
     * @param cause The {@link Throwable} event that should never have happened
     */
    public UnexpectedLiquibaseException(Throwable cause) {
        super(cause);
    }
}
