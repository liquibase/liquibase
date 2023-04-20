package liquibase.parser.core;

public class ParsedNodeException extends Exception {
    private static final long serialVersionUID = -7841445953043527911L;
    
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
