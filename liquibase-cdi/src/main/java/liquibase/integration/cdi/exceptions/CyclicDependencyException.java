package liquibase.integration.cdi.exceptions;

/**
 * @author <a href="https://github.com/dikeert">antoermo</a>
 * @since 31/07/2015
 */
public class CyclicDependencyException extends RuntimeException {
    private static final long serialVersionUID = -3311896900258674329L;
    
    public CyclicDependencyException(String message) {
        super(message);
    }
}
