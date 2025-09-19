package liquibase.license;

import liquibase.Scope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that the OSS LicenseService implementation properly handles license validation
 */
public class OSSLicenseServiceTest {

    @Test
    public void testOSSLicenseServiceAlwaysReturnsFalse() {
        OSSLicenseService ossLicenseService = new OSSLicenseService();

        // Should always return false for any license validation
        assertFalse(ossLicenseService.licenseIsInstalled());
        assertFalse(ossLicenseService.licenseIsValid("Liquibase Pro"));
        assertFalse(ossLicenseService.licenseIsValid("any license"));
        assertFalse(ossLicenseService.licenseIsAboutToExpire());

        // Should have minimal valid priority
        assertEquals(0, ossLicenseService.getPriority());

        // Days til expiration should indicate no license
        assertEquals(-1, ossLicenseService.daysTilExpiration());

        // Should provide proper error message
        String[] commandNames = {"diff", "--format=json"};
        String errorMessage = ossLicenseService.getInvalidLicenseMessage(commandNames);
        assertTrue(errorMessage.contains("diff --format=json"));
        assertTrue(errorMessage.contains("Liquibase Secure"));
    }

    @Test
    public void testLicenseServiceFactoryReturnsOSSService() {
        LicenseServiceFactory factory = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class);
        assertNotNull(factory, "LicenseServiceFactory should not be null");

        LicenseService licenseService = factory.getLicenseService();
        assertNotNull(licenseService, "LicenseService should not be null");
        assertTrue(licenseService instanceof OSSLicenseService, "Should be OSS LicenseService");

        // Verify service behaves correctly
        assertFalse(licenseService.licenseIsValid("Liquibase Pro"));
    }

    @Test
    public void testLicenseServiceUtilsProLicenseValidation() {
        // This should now return false instead of throwing NPE
        assertFalse(LicenseServiceUtils.isProLicenseValid());

        // This should throw CommandValidationException with proper message
        assertThrows(liquibase.exception.CommandValidationException.class,
            () -> LicenseServiceUtils.checkProLicenseAndThrowException(new String[]{"diff", "--format=json"}));
    }
}
