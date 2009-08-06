package liquibase.logging;

import liquibase.servicelocator.ServiceLocator;
import liquibase.exception.ServiceNotFoundException;

import java.util.Map;
import java.util.HashMap;

public class LogFactory {
    private static Map<String, Logger> loggers = new HashMap<String, Logger>();
    private static String defaultLoggingLevel = "warning";

    public static Logger getLogger(String name) {
        if (!loggers.containsKey(name)) {
            Logger value;
            try {
                value = (Logger) ServiceLocator.getInstance().createInstance(Logger.class);
            } catch (Exception e) {
                throw new ServiceNotFoundException(e);
            }
            value.setName(name);
            value.setLogLevel(defaultLoggingLevel);
            loggers.put(name, value);
        }

        return loggers.get(name);
    }

    public static Logger getLogger() {
        return getLogger("liquibase");
    }

    public static void setLoggingLevel(String defaultLoggingLevel) {
        LogFactory.defaultLoggingLevel = defaultLoggingLevel;
    }
}
