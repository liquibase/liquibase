package liquibase.exception;

public class ServiceNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 4832384741282505341L;
    
    public ServiceNotFoundException(String message) {
        super(message);
    }

    public ServiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNotFoundException(Throwable cause) {
        super(cause);
    }
}
