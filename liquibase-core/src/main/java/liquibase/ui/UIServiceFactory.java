package liquibase.ui;

import liquibase.plugin.AbstractPluginFactory;

public class UIServiceFactory extends AbstractPluginFactory<UIService> {

    private UIServiceFactory() {
    }

    @Override
    protected Class<UIService> getPluginClass() {
        return UIService.class;
    }

    @Override
    protected int getPriority(UIService uiService, Object... args) {
        return uiService.getPriority();
    }
}
