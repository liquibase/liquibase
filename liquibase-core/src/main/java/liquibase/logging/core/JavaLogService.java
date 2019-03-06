package liquibase.logging.core;

import liquibase.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class JavaLogService extends AbstractLogService {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    private Map<Class, JavaLogger> loggers = new HashMap<>();


    @Override
    public void setLogLevel(Level level) {
        if (this.loggers != null) {
            this.loggers.clear();
        }

        super.setLogLevel(level);

        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(level);
        }
    }

    @Override
    public Logger getLog(Class clazz) {
        JavaLogger logger = loggers.get(clazz);
        if (logger == null) {
            java.util.logging.Logger utilLogger = java.util.logging.Logger.getLogger(clazz.getName());
            utilLogger.setLevel(getLogLevel());

            logger = new JavaLogger(utilLogger);

            this.loggers.put(clazz, logger);
        }

        return logger;
    }

}
