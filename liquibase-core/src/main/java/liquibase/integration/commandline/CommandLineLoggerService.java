package liquibase.integration.commandline;

import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.logging.core.AbstractLogService;
import liquibase.logging.core.AbstractLogger;

import java.util.Map;
import java.util.logging.Level;

/**
 * Customized log service which outputs user message and/or SQL to pipe from the log to the console.
 */
public class CommandLineLoggerService extends AbstractLogService {

    private CommandLineLoggerService.Mode mode = Mode.STANDARD;

    private CommandLineLogger logger = new CommandLineLogger();

    /**
     * Sets the mode this command line logger should execute in.
     */
    public void setMode(CommandLineLoggerService.Mode mode) {
        this.mode = mode;
    }

    @Override
    public int getPriority() {
        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public Logger getLog(Class clazz) {
        return logger;
    }

    public enum Mode {
        /**
         * "Normal" mode where only user-level messages are printed to stdout.
         */
        STANDARD,

        /**
         * Command line is expecting to be output SQL to stdout that may be piped to a file.
         * Any non-sql messages should be sent to stderr.
         */
        PIPE_SQL,

        /**
         * Outputs all log messages to stdout
         */
        FULL_LOG,
    }

    private class CommandLineLogger extends AbstractLogger {

        private Level level = Level.SEVERE;

        @Override
        public void log(Level level, LogType logType, String message, Throwable e) {
            if (level.intValue() < this.level.intValue()) {
                return;
            }
            if (CommandLineLoggerService.this.mode == Mode.STANDARD) {
                if (logType == LogType.USER_MESSAGE) {
                    System.out.println(message);
                }
            } else if (CommandLineLoggerService.this.mode == Mode.PIPE_SQL) {
                if (logType == LogType.USER_MESSAGE) {
                    System.err.println(message);
                } else if (logType == LogType.WRITE_SQL) {
                    System.out.println(message);
                }
            }
        }
    }
}
