package liquibase.logging.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.logging.LogLevel;
import liquibase.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DefaultLogger extends AbstractLogger {

    private String name = "liquibase";
    private PrintStream err = System.err;
    private String changeLogName = null;
    private String changeSetName = null;

    public DefaultLogger() {
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public LogLevel getLogLevel() {
        LogLevel logLevel = super.getLogLevel();

        if (logLevel == null) {
            return toLogLevel(LiquibaseConfiguration.getInstance().getConfiguration(DefaultLoggerConfiguration.class).getLogLevel());
        } else {
            return logLevel;
        }
    }

    @Override
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

    @Override
    public void severe(String message) {
        if (getLogLevel().compareTo(LogLevel.SEVERE) <=0) {
            print(LogLevel.SEVERE, message);
        }
    }

    protected void print(LogLevel logLevel, String message) {
        if (StringUtils.trimToNull(message) == null) {
            return;
        }

        List<String> description = new ArrayList<String>();
        description.add(name);
        if (changeLogName != null) {
            description.add(changeLogName);
        }
        if (changeSetName != null) {
            description.add(changeSetName.replace(changeLogName+"::", ""));
        }

        err.println(logLevel + " " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date()) + ":" + StringUtils.join(description, ": ") + ": " + message);
    }

    @Override
    public void severe(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.SEVERE) <=0) {
            print(LogLevel.SEVERE, message);
            e.printStackTrace(err);
        }
    }

    @Override
    public void warning(String message) {
        if (getLogLevel().compareTo(LogLevel.WARNING) <=0) {
            print(LogLevel.WARNING, message);
        }
    }

    @Override
    public void warning(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.WARNING) <=0) {
            print(LogLevel.WARNING, message);
            e.printStackTrace(err);
        }
    }

    @Override
    public void info(String message) {
        if (getLogLevel().compareTo(LogLevel.INFO) <=0) {
            print(LogLevel.INFO, message);
        }
    }

    @Override
    public void info(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.INFO) <=0) {
            print(LogLevel.INFO, message);
            e.printStackTrace(err);
        }
    }

    @Override
    public void debug(String message) {
        if (getLogLevel().compareTo(LogLevel.DEBUG) <=0) {
            print(LogLevel.DEBUG, message);
        }
    }

    @Override
    public void debug(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.DEBUG) <=0) {
            print(LogLevel.DEBUG, message);
            e.printStackTrace(err);
        }

    }

    @Override
    public void setChangeLog(DatabaseChangeLog databaseChangeLog) {
      if (databaseChangeLog == null) {
        changeLogName = null;
      } else {
        changeLogName  = databaseChangeLog.getFilePath();
      }
    }

    @Override
    public void setChangeSet(ChangeSet changeSet) {
      changeSetName = (changeSet == null ? null : changeSet.toString(false));
    }
}
