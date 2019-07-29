package liquibase.license;

import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.changelog.ChangeSet;

public class LicenseServiceUtils {
    private static final Logger LOG = LogFactory.getLogger();

    public static ValidationErrors checkForValidLicense(String licenseType, ChangeSet changeSet) {
      LicenseService licenseService = LicenseServiceFactory.getInstance().getLicenseService(licenseType);
      if (licenseService == null) {
        return new ValidationErrors();
      }

      if (licenseService.licenseIsValid(licenseType)) {
        String message = "Found valid LiquibasePro license";
        LOG.info(message);
        return new ValidationErrors();
      }
      ValidationErrors validationErrors = new ValidationErrors();
      String message = "Change Set ID: " + changeSet.getId() + " Change Set Author: " + changeSet.getAuthor() + "\n";
      message += "Change Type 'pro:createFunction' is not allowed without a valid Liquibase Pro License.\n";
      message += "To purchase or renew a Liquibase Pro license key please contact lbprosupport@datical.com or\n" +
                "go to https://download.liquibase.org/download/pricing";
      validationErrors.addError(message);
      return validationErrors;
    }
}
