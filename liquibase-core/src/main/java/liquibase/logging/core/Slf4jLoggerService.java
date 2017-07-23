package liquibase.logging.core;

import liquibase.logging.Logger;
import liquibase.logging.LoggerContext;
import liquibase.logging.LoggerService;

public class Slf4jLoggerService implements LoggerService {
    @Override
    public Logger getLog(String name) {
        return createLoggerImpl(org.slf4j.LoggerFactory.getLogger(name));
    }

    @Override
    public Logger getLog(Class clazz) {
        return createLoggerImpl(org.slf4j.LoggerFactory.getLogger(clazz));
    }

    @Override
    public LoggerContext pushContext(Object object) {
        return new Slf4jLoggerContext();
    }

    protected Logger createLoggerImpl(org.slf4j.Logger logger) {
        return new Slf4jLogger(logger);
    }
}
