package liquibase.logging.core;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogLevel;
import liquibase.logging.Logger;

public abstract class AbstractLogger  implements Logger {
    private LogLevel logLevel;

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public void setLogLevel(String logLevel) {
        setLogLevel(toLogLevel(logLevel));
    }

    protected LogLevel toLogLevel(String logLevel) {
        if ("debug".equalsIgnoreCase(logLevel)) {
            return LogLevel.DEBUG;
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

    @Override
    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
    }
}
