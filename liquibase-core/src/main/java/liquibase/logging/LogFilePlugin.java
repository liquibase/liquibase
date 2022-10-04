package liquibase.logging;

import liquibase.plugin.Plugin;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface used to work with log files, specifically for internal Liquibase logging.
 *
 * Instances of these objects are created using the {@link LogFileFactory}.
 */
public interface LogFilePlugin extends Plugin {

    int getPriority(String logFilePath);

    /**
     * Given a log file path, open an output stream to that log file.
     */
    OutputStream getOutputStream(String logFilePath) throws IOException;
}
