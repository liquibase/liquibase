package liquibase.exception;

public class CustomChangeException extends Exception {
    
    private static final long serialVersionUID = 3360799051348078105L;
    
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
