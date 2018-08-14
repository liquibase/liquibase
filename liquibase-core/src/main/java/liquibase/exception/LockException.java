package liquibase.exception;

public class LockException extends LiquibaseException {
    
    private static final long serialVersionUID = 4541125759401539389L;
    
    public LockException(String message) {
        super(message);
    }

    public LockException(Throwable cause) {
        super(cause);
    }

    public LockException(String message, Throwable cause) {
        super(message, cause);
    }
}
