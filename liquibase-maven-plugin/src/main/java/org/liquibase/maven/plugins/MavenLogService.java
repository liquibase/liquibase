package org.liquibase.maven.plugins;

import liquibase.logging.Logger;
import liquibase.logging.core.AbstractLogService;
import liquibase.logging.core.AbstractLogger;
import org.apache.maven.plugin.logging.Log;

import java.util.logging.Level;

public class MavenLogService extends AbstractLogService {

    private final Log rootLog;
    private MavenLogger mavenLogger;

    public MavenLogService(Log log) {
        this.rootLog = log;
    }

    @Override
    public int getPriority() {
        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public Logger getLog(Class clazz) {
        if (this.mavenLogger == null) {
            this.mavenLogger = new MavenLogger(rootLog);
        }
        return this.mavenLogger;
    }

    private static class MavenLogger extends AbstractLogger {

        private final Log mavenLog;

        public MavenLogger(Log log) {
            this.mavenLog = log;
        }

        @Override
        public void log(Level level, String message, Throwable e) {
            if (level == Level.OFF) {
                return;
            }

            if (level.equals(Level.SEVERE)) {
                mavenLog.error(message, e);
            } else if (level.equals(Level.WARNING)) {
                mavenLog.warn(message, e);
            } else if (level.equals(Level.INFO)) {
                mavenLog.info(message, e);
            } else if (level.equals(Level.FINE) || level.equals(Level.FINER) || level.equals(Level.FINEST)) {
                mavenLog.debug(message, e);
            } else if (level.equals(Level.CONFIG)) {
                mavenLog.debug(message, e);
            }
        }
    }
}
