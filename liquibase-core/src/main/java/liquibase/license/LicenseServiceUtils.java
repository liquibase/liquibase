package liquibase.license;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.changelog.ChangeSet;
import liquibase.exception.ValidationErrors;

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
        String message = String.format("Found valid license with subject '%s' for '%s'",licenseType, change.getDescription());
        Scope.getCurrentScope().getLog(LicenseService.class).fine(message);
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

  /**
   * check for a Liquibase Pro License, return true if licensed, false if not
   * @param licenseType
   * @return
   */
  public static boolean checkForValidLicense(String licenseType) {
    LicenseService licenseService = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class).getLicenseService();
    if (licenseService == null) {
      return false;
    }
    if (licenseService.licenseIsValid(licenseType)) {
      return true;
    }
    return false;
  }

}
