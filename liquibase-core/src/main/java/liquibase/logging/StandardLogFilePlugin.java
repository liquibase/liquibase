package liquibase.logging;

import liquibase.Scope;
import liquibase.plugin.Plugin;
import liquibase.resource.OpenOption;
import liquibase.resource.PathHandlerFactory;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Standard implementation of the LogFilePlugin which is used by default. This implementation will write
 * log files with an "append" flag so that existing data in the log files is not overwritten. Not all providers
 * support appending to files.
 */
public class StandardLogFilePlugin implements LogFilePlugin {

    @Override
    public int getPriority(String logFilePath) {
        return Plugin.PRIORITY_DEFAULT;
    }

    @Override
    public OutputStream getOutputStream(String logFilePath) throws IOException {
        final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
        return pathHandlerFactory.openResourceOutputStream(logFilePath, true, OpenOption.APPEND);
    }
}
