package liquibase.exception;

import lombok.Getter;

/**
 * Exception indicating that the required argument is missing.
 */
@Getter
public class MissingRequiredArgumentException extends Exception {


    private static final long serialVersionUID = -4166453748767367091L;
    private final String argumentName;

    public MissingRequiredArgumentException(String argumentName) {
        this.argumentName = argumentName;
    }

}
