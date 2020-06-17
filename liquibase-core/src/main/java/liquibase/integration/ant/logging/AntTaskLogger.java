package liquibase.integration.ant.logging;

import liquibase.logging.LogLevel;
import liquibase.logging.LogType;
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
    public void severe(LogType target, String message) {
        task.log(message, Project.MSG_ERR);
    }

    @Override
    public void severe(LogType target, String message, Throwable e) {
        task.log(message, e, Project.MSG_ERR);
    }

    @Override
    public void warning(LogType target, String message) {
        task.log(message, Project.MSG_WARN);
    }

    @Override
    public void warning(LogType target, String message, Throwable e) {
        task.log(message, e, Project.MSG_WARN);
    }

    @Override
    public void info(LogType logType, String message) {
        task.log(message, Project.MSG_INFO);
    }

    @Override
    public void info(LogType target, String message, Throwable e) {
        task.log(message, e, Project.MSG_INFO);
    }

    @Override
    public void debug(LogType target, String message) {
        task.log(message, Project.MSG_DEBUG);
    }

    @Override
    public void debug(LogType target, String message, Throwable e) {
        task.log(message, e, Project.MSG_DEBUG);
    }
}
