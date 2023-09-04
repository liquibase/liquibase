package liquibase.logging.mdc;

import liquibase.plugin.AbstractPluginFactory;

public class MdcManagerFactory extends AbstractPluginFactory<MdcManager> {
    private MdcManagerFactory() {
    }

    @Override
    protected Class<MdcManager> getPluginClass() {
        return MdcManager.class;
    }

    public MdcManager getMdcManager() {
        return getPlugin(PLAIN_PRIORITIZED_SERVICE);
    }

    public void unregister(MdcManager manager) {
        this.removeInstance(manager);
    }
}
