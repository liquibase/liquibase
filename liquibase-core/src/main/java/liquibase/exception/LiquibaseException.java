package liquibase.exception;

/**
 * Base class for all Liquibase exceptions.
 */
public class LiquibaseException extends Exception {

    private static final long serialVersionUID = 1L;
    private String timestamp;
    private String details;

    public LiquibaseException() {
    }

    public LiquibaseException(String message) {
        super(message);
    }

    public LiquibaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public LiquibaseException(Throwable cause) {
        super(cause);
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
