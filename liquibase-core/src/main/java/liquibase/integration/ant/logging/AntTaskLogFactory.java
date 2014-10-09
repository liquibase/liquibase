package liquibase.integration.ant.logging;

import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import org.apache.tools.ant.Task;

/**
 * An implementation of the Liquibase LogFactory that logs all messages to the given Ant task. This should only be used
 * inside of Ant tasks.
 */
public final class AntTaskLogFactory extends LogFactory {
    private AntTaskLogger logger;

    public AntTaskLogFactory(Task task) {
        logger = new AntTaskLogger(task);
    }
    @Override
    public Logger getLog(String name) {
        return logger;
    }

    @Override
    public Logger getLog() {
        return logger;
    }
}
