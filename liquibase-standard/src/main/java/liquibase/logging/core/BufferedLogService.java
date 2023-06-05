package liquibase.logging.core;

import liquibase.logging.Logger;
import liquibase.util.ISODateFormat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

@Deprecated
public class BufferedLogService extends AbstractLogService {
    //
    // Truncate the return value at 10MB = 10,000,000 bytes
    //
    public static final int MAX_LOG_LENGTH = 10000000;
    private final List<BufferedLogMessage> log = Collections.synchronizedList(new ArrayList<>());


    @Override
    public int getPriority() {
        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public Logger getLog(Class clazz) {
        return new BufferedLogger(clazz, this);
    }


    public List<BufferedLogMessage> getLog() {
        return log;
    }

    public String getLogAsString(Level minimumLevel) {
        StringBuilder returnLog = new StringBuilder();
        for (BufferedLogMessage message : log) {
            if (minimumLevel == null || minimumLevel.intValue() <= message.getLevel().intValue()) {
                returnLog.append("[").append(new ISODateFormat().format(message.getTimestamp())).append("] ");
                returnLog.append(message.getLevel().getName()).append(" ");
                returnLog.append(message.getMessage());
                returnLog.append("\n");

                if (message.getThrowable() != null) {
                    try (final StringWriter stringWriter = new StringWriter();
                         final PrintWriter printWriter = new PrintWriter(stringWriter)) {

                        message.getThrowable().printStackTrace(printWriter);
                        printWriter.flush();

                        returnLog.append(stringWriter).append("\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (returnLog.length() > MAX_LOG_LENGTH) {
            returnLog.setLength(MAX_LOG_LENGTH);
        }
        return returnLog.toString();
    }

    public void addLog(BufferedLogMessage log) {
        this.log.add(log);
    }

    public static class BufferedLogMessage {
        private final Date timestamp;
        private final Level level;
        private final Class location;
        private final String message;
        private final Throwable throwable;

        public BufferedLogMessage(Level level, Class location, String message, Throwable throwable) {
            this.timestamp = new Date();
            this.location = location;
            this.level = level;
            this.message = message;
            this.throwable = throwable;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public Level getLevel() {
            return level;
        }

        public Class getLocation() {
            return location;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getThrowable() {
            return throwable;
        }
    }
}
