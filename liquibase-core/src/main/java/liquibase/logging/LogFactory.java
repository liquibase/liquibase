package liquibase.logging;

import liquibase.logging.core.Slf4jLoggerService;

public class LogFactory {


    private static LoggerService loggerService = new Slf4jLoggerService();

    /**
     * Set the instance used by this singleton.
     */
    public static void setService(LoggerService service) {
        LogFactory.loggerService = service;
    }

    public static Logger getLog(String name) {
        return loggerService.getLog(name);
    }

    public static Logger getLog(Class clazz) {
        return loggerService.getLog(clazz);
    }

    public static LoggerContext pushContext(Object object) {
        return loggerService.pushContext(object);
    }

}
