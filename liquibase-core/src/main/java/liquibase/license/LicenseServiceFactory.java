package liquibase.license;

import java.util.Map;
import java.util.HashMap;

import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.servicelocator.ServiceLocator;

public class LicenseServiceFactory {
  private static final Logger LOG = LogService.getLog(LicenseServiceFactory.class);
  private Map<String, LicenseService> licenseServiceMap = new HashMap<String, LicenseService>();
  private static LicenseServiceFactory INSTANCE = new LicenseServiceFactory();
  private LicenseServiceFactory() {}

  public static LicenseServiceFactory getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new LicenseServiceFactory();
    }
    return INSTANCE;
  }

  public LicenseService getLicenseService(String licenseType) {
    LicenseService licenseService = null;
    if (licenseServiceMap.containsKey(licenseType)) {
      licenseService = licenseServiceMap.get(licenseType);
    }
    else {
      Class<? extends LicenseService>[] classes = ServiceLocator.getInstance().findClasses(LicenseService.class);
      if (classes.length > 0) {
        try {
          int highPriority = -1;
          for (Class<? extends LicenseService> clazz : classes) {
            LicenseService test = licenseService = clazz.newInstance();
            int priority = test.getPriority(licenseType);
            if (priority > highPriority && priority > 0) {
              highPriority = priority;
              licenseService = test;
            }
          }
          licenseServiceMap.put(licenseType, licenseService);
        }
        catch (InstantiationException | IllegalAccessException e) {
          LOG.severe("Unable to instance LicenseService", e);
        }
      }
    }
    return licenseService;
  }
}