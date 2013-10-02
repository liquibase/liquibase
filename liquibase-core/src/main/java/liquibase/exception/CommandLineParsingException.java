package liquibase.exception;

public class CommandLineParsingException extends Exception {

    private static final long serialVersionUID = 1L;

    public CommandLineParsingException() {
    }

    public CommandLineParsingException(String message) {
        super(message);
    }

    public CommandLineParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandLineParsingException(Throwable cause) {
        super(cause);
    }
}
