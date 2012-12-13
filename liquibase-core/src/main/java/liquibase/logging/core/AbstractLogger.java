package liquibase.logging.core;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogLevel;
import liquibase.logging.Logger;

import java.util.Stack;

public abstract class AbstractLogger  implements Logger {
    private LogLevel logLevel;
    private Stack<String> contextStack = new Stack<String>();


    public LogLevel getLogLevel() {
        return logLevel;
    }

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

    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
    }


    public void pushContext(String context) {
        contextStack.push(context);
    }

    public void popContext() {
        if (! contextStack.empty()) {
            contextStack.pop();
        }
    }

    String getContext() {
        if ( contextStack.empty()) {
            return "";
        }
        return contextStack.peek();
    }
}
