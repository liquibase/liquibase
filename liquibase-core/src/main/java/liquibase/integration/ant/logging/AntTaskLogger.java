package liquibase.integration.ant.logging;

import liquibase.logging.core.AbstractLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * An implementation of the Liquibase logger that logs to the given Ant task.
 */
public final class AntTaskLogger extends AbstractLogger {
    /**
     * The priority of this logger.
     */
    public static final int PRIORITY = Integer.MIN_VALUE;

    private final Task task;

    public AntTaskLogger(Task task) {
        this.task = task;
    }

    @Override
    public void setName(String name) {
        // Do nothing
    }

    @Override
    public void setLogLevel(String logLevel, String logFile) {
        super.setLogLevel(logLevel);
    }

    @Override
    public void severe(String message) {
        task.log(buildMessage(message), Project.MSG_ERR);
    }

    @Override
    public void severe(String message, Throwable e) {
        task.log(buildMessage(message), e, Project.MSG_ERR);
    }

    @Override
    public void warning(String message) {
        task.log(buildMessage(message), Project.MSG_WARN);
    }

    @Override
    public void warning(String message, Throwable e) {
        task.log(buildMessage(message), e, Project.MSG_WARN);
    }

    @Override
    public void info(String message) {
        task.log(buildMessage(message), Project.MSG_INFO);
    }

    @Override
    public void info(String message, Throwable e) {
        task.log(buildMessage(message), e, Project.MSG_INFO);
    }

    @Override
    public void debug(String message) {
        task.log(buildMessage(message), Project.MSG_DEBUG);
    }

    @Override
    public void debug(String message, Throwable e) {
        task.log(buildMessage(message), e, Project.MSG_DEBUG);
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }
}
