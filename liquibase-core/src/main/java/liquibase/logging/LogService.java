package liquibase.logging;

import liquibase.logging.core.Slf4JLoggerFactory;

/**
 * Primary facade for working with Logs in Liquibase.
 *
 * This service is used to create named {@link Logger} instances through a {@link LoggerFactory}.
 *
 * This service supports "nested diagnostic contexts" and progress tracking through {@link #pushContext(String, Object)}.
 * It is up to the underlying {@link LoggerFactory} implementations to support those features as they can.
 *
 * The default LoggerFactory used in {@link Slf4JLoggerFactory} which allows the log to be bound to most any underlying logging system
 * via SLF4j.
 */
public class LogService {


    private static LoggerFactory loggerFactory = new Slf4JLoggerFactory();

    /**
     * Singleton so private constructor.
     */
    private LogService() {
    }

    /**
     * Set the LoggerFactory used by this singleton.
     */
    public static void setLoggerFactory(LoggerFactory service) {
        LogService.loggerFactory = service;
    }

    /**
     * Returns a Logger for the given class based on the configured {@link #setLoggerFactory(LoggerFactory)}.
     * There is no string version of this class to force a class-based log pattern.
     */
    public static Logger getLog(Class clazz) {
        return loggerFactory.getLog(clazz);
    }

    /**
     * Pushes a new nested diagnostic context onto the stack.
     * The {@link LoggerContext} most be {@link LoggerContext#close()}'ed correctly.
     * Ususally a "try with resource" pattern is best.
     */
    public static LoggerContext pushContext(String key, Object object) {
        return loggerFactory.pushContext(key, object);
    }

    /**
     * Close the current log factory.
     */
    public void close() {
        loggerFactory.close();
    }

}
