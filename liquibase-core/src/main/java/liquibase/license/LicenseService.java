package liquibase.license; 

import java.util.List;

public interface LicenseService {

  /**
   *
   * This method returns a priority value for an implementation
   *
   * @param   licenseType       String
   * @return  int
   *
   */
  int getPriority(String licenseType);

  /**
   * This method checks whether there is any license with any valid subject installed.
   *  
   * @return true if any license with any valid subject is installed.
   */
  boolean licenseIsInstalled();
  
  /**
   * Check if an installed license with the given subject is valid or not. The set of 
   * subjects that are valid is defined by the implementation.
   * 
   * @return true if the license with the given subject is valid.
   */
  boolean licenseIsValid(String subject);
  
  /**
   * @return a string representation of the license(s) installed for display in logs, etc.
   */
  String getLicenseInfo();

  /**
   * Return the number of installed licenses. Note that some of the installed licenses 
   * may not be valid, so this is mainly for informational messages.
   * 
   */
  int getNumLicensesInstalled();
  
  /**
   * Given a list of potential locations that a license file could be located,
   * check each one and install any .lic files that are found there, iterating until
   * a valid license is installed successfully or all the locations have been tried.
   * After calling this method, clients still need to check licenseIsValid().
   * 
   * @param locations - A variable number of Location objects, each of which has a name, 
   * a type, and a value.
   * @return A list of strings that detail the locations checked and the result of 
   * checking each location. 
   */
  List<String> installLicense(Location... locations);

  /**
   * @return true if any installed license is valid but will expire within the next 30 days.
   */
  boolean licenseIsAboutToExpire();

  /**
   * Remove any existing licenses with any valid subjects.
   * 
   * @return true if license was removed or if no license exists. 
   * false if there was an error.
   */
  boolean uninstallLicense();

  /**
   * It is possible that users might have multiple licenses installed. In that case,
   * this will return the lowest number.
   * 
   * @return the number of whole days until the license expires. Negative numbers 
   * would indicate that the license expired that many days ago.
   */
  int daysTilExpiration();

  /**
   * Method to allow the license key to persisted in memory
   *
   * @param  key
   *
   */
   void setKey(String key);
}
