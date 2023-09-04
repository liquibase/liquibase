package liquibase.logging.core;

import liquibase.logging.LogService;
import liquibase.plugin.AbstractPluginFactory;

public class LogServiceFactory extends AbstractPluginFactory<LogService> {

    private LogServiceFactory() {
    }

    @Override
    protected Class<LogService> getPluginClass() {
        return LogService.class;
    }

    public LogService getDefaultLogService() {
        return getPlugin(PLAIN_PRIORITIZED_SERVICE);
    }
}
