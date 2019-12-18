package liquibase.logging;

import liquibase.plugin.AbstractPluginFactory;

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
}
