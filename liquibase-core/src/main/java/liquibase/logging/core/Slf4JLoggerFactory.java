package liquibase.logging.core;

import liquibase.logging.Logger;
import liquibase.logging.LoggerContext;

public class Slf4JLoggerFactory extends AbstractLoggerFactory{
    @Override
    public Logger getLog(Class clazz) {
        return createLoggerImpl(org.slf4j.LoggerFactory.getLogger(clazz));
    }

    @Override
    public LoggerContext pushContext(String key, Object object) {
        return new Slf4jLoggerContext(key, object);
    }

    protected Logger createLoggerImpl(org.slf4j.Logger logger) {
        return new Slf4jLogger(logger);
    }

    @Override
    public void close() {

    }
}
