package liquibase.integration.ant.logging;

import liquibase.logging.Logger;
import liquibase.logging.LoggerContext;
import liquibase.logging.LoggerFactory;
import liquibase.logging.core.NoOpLoggerContext;
import org.apache.tools.ant.Task;

/**
 * An implementation of the Liquibase LogService that logs all messages to the given Ant task. This should only be used
 * inside of Ant tasks.
 */
public final class AntTaskLogFactory implements LoggerFactory {

    private AntTaskLogger logger;

    public AntTaskLogFactory(Task task) {
        logger = new AntTaskLogger(task);
    }

    @Override
    public Logger getLog(Class clazz) {
        return logger;
    }

    @Override
    public LoggerContext pushContext(String key, Object object) {
        return new NoOpLoggerContext();
    }

    @Override
    public void close() {

    }
}
