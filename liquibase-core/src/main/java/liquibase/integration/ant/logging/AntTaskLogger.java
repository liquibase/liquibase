package liquibase.integration.ant.logging;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
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
    private String changeLogName = null;
    private String changeSetName = null;

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
    public void setChangeLog(DatabaseChangeLog databaseChangeLog) {
        changeLogName = (databaseChangeLog == null) ? null : databaseChangeLog.getFilePath();
    }

    @Override
    public void setChangeSet(ChangeSet changeSet) {
        changeSetName = (changeSet == null ? null : changeSet.toString(false));
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    private String buildMessage(String message) {
        StringBuilder msg = new StringBuilder();
        if(changeLogName != null) {
            msg.append(changeLogName).append(": ");
        }
        if(changeSetName != null) {
            msg.append(changeSetName.replace(changeLogName + "::", "")).append(": ");
        }
        msg.append(message);
        return msg.toString();
    }
}
