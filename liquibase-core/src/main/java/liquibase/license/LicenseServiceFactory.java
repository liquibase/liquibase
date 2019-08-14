package liquibase.license;

import java.util.Map;
import java.util.HashMap;

import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.servicelocator.ServiceLocator;

public class LicenseServiceFactory {
  private static final Logger LOG = LogService.getLog(LicenseServiceFactory.class);
  private LicenseService licenseService;
  private static LicenseServiceFactory INSTANCE = new LicenseServiceFactory();
  private LicenseServiceFactory() {}

  public static LicenseServiceFactory getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new LicenseServiceFactory();
    }
    return INSTANCE;
  }

  public LicenseService getLicenseService() {
    if (licenseService != null) {
      return licenseService;
    }
    else {
      Class<? extends LicenseService>[] classes = ServiceLocator.getInstance().findClasses(LicenseService.class);
      if (classes.length > 0) {
        try {
          int highPriority = -1;
          for (Class<? extends LicenseService> clazz : classes) {
            LicenseService test = clazz.newInstance();
            int priority = test.getPriority();
            LOG.debug(String.format("Found an implementation of LicenseService named '%s' with priority %d",test.getClass().getName(),priority));
            if (priority > highPriority && priority > 0) {
              highPriority = priority;
              licenseService = test;
            }
          }
        }
        catch (InstantiationException | IllegalAccessException e) {
          LOG.severe("Unable to instantiate LicenseService", e);
        }
      }
    }
    return licenseService;
  }
}