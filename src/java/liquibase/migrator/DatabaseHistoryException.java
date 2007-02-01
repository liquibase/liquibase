package liquibase.migrator;

public class DatabaseHistoryException extends Exception {
    public DatabaseHistoryException(String message) {
        super(message);
    }

    public DatabaseHistoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
