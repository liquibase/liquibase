package liquibase.license;

import liquibase.configuration.ConfiguredValue;

import java.util.Date;

/**
 * No-op LicenseService implementation for OSS distribution.
 * This provides a consistent "invalid license" response instead of null,
 * allowing Pro features to properly fail with appropriate error messages.
 */
public class OSSLicenseService implements LicenseService {

    private static final int PRIORITY = 0; // Lowest valid priority (must be >= 0)

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public boolean licenseIsInstalled() {
        return false;
    }

    @Override
    public boolean licenseIsValid(String subject) {
        return false;
    }

    @Override
    public String getLicenseInfo() {
        return "";
    }

    @Override
    public LicenseInfo getLicenseInfoObject() {
        return null;
    }

    @Override
    public LicenseInstallResult installLicense(Location... locations) {
        return new LicenseInstallResult(1, "License installation not supported in OSS distribution");
    }

    @Override
    public void disable() {
        // No-op for OSS
    }

    @Override
    public void reset() {
        // No-op for OSS
    }

    @Override
    public boolean licenseIsAboutToExpire() {
        return false;
    }

    @Override
    public int daysTilExpiration() {
        return -1; // Indicates no license/expired
    }

    @Override
    public Date getExpirationDate() {
        return null;
    }

    @Override
    public String getInvalidLicenseMessage(String[] commandNames) {
        // Use the standard message format from the interface
        return LicenseService.super.getInvalidLicenseMessage(commandNames);
    }

    @Override
    public ConfiguredValue<String> getLicenseKey() {
        return null;
    }
}
