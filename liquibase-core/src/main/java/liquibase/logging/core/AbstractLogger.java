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
        if ("debug".equalsIgnoreCase(logLevel)) {
            setLogLevel(LogLevel.DEBUG);
        } else if ("info".equalsIgnoreCase(logLevel)) {
            setLogLevel(LogLevel.INFO);
        } else if ("warning".equalsIgnoreCase(logLevel)) {
            setLogLevel(LogLevel.WARNING);
        } else if ("severe".equalsIgnoreCase(logLevel)) {
            setLogLevel(LogLevel.SEVERE);
        } else if ("off".equalsIgnoreCase(logLevel)) {
            setLogLevel(LogLevel.OFF);
        } else {
            throw new UnexpectedLiquibaseException("Unknown log level: " + logLevel+".  Valid levels are: debug, info, warning, severe, off");
        }
    }

    @Override
    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
    }
}
