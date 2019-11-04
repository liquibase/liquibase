package liquibase.license;

import liquibase.plugin.AbstractPluginFactory;

public class LicenseServiceFactory extends AbstractPluginFactory<LicenseService> {

  private LicenseServiceFactory() {}

  @Override
  protected Class<LicenseService> getPluginClass() {
    return LicenseService.class;
  }

  @Override
  protected int getPriority(LicenseService obj, Object... args) {
    return obj.getPriority();
  }

  public LicenseService getLicenseService() {
    return getPlugin();
  }
}
