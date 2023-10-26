package liquibase.checksum;

import liquibase.plugin.AbstractPluginFactory;

public class LatestChecksumVersionFactory extends AbstractPluginFactory<LatestChecksumVersionPlugin> {

    private LatestChecksumVersionFactory() {
    }

    @Override
    protected Class<LatestChecksumVersionPlugin> getPluginClass() {
        return LatestChecksumVersionPlugin.class;
    }

    @Override
    protected int getPriority(LatestChecksumVersionPlugin obj, Object... args) {
        return obj.getPriority();
    }

    public LatestChecksumVersionPlugin get() {
        return getPlugin();
    }
}
