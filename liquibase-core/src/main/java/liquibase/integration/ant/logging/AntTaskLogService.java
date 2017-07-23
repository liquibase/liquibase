package liquibase.integration.ant.logging;

import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.logging.LoggerContext;
import liquibase.logging.LoggerService;
import liquibase.logging.core.NoOpLoggerContext;
import org.apache.tools.ant.Task;

/**
 * An implementation of the Liquibase LogFactory that logs all messages to the given Ant task. This should only be used
 * inside of Ant tasks.
 */
public final class AntTaskLogService implements LoggerService {

    private AntTaskLogger logger;

    public AntTaskLogService(Task task) {
        logger = new AntTaskLogger(task);
    }


    @Override
    public Logger getLog(String name) {
        return logger;
    }

    @Override
    public Logger getLog(Class clazz) {
        return logger;
    }

    @Override
    public LoggerContext pushContext(Object object) {
        return new NoOpLoggerContext();
    }
}
