package liquibase.logging.core;

import liquibase.logging.Logger;

public class CommandLineLoggerService extends Slf4jLoggerService {

    @Override
    protected Logger createLoggerImpl(org.slf4j.Logger logger) {
        return new CommandLineLogger(logger);
    }

}
