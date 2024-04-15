package liquibase.integration.commandline;

import liquibase.plugin.AbstractPluginFactory;

public class DefaultsFileErrorHandlerFactory extends AbstractPluginFactory<DefaultsFileErrorHandler> {
    @Override
    protected Class<DefaultsFileErrorHandler> getPluginClass() {
        return DefaultsFileErrorHandler.class;
    }

    @Override
    protected int getPriority(DefaultsFileErrorHandler obj, Object... args) {
        return obj.getPriority();
    }

    public DefaultsFileErrorHandler getDefaultsFileErrorHandler() {
        return getPlugin();
    }
}
