package liquibase.exception;

/**
 * @author gette
 */
public class SqlPlusException extends Exception {
    public SqlPlusException(String message) {
        super(message);
    }

    public SqlPlusException(Throwable cause){
        super(cause);
    }
}
