package liquibase.logging;

import liquibase.logging.core.JavaLogService;
import liquibase.plugin.Plugin;

import java.util.logging.Formatter;

/**
 * This service is used to create named {@link Logger} instances through a {@link LogService}.
 * <p>
 * The default LoggerFactory used in {@link JavaLogService} uses {@link java.util.logging.Logger}
 */
public interface LogService extends Plugin {

    int getPriority();

    /**
     * Returns the {@link Formatter} to be applied specifically to the console (stdout/stderr) handler.
     * <p>
     * Returning {@code null} here means the console handler receives no override and falls back to whatever
     * the JUL infrastructure provides (typically {@link java.util.logging.SimpleFormatter}).  Subclasses
     * that wish to apply per-sink formatting — for example to inject ANSI colour codes on the console
     * without affecting file handlers — should override this method.
     * <p>
     * The default implementation returns {@code null}, preserving existing behaviour for all
     * {@link LogService} implementations that do not override this method.
     *
     * @return a {@link Formatter} for the console handler, or {@code null} to use the JUL default
     * @since 5.2
     */
    default Formatter getConsoleFormatter() {
        return null;
    }

    /**
     * Creates a logger for logging from the given class.
     * Unlike most logging systems, there is no exposed getLog(String) method in order to provide more consistency in how logs are named.
     */
    Logger getLog(Class clazz);

    /**
     * Closes the current log output file(s) or any other resources used by this LoggerFactory and its Loggers.
     */
    void close();

    LogMessageFilter getFilter();

    /**
     * Sets the filter to use for messages sent through this log service.
     */
    void setFilter(LogMessageFilter filter);

}
