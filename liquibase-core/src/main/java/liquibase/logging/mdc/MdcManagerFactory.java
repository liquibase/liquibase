package liquibase.logging.mdc;

import liquibase.plugin.AbstractPluginFactory;

public class MdcManagerFactory extends AbstractPluginFactory<MdcManager> {
    private MdcManagerFactory() {
    }

    @Override
    protected Class<MdcManager> getPluginClass() {
        return MdcManager.class;
    }

    @Override
    protected int getPriority(MdcManager obj, Object... args) {
        return obj.getPriority();
    }

    public MdcManager getMdcManager() {
        return getPlugin();
    }

    public void unregister(MdcManager manager) {
        this.removeInstance(manager);
    }
}
