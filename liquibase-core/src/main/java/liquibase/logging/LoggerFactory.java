package liquibase.logging;


/**
 * Primary front-end for various Logging implementations by constructing the correct {@link Logger} version.
 *
 * This interface is also the front-end for managing Nested Diagnostic Contexts.
 */
public interface LoggerFactory {

    /**
     * Creates a logger for logging from the given class.
     * Unlike most logging systems, there is no exposed getLog(String) method in order to provide more consistency in how logs are named.
     */
    Logger getLog(Class clazz);


    /**
     * Creates a new {@link LoggerContext} and pushes it onto the stack.
     * LoggerContexts are removed from the stack via the {@link LoggerContext#close()} method.
     */
    LoggerContext pushContext(String key, Object object);

    /**
     * Closes the current log output file(s) or any other resources used by this LoggerFactory and its Loggers.
     */
    void close();

}
