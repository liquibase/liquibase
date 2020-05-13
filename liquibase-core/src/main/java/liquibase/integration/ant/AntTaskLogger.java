package liquibase.integration.ant;

import liquibase.logging.core.AbstractLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.util.logging.Level;

/**
 * An implementation of the Liquibase logger that logs to the given Ant task.
 */
public final class AntTaskLogger extends AbstractLogger {

    private final Task task;

    public AntTaskLogger(Task task) {
        this.task = task;
    }

    @Override
    public void log(Level level, String message, Throwable e) {
        int antLevel;
        if (level.intValue() == Level.SEVERE.intValue()) {
            antLevel = Project.MSG_ERR;
        } else if (level.intValue() == Level.WARNING.intValue()) {
            antLevel = Project.MSG_WARN;
        } else if (level.intValue() == Level.INFO.intValue()) {
            antLevel = Project.MSG_INFO;
        } else if (level.intValue() == Level.CONFIG.intValue()) {
            //no config level, using debug
            antLevel = Project.MSG_DEBUG;
        } else if (level.intValue() == Level.FINE.intValue()) {
            antLevel = Project.MSG_DEBUG;
        } else {
            //lower than FINE
            antLevel = Project.MSG_DEBUG;
        }
        task.log(message, e, antLevel);
    }
}
