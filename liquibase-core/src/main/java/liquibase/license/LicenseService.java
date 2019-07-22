package liquibase.license;

public interface LicenseService {
  boolean isValidLicense(String licenseType);
}
