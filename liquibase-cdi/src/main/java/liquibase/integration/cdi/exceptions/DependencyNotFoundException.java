package liquibase.integration.cdi.exceptions;

/**
 * @author antoermo (https://github.com/dikeert)
 * @since 31/07/2015
 */
public class DependencyNotFoundException extends RuntimeException {

	public DependencyNotFoundException(String message) {
		super(message);
	}
}
