package liquibase.logging.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogLevel;
import liquibase.logging.Logger;

/**
 * Basis for an implementation of a Logger in this software.
 */
public abstract class AbstractLogger implements Logger {
    private LogLevel logLevel;
    private DatabaseChangeLog databaseChangeLog;
    private ChangeSet changeSet;

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
    }

    @Override
    public void setLogLevel(String logLevel) {
        setLogLevel(toLogLevel(logLevel));
    }

    @Override
    public void closeLogFile() {

    }

    /**
     * Transforms the strings DEBUG, INFO, WARNING, SEVERE and OFF (case-insensitive) into the appropriate LogLevel.
     * @param logLevel one of the above strings
     * @return a value from the LogLevel enum
     */
    protected LogLevel toLogLevel(String logLevel) {
        if ("debug".equalsIgnoreCase(logLevel)) {
            return LogLevel.DEBUG;
        } else if ("sql".equalsIgnoreCase(logLevel)) {
            return LogLevel.SQL;
        } else if ("info".equalsIgnoreCase(logLevel)) {
            return LogLevel.INFO;
        } else if ("warning".equalsIgnoreCase(logLevel)) {
            return LogLevel.WARNING;
        } else if ("severe".equalsIgnoreCase(logLevel)) {
            return LogLevel.SEVERE;
        } else if ("off".equalsIgnoreCase(logLevel)) {
            return LogLevel.OFF;
        } else {
            throw new UnexpectedLiquibaseException("Unknown log level: " + logLevel+".  Valid levels are: debug, info, warning, severe, off");
        }
    }

    /**
     * Constructs a prefix of the form
     * "changelogWithPath: changeSetName: "
     * and adds the given message to that. For this to work, the application must diligently use
     * Logger.setChangeLog()/Logger.setChangeSet(), or else wrong information might be returned.
     * @param message the log message
     * @return the constructed string
     * @see Logger
     */
    protected String buildMessage(String message) {
        StringBuilder msg = new StringBuilder();
        if(databaseChangeLog != null) {
            msg.append(databaseChangeLog.getFilePath()).append(": ");
        }
        if(changeSet != null) {
            String changeSetName = changeSet.toString(false);
            msg.append(changeSetName.replace(changeSetName + "::", "")).append(": ");
        }
        msg.append(message);
        return msg.toString();
    }

    @Override
    public void setChangeLog(DatabaseChangeLog databaseChangeLog) {
        this.databaseChangeLog = databaseChangeLog;
    }

    @Override
    public void setChangeSet(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }
}
