package liquibase.exception;

public class CustomChangeException extends Exception {

    public CustomChangeException() {
    }

    public CustomChangeException(String message) {
        super(message);
    }

    public CustomChangeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomChangeException(Throwable cause) {
        super(cause);
    }
}
