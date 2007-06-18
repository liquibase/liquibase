package liquibase.migrator.exception;

public class JDBCException extends Exception {
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
