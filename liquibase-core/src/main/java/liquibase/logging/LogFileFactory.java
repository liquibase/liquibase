package liquibase.logging;

import liquibase.plugin.AbstractPluginFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This factory supports the creation of log files for internal Liquibase logging.
 */
public class LogFileFactory extends AbstractPluginFactory<LogFilePlugin> {
    @Override
    protected Class<LogFilePlugin> getPluginClass() {
        return LogFilePlugin.class;
    }

    @Override
    protected int getPriority(LogFilePlugin obj, Object... args) {
        return obj.getPriority((String) args[0]);
    }

    /**
     * Given a log file path, open an output stream to that log file.
     */
    public OutputStream getOutputStream(String logFilePath) throws IOException {
        LogFilePlugin plugin = getPlugin(logFilePath);
        return plugin.getOutputStream(logFilePath);
    }
}
