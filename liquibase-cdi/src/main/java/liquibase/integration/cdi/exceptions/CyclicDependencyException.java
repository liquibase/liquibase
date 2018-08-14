package liquibase.integration.cdi.exceptions;

/**
 * @author antoermo (https://github.com/dikeert)
 * @since 31/07/2015
 */
public class CyclicDependencyException extends RuntimeException {
    private static final long serialVersionUID = -3311896900258674329L;
    
    public CyclicDependencyException(String message) {
        super(message);
    }
}
