package liquibase.parser.core;

public class ParsedNodeException extends Exception {
    public ParsedNodeException(String message) {
        super(message);
    }

    public ParsedNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsedNodeException(Throwable cause) {
        super(cause);
    }
}
