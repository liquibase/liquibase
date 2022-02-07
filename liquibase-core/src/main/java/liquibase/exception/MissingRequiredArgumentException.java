package liquibase.exception;

/**
 * Exception indicating that the required argument is missing.
 */
public class MissingRequiredArgumentException extends Exception {

    private final String argumentName;

    public MissingRequiredArgumentException(String argumentName) {
        this.argumentName = argumentName;
    }

    public String getArgumentName() {
        return argumentName;
    }
}
