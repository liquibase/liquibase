package liquibase.migrator;

public class UnsupportedChangeException extends Exception {
    public UnsupportedChangeException(String message) {
        super(message);
    }

    public UnsupportedChangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedChangeException(Throwable cause) {
        super(cause);
    }
}
