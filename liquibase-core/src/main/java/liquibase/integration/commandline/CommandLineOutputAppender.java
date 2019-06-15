package liquibase.integration.commandline;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.filter.AbstractMatcherFilter;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.joran.spi.ConsoleTarget;
import ch.qos.logback.core.spi.FilterReply;
import liquibase.logging.LogLevel;
import liquibase.logging.LogType;

import java.util.Iterator;

import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

/**
 * Customized SLF4j appender which outputs user message and/or SQL to pipe from the log to the console.
 */
public class CommandLineOutputAppender extends ConsoleAppender {

    private CommandLineOutputAppender.Mode mode = Mode.STANDARD;

    public CommandLineOutputAppender(ILoggerFactory loggerContext, String target) {
        setContext((LoggerContext) loggerContext);
        setTarget(target);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%msg%n");
        encoder.setContext((LoggerContext) loggerContext);
        encoder.start();
        setEncoder(encoder);

        addFilter(new ModeFilter());
    }

    /**
     * Sets the mode this command line logger should execute in.
     */
    public void setMode(CommandLineOutputAppender.Mode mode) {
        this.mode = mode;
    }


    protected class ModeFilter extends Filter {

        @Override
        public FilterReply decide(Object event) {
            Marker marker = ((LoggingEvent) event).getMarker();
            LogType logType = LogType.valueOf(marker.getName());
            if (CommandLineOutputAppender.this.mode == Mode.STANDARD) {
                if (logType == LogType.USER_MESSAGE && target.equals(ConsoleTarget.SystemOut)) {
                    return FilterReply.ACCEPT;
                } else {
                    return FilterReply.DENY;
                }
            } else if (CommandLineOutputAppender.this.mode == Mode.PIPE_SQL) {
                if (logType == LogType.USER_MESSAGE && target == ConsoleTarget.SystemErr) {
                    return FilterReply.ACCEPT;
                } else if (logType == LogType.WRITE_SQL && target == ConsoleTarget.SystemOut) {
                    return FilterReply.ACCEPT;
                } else {
                    return FilterReply.DENY;
                }
            }
            return FilterReply.DENY;
        }
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

    /**
     * Set up Logback logging to the STDOUT/STDERR console streams.
     */
    protected static void setupLogging(final org.slf4j.Logger rootLogger, final LogLevel defaultLogLevel) {
        Logger root = (ch.qos.logback.classic.Logger) rootLogger;
        // NOTE: Mismatched levels default to debug.
        root.setLevel(Level.toLevel(defaultLogLevel.name()));
    
        ConsoleLogFilter consoleLogFilter = new ConsoleLogFilter();

        Iterator<Appender<ILoggingEvent>> appenderIterator = root.iteratorForAppenders();
        while (appenderIterator.hasNext()) {
            Appender<ILoggingEvent> next = appenderIterator.next();
            if (next instanceof ConsoleAppender) {
                ((ConsoleAppender) next).addFilter(consoleLogFilter);
            }
        }
    
        for (String target : new String[] { "System.out", "System.err" }) {
            CommandLineOutputAppender appender = new CommandLineOutputAppender(LoggerFactory.getILoggerFactory(), target);
            root.addAppender(appender);
            appender.start();
        }
    }

    private static class ConsoleLogFilter extends AbstractMatcherFilter {

        // NOTE: This is never set so the reply is always DENY.
        private boolean outputLogs;

        @Override
        public FilterReply decide(Object event) {
            if (outputLogs) {
                return FilterReply.ACCEPT;
            } else {
                return FilterReply.DENY;
            }
        }
    }
}
