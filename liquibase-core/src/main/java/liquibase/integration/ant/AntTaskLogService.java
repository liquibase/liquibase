package liquibase.integration.ant;

import liquibase.integration.ant.AntTaskLogger;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import org.apache.tools.ant.Task;

import java.util.logging.Level;

/**
 * An implementation of the Liquibase LogService that logs all messages to the given Ant task. This should only be used
 * inside of Ant tasks.
 */
public final class AntTaskLogService implements LogService {

    private AntTaskLogger logger;

    public AntTaskLogService(Task task) {
        logger = new AntTaskLogger(task);
    }

    @Override
    public int getPriority() {
        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public Logger getLog(Class clazz) {
        return logger;
    }

    @Override
    public void close() {

    }
}
