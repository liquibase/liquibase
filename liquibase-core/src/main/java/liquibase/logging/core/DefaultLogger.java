package liquibase.logging.core;

import liquibase.logging.LogLevel;
import liquibase.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.Stack;

public class DefaultLogger extends AbstractLogger {

    private String name = "liquibase";
    private PrintStream err = System.err;

    public DefaultLogger() {
        String passedLevel = System.getProperty("liquibase.defaultlogger.level");
        if (passedLevel == null) {
            setLogLevel(LogLevel.INFO);
        } else {
            setLogLevel(passedLevel);
        }
    }

    public int getPriority() {
        return 1;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLogLevel(String logLevel, String logFile) {
        setLogLevel(logLevel);
        if (logFile != null) {
            File log = new File(logFile);
            try {
                if (!log.exists()) {
                    if (!log.createNewFile()) {
                        throw new RuntimeException("Could not create logFile "+log.getAbsolutePath());
                    }
                }
                err = new PrintStream(log);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void severe(String message) {
        if (getLogLevel().compareTo(LogLevel.SEVERE) <=0) {
            print(LogLevel.SEVERE, message);
        }
    }

    protected void print(LogLevel logLevel, String message) {
        if (StringUtils.trimToNull(message) == null) {
            return;
        }
        
        err.println(logLevel+" "+DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date())+ ":"+name + ":" + getContext() + ": " + message);
    }

    public void severe(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.SEVERE) <=0) {
            print(LogLevel.SEVERE, message);
            e.printStackTrace(err);
        }
    }

    public void warning(String message) {
        if (getLogLevel().compareTo(LogLevel.WARNING) <=0) {
            print(LogLevel.WARNING, message);
        }
    }

    public void warning(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.WARNING) <=0) {
            print(LogLevel.WARNING, message);
            e.printStackTrace(err);
        }
    }

    public void info(String message) {
        if (getLogLevel().compareTo(LogLevel.INFO) <=0) {
            print(LogLevel.INFO, message);
        }
    }

    public void info(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.INFO) <=0) {
            print(LogLevel.INFO, message);
            e.printStackTrace(err);
        }
    }

    public void debug(String message) {
        if (getLogLevel().compareTo(LogLevel.DEBUG) <=0) {
            print(LogLevel.DEBUG, message);
        }
    }

    public void debug(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.DEBUG) <=0) {
            print(LogLevel.DEBUG, message);
            e.printStackTrace(err);
        }

    }

}
