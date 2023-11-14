package liquibase.exception;

/**
 * This interface marks an exception as one which can specify the exit code that Liquibase should use if an exception
 * which implements this interface is thrown.
 */
public interface ExitCodeException {

    default Integer getExitCode() {
        return null;
    }
}
