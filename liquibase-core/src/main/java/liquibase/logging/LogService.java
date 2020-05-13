package liquibase.logging;

import liquibase.logging.core.JavaLogService;
import liquibase.plugin.Plugin;

import java.util.logging.Level;

/**
 * This service is used to create named {@link Logger} instances through a {@link LogService}.
 *
 * The default LoggerFactory used in {@link JavaLogService} uses {@link java.util.logging.Logger}
 */
public interface LogService extends Plugin {

    int getPriority();

    /**
     * Creates a logger for logging from the given class.
     * Unlike most logging systems, there is no exposed getLog(String) method in order to provide more consistency in how logs are named.
     */
    Logger getLog(Class clazz);

    /**
     * Closes the current log output file(s) or any other resources used by this LoggerFactory and its Loggers.
     */
    void close();


}
