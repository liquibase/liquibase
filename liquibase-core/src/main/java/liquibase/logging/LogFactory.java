package liquibase.logging;

import liquibase.Scope;
import liquibase.plugin.AbstractPluginFactory;

import java.util.logging.Level;

public class LogFactory extends AbstractPluginFactory<LogService> {

    private LogFactory() {
    }

    @Override
    protected Class<LogService> getPluginClass() {
        return LogService.class;
    }

    @Override
    protected int getPriority(LogService logService, Object... args) {
        return logService.getPriority();
    }

    /**
     * @deprecated Use {@link Scope#getLog(Class)}
     */
    @Deprecated
    public static Logger getLogger(String ignored) {
        return Scope.getCurrentScope().getLog(LogFactory.class);
    }

    /**
     * @deprecated Use {@link Scope#getLog(Class)}
     */
    @Deprecated
    public static Logger getLogger() {
        return Scope.getCurrentScope().getLog(LogFactory.class);
    }

    public Level parseLogLevel(String logLevelName) throws IllegalArgumentException {
        logLevelName = logLevelName.toUpperCase();

        Level logLevel;
        if (logLevelName.equals("DEBUG")) {
            logLevel = Level.FINE;
        } else if (logLevelName.equals("WARN")) {
            logLevel = Level.WARNING;
        } else if (logLevelName.equals("ERROR")) {
            logLevel = Level.SEVERE;
        } else {
            logLevel = Level.parse(logLevelName);
        }
        return logLevel;
    }
}
