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

  /**
   * Check for a Liquibase Pro License.
   * @return true if licensed, false if not
   */
  public static boolean isProLicenseValid() {
    LicenseServiceFactory licenseServiceFactory = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class);
    if (licenseServiceFactory == null) {
      return false;
    }

    LicenseService licenseService = licenseServiceFactory.getLicenseService();
    if (licenseService == null) {
      return false;
    }
    return licenseService.licenseIsValid("Liquibase Pro");
  }

}
