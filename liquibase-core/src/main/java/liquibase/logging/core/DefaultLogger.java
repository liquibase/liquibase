package liquibase.logging.core;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogLevel;
import liquibase.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;

/**
 * The default logger for this software. The general output format created by this logger is:
 * [log level] date/time: liquibase: DatabaseChangeLog name:ChangeSet name: message.
 * DEBUG/SQL/INFO message are printer to STDOUT and WARNING/SEVERE messages are printed to STDERR.
 */
public class DefaultLogger extends AbstractLogger {

    private String name = "liquibase";
    private PrintStream stderr = System.err;
    private PrintStream stdout = System.out;

    /**
     * default constructor
     */
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
            return toLogLevel(LiquibaseConfiguration.getInstance().getConfiguration(
                    DefaultLoggerConfiguration.class
            ).getLogLevel());
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
                stderr = new PrintStream(log, LiquibaseConfiguration.getInstance()
                        .getConfiguration(GlobalConfiguration.class).getOutputEncoding());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void closeLogFile() {
        if (stderr.equals(System.err) || stderr.equals(System.out)) {
            return;
        }
        stderr.flush();
        stderr.close();
        stderr = System.err;
    }

    @Override
    public void severe(String message)  {
        if (getLogLevel().compareTo(LogLevel.SEVERE) <=0) {
            print(LogLevel.SEVERE, message);
        }
    }

    /**
     * Outputs a message in the format
     * [log level] date/time: liquibase: DatabaseChangeLog name:ChangeSet name: message
     * DEBUG/SQL/INFO message are printer to STDOUT and WARNING/SEVERE messages are printed to STDERR.
     *
     * @param logLevel desired log level
     * @param message the message describing the event
     * @throws UnexpectedLiquibaseException if an internal software error occurs
     */
    protected void print(LogLevel logLevel, String message) throws UnexpectedLiquibaseException {
        if (StringUtils.trimToNull(message) == null) {
            return;
        }

        String outputString = String.format(
                "[%s] %s",
                logLevel,
                DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(new Date()) + ": " + name + ": " + buildMessage(message)
        );
        switch (logLevel) {
            case DEBUG:
                stdout.println(outputString);
                break;
            case SQL:
                stdout.println(outputString);
                break;
            case INFO:
                stdout.println(outputString);
                break;
            case SEVERE:
                stderr.println(outputString);
                break;
            case WARNING:
                stderr.println(outputString);
                break;
            default:
                throw new UnexpectedLiquibaseException("Encountered an unknown log level: " + logLevel.toString());
        }
    }

    @Override
    public void severe(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.SEVERE) <=0) {
            print(LogLevel.SEVERE, message);
            e.printStackTrace(stderr);
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
            e.printStackTrace(stderr);
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
            e.printStackTrace(stderr);
        }
    }

    @Override
    public void sql(String message) {
        if (getLogLevel().compareTo(LogLevel.SQL) <= 0) {
            print(LogLevel.SQL, message);
        }
    }

    @Override
    public void sql(String message, Throwable e) {
        if (getLogLevel().compareTo(LogLevel.SQL) <= 0) {
            print(LogLevel.SQL, message);
            e.printStackTrace(stderr);
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
            e.printStackTrace(stderr);
        }

    }
}
