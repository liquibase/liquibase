package liquibase.exception;

public class JDBCException extends LiquibaseException {

    private static final long serialVersionUID = 1L;
    
    public JDBCException() {
    }

    public JDBCException(String message) {
        super(message);
    }

    public JDBCException(String message, Throwable cause) {
        super(message, cause);
    }

    public JDBCException(Throwable cause) {
        super(cause);
    }
}
