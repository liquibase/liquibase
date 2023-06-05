package liquibase.logging.core;

import liquibase.logging.LogMessageFilter;

import java.util.logging.Level;

@Deprecated
public class BufferedLogger extends AbstractLogger {

    private final BufferedLogService bufferedLogService;
    private final Class clazz;

    /**
     * @deprecated use {@link #BufferedLogger(Class, BufferedLogService)}
     */
    public BufferedLogger(Class clazz, BufferedLogService bufferedLogService, LogMessageFilter ignored) {
        this(clazz, bufferedLogService);
    }

    public BufferedLogger(Class clazz, BufferedLogService bufferedLogService) {
        this.clazz = clazz;
        this.bufferedLogService = bufferedLogService;
    }

    @Override
    public void log(Level level, String message, Throwable e) {
        if (level == Level.OFF) {
            return;
        }

        this.bufferedLogService.addLog(new BufferedLogService.BufferedLogMessage(level, clazz, filterMessage(message), e));
    }

    @Override
    public void close() throws Exception {

    }

}
