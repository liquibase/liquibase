package liquibase.sdk.exception;

public class UnexpectedLiquibaseSdkException extends RuntimeException {
    
    private static final long serialVersionUID = 4900618014872610542L;
    
    public UnexpectedLiquibaseSdkException() {
    }

    public UnexpectedLiquibaseSdkException(String message) {
        super(message);
    }

    public UnexpectedLiquibaseSdkException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedLiquibaseSdkException(Throwable cause) {
        super(cause);
    }
}
