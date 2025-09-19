package liquibase.license;

import liquibase.Scope;
import liquibase.exception.CommandValidationException;
import liquibase.util.StringUtil;

/**
 *
 * This class provides a static method for verifying licenses
 *
 */
public class LicenseServiceUtils {

  private LicenseServiceUtils() {
    // Utility class
  }

  /**
   * Check for a Liquibase Pro License.
   * @return true if licensed, or the installed license also permits access to Liquibase Pro features, false if not
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

  /**
   * Throw an exception if there is no valid pro license.
   * @param commandNames the name of the command; each element of the array will be joined by spaces
   * @throws CommandValidationException the exception thrown if the license is not valid
   */
  public static void checkProLicenseAndThrowException(String[] commandNames) throws CommandValidationException {
    if (!isProLicenseValid()) {
      LicenseServiceFactory licenseServiceFactory = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class);
      LicenseService licenseService = licenseServiceFactory.getLicenseService();
      if (licenseService == null) {
          // use default message from core
          throw new CommandValidationException(String.format(LicenseService.BASE_INVALID_LICENSE_MESSAGE + LicenseService.END_INVALID_LICENSE_MESSAGE, StringUtil.join(commandNames, " ")));
      }
      throw new CommandValidationException(licenseService.getInvalidLicenseMessage(commandNames));
    }
  }
}
