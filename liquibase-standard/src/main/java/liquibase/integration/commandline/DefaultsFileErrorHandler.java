package liquibase.integration.commandline;

import liquibase.plugin.Plugin;

public interface DefaultsFileErrorHandler extends Plugin {
    void fileNotFound(String defaultsFileConfigValue);

    int getPriority();
}
