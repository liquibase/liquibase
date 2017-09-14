package liquibase.exception;

/**
 * Created by sbt-gette-is on 14.09.2017.
 */
public class SqlPlusException extends Exception {
    public SqlPlusException(String message) {
        super(message);
    }

    public SqlPlusException(Throwable cause){
        super(cause);
    }
}
