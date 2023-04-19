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

  public static final String TRIAL_LICENSE_URL = "https://liquibase.com/trial";
  private static final String BASE_INVALID_LICENSE_MESSAGE = "Using '%s' requires a valid Liquibase Pro or Labs license. Get a free license key at " + TRIAL_LICENSE_URL + ".";

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
      throw new CommandValidationException(String.format(BASE_INVALID_LICENSE_MESSAGE + " Add liquibase.licenseKey=<yourKey> into your defaults file or use --license-key=<yourKey> before your command in the CLI.", StringUtil.join(commandNames, " ")));
    }
  }
}
