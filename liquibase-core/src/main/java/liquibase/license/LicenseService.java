package liquibase.license;

import liquibase.license.LiquibaseProLicense;

public interface LicenseService {
  LiquibaseProLicense isValidLicense(String licenseType);
}
