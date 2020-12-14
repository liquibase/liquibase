package liquibase.integration.ant;

import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.logging.core.AbstractLogService;
import org.apache.tools.ant.Task;

/**
 * An implementation of the Liquibase LogService that logs all messages to the given Ant task. This should only be used
 * inside of Ant tasks.
 */
public final class AntTaskLogService extends AbstractLogService {

    private final AntTaskLogger logger;

    public AntTaskLogService(Task task) {
        logger = new AntTaskLogger(task, this.filter);
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
