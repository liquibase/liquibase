package liquibase.integration.cdi.exceptions;

/**
  * @author <a href="https://github.com/dikeert">antoermo</a>
 * @since 31/07/2015
 */
public class DependencyNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 8350372637527962559L;
    
    public DependencyNotFoundException(String message) {
		super(message);
	}
}
