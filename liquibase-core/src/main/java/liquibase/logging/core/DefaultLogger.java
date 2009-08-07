package liquibase.logging.core;

import liquibase.logging.LogLevel;

import java.text.DateFormat;
import java.util.Date;

public class DefaultLogger extends AbstractLogger {

    private String name = "liquibase";

    public DefaultLogger() {
        String passedLevel = System.getProperty("liquibase.default.logger.level");
        if (passedLevel == null) {
            setLogLevel(LogLevel.SEVERE);
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
    }

    public void severe(String message) {
        if (getLogLevel().compareTo(LogLevel.SEVERE) <=0) {
            print(message);
        }
    }

    private void print(String message) {
        System.out.println(name + ":" + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date()) + ":" + message);
    }

    public void severe(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.SEVERE) <=0) {
            print(message);
            e.printStackTrace();
        }
    }

    public void warning(String message) {
        if (getLogLevel().compareTo(LogLevel.WARNING) <=0) {
            print(message);
        }
    }

    public void warning(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.WARNING) <=0) {
            print(message);
            e.printStackTrace();
        }
    }

    public void info(String message) {
        if (getLogLevel().compareTo(LogLevel.INFO) <=0) {
            print(message);
        }
    }

    public void info(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.INFO) <=0) {
            print(message);
            e.printStackTrace();
        }
    }

    public void debug(String message) {
        if (getLogLevel().compareTo(LogLevel.DEBUG) <=0) {
            print(message);
        }
    }

    public void debug(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.DEBUG) <=0) {
            print(message);
            e.printStackTrace();
        }

    }
}
