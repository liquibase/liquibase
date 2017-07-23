package liquibase.integration.ant.logging;

import liquibase.logging.LogTarget;
import liquibase.logging.core.AbstractLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * An implementation of the Liquibase logger that logs to the given Ant task.
 */
public final class AntTaskLogger extends AbstractLogger {

    private final Task task;

    public AntTaskLogger(Task task) {
        this.task = task;
    }

    @Override
    public void error(LogTarget target, String message) {
        task.log(message, Project.MSG_ERR);
    }

    @Override
    public void error(LogTarget target, String message, Throwable e) {
        task.log(message, e, Project.MSG_ERR);
    }

    @Override
    public void warn(LogTarget target, String message) {
        task.log(message, Project.MSG_WARN);
    }

    @Override
    public void warn(LogTarget target, String message, Throwable e) {
        task.log(message, e, Project.MSG_WARN);
    }

    @Override
    public void info(LogTarget logTarget, String message) {
        task.log(message, Project.MSG_INFO);
    }

    @Override
    public void info(LogTarget target, String message, Throwable e) {
        task.log(message, e, Project.MSG_INFO);
    }

    @Override
    public void sql(LogTarget target, String message) {
        task.log(message, Project.MSG_VERBOSE);
    }

    @Override
    public void debug(LogTarget target, String message) {
        task.log(message, Project.MSG_DEBUG);
    }

    @Override
    public void debug(LogTarget target, String message, Throwable e) {
        task.log(message, e, Project.MSG_DEBUG);
    }
}
