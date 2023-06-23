package liquibase.license;

import liquibase.plugin.AbstractPluginFactory;

public class LicenseServiceFactory extends AbstractPluginFactory<LicenseService> {

    private LicenseServiceFactory() {
    }

    @Override
    protected Class<LicenseService> getPluginClass() {
        return LicenseService.class;
    }

    public LicenseService getLicenseService() {
        return getPlugin(PLAIN_PRIORITIZED_SERVICE);
    }

    public void unregister(LicenseService service) {
        this.removeInstance(service);
    }
}
