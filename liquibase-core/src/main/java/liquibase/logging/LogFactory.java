package liquibase.logging;

import liquibase.exception.ServiceNotFoundException;
import liquibase.servicelocator.ServiceLocator;

import java.util.HashMap;
import java.util.Map;

public class LogFactory {

    private static LogFactory instance;

    private Map<String, Logger> loggers = new HashMap<String, Logger>();
    private String defaultLoggingLevel = "info";


    public static void reset() {
        instance = new LogFactory();
    }

    public static LogFactory getInstance() {
        if (instance == null) {
            instance = new LogFactory();
        }
        return instance;
    }

    /**
     * Set the instance used by this singleton. Used primarily for testing.
     */
    public static void setInstance(LogFactory instance) {
        LogFactory.instance = instance;
    }

    /**
     * @deprecated Use non-static {@link #getLog(String)} method
     */
    @Deprecated
    public static Logger getLogger(String name) {
        return getInstance().getLog(name);
    }

    public Logger getLog(String name) {
        if (!loggers.containsKey(name)) {
            Logger value;
            try {
                value = (Logger) ServiceLocator.getInstance().newInstance(Logger.class);
            } catch (Exception e) {
                throw new ServiceNotFoundException(e);
            }
            value.setName(name);
            value.setLogLevel(defaultLoggingLevel);
            loggers.put(name, value);
        }

        return loggers.get(name);
    }

    /**
     * @deprecated Use non-static {@link #getLog()} method
     */
    @Deprecated
    public static Logger getLogger() {
        return getInstance().getLog();
    }

    public Logger getLog() {
        return getLog("liquibase");
    }

    public void setDefaultLoggingLevel(String defaultLoggingLevel) {
        this.defaultLoggingLevel = defaultLoggingLevel;
    }

    /**
     * @deprecated Use non-static {@link #setDefaultLoggingLevel(String)} method
     */
    @Deprecated
    public static void setLoggingLevel(String defaultLoggingLevel) {
        getInstance().defaultLoggingLevel = defaultLoggingLevel;
    }
}
