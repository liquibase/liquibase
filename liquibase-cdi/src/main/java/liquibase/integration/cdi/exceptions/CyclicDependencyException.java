package liquibase.integration.cdi.exceptions;

/**
 * @author antoermo (https://github.com/dikeert)
 * @since 31/07/2015
 */
public class CyclicDependencyException extends RuntimeException {
    public CyclicDependencyException(String message) {
        super(message);
    }
}
