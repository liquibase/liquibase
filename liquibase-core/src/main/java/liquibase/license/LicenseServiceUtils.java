package liquibase.license;

import liquibase.Scope;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.changelog.ChangeSet;
import liquibase.plugin.PluginFactory;

/**
 *
 * This class provides a static method for verifying licenses
 *
 */
public class LicenseServiceUtils {

    public static ValidationErrors checkForValidLicense(String licenseType, Change change) {
      LicenseService licenseService = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class).getLicenseService();
      if (licenseService == null) {
        return new ValidationErrors();
      }
      if (licenseService.licenseIsValid(licenseType)) {
        String message = String.format("Found valid license with subject '%s'",licenseType);
        Scope.getCurrentScope().getLog(LicenseService.class).info(message);
        return new ValidationErrors();
      }

      ChangeSet changeSet = change.getChangeSet();
      String changeType = Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(change).getName();
      ValidationErrors validationErrors = new ValidationErrors();
      String message = "Change Set ID: " + changeSet.getId() + " Change Set Author: " + changeSet.getAuthor() + "\n";
      message += "Change Type 'pro:" + changeType + "' is not allowed without a valid Liquibase Pro License.\n";
      message += "To purchase or renew a Liquibase Pro license key please contact lbprosales@datical.com or\n" +
                "go to https://download.liquibase.org/liquibase-pro-pricing-details";
      validationErrors.addError(message);
      return validationErrors;
    }
}
