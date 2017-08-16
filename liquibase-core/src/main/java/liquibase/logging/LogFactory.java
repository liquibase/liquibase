package liquibase.logging;

import liquibase.Liquibase;

/**
 * @deprecated use {@link LogService} now.
 * This class is kept for compatibility with Liquibase 3.5 and prior.
 */
public class LogFactory {

    private static LogFactory instance;

    public static synchronized void reset() {
        instance = new LogFactory();
    }

    public static synchronized LogFactory getInstance() {
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

    public static Logger getLogger(String name) {
        return getInstance().getLog(name);
    }

    public Logger getLog(String name) {
        Class clazz;
        try {
            clazz = Class.forName(name);
        } catch (ClassNotFoundException e) {
            clazz = Liquibase.class;
        }
        return LogService.getLog(clazz);
    }

    public static Logger getLogger() {
        return getInstance().getLog();
    }

    public Logger getLog() {
        return LogService.getLog(Liquibase.class);
    }

    public void setDefaultLoggingLevel(String defaultLoggingLevel) {
        LogService.getLog(getClass()).warning(LogType.LOG, "LogFactory.setDefaultLoggingLevel() is now a no-op.");
    }

    public void setDefaultLoggingLevel(LogLevel defaultLoggingLevel) {
        LogService.getLog(getClass()).warning(LogType.LOG, "LogFactory.setDefaultLoggingLevel() is now a no-op.");
    }

    public static void setLoggingLevel(String defaultLoggingLevel) {
        LogService.getLog(LogFactory.class).warning(LogType.LOG, "LogFactory.setLoggingLevel() is now a no-op.");
    }
}
