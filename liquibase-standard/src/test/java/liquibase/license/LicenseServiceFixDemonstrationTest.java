package liquibase.license;

import liquibase.Scope;
import liquibase.exception.CommandValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating that the DAT-20812 fix works properly.
 * This test shows that:
 * 1. LicenseServiceFactory no longer returns null in OSS
 * 2. Pro features now get consistent license validation errors instead of NPEs
 * 3. The error messages are user-friendly and indicate the feature requires Pro/Secure
 */
public class LicenseServiceFixDemonstrationTest {

    @Test
    public void testDAT20812FixNullPointerExceptionResolved() {
        // Before fix: This would cause NullPointerException because
        // Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class) returned null
        // After fix: Returns false consistently

        LicenseServiceFactory factory = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class);
        assertNotNull(factory, "Factory should never be null");

        LicenseService licenseService = factory.getLicenseService();
        assertNotNull(licenseService, "LicenseService should never be null in OSS");

        // Verify it behaves as expected for OSS
        assertFalse(licenseService.licenseIsValid("Liquibase Pro"));
        assertFalse(licenseService.licenseIsInstalled());
    }

    @Test
    public void testLicenseServiceUtilsNoLongerThrowsNPE() {
        // Before fix: isProLicenseValid() could throw NPE due to null factory or service
        // After fix: Returns false consistently
        assertDoesNotThrow(() -> {
            boolean isValid = LicenseServiceUtils.isProLicenseValid();
            assertFalse(isValid, "OSS should never have valid Pro license");
        });
    }

    @Test
    public void testProFeatureValidationThrowsProperError() {
        // Before fix: Could throw NPE or use default generic message
        // After fix: Throws CommandValidationException with proper message

        CommandValidationException exception = assertThrows(
            CommandValidationException.class,
            () -> LicenseServiceUtils.checkProLicenseAndThrowException(new String[]{"diff", "--format=json"})
        );

        String message = exception.getMessage();
        assertNotNull(message);

        // Should contain the command name
        assertTrue(message.contains("diff --format=json"),
            "Error message should contain command: " + message);

        // Should indicate it requires Pro/Secure license
        assertTrue(message.contains("Liquibase Secure") || message.contains("Liquibase Pro"),
            "Error message should mention Pro/Secure requirement: " + message);

        // Should provide helpful URL
        assertTrue(message.contains("liquibase.com/trial") || message.contains("trial"),
            "Error message should provide trial URL: " + message);
    }

    @Test
    public void testTagDatabaseScenario() {
        // This test demonstrates the tagDatabase scenario from DAT-20812
        // The actual tagDatabase functionality would be tested elsewhere,
        // but this shows the license validation now works properly

        assertThrows(CommandValidationException.class, () -> {
            LicenseServiceUtils.checkProLicenseAndThrowException(new String[]{"tagDatabase"});
        });
    }

    @Test
    public void testDiffFormatJsonScenario() {
        // This test demonstrates the diff --format=json scenario from DAT-20561
        assertThrows(CommandValidationException.class, () -> {
            LicenseServiceUtils.checkProLicenseAndThrowException(new String[]{"diff", "--format=json"});
        });
    }

    @Test
    public void testIncludeAllScenario() {
        // This test demonstrates the includeAll scenario from DAT-20559
        assertThrows(CommandValidationException.class, () -> {
            LicenseServiceUtils.checkProLicenseAndThrowException(new String[]{"includeAll"});
        });
    }
}