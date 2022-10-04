package liquibase.logging;

import liquibase.Scope;
import liquibase.plugin.Plugin;
import liquibase.resource.OpenOption;
import liquibase.resource.PathHandlerFactory;

import java.io.IOException;
import java.io.OutputStream;

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
