package liquibase.telemetry

import liquibase.Scope
import liquibase.license.LicenseInfo
import liquibase.license.LicenseInstallResult
import liquibase.license.LicenseService
import liquibase.license.LicenseServiceFactory
import liquibase.license.Location
import liquibase.telemetry.configuration.TelemetryArgs
import liquibase.telemetry.configuration.TelemetryConfiguration
import liquibase.telemetry.configuration.TelemetryConfigurationFactory
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Constructor

class TelemetryArgsTest extends Specification {

    @Unroll
    def "test all permutations of options for enabling/disabling"(Boolean userCliOption, boolean remoteProEnabled, boolean remoteOssEnabled, boolean hasLicenseKey, boolean isEnabled) {
        setup:
        def telemetryConfigurationFactory = Scope.getCurrentScope().getSingleton(TelemetryConfigurationFactory.class)
        def existingConfig = telemetryConfigurationFactory.getPlugin()
        telemetryConfigurationFactory.removeInstance(existingConfig)
        def mockConfig = new TelemetryConfiguration() {
            @Override
            int getPriority() {
                return PRIORITY_SPECIALIZED
            }

            @Override
            boolean isOssTelemetryEnabled() throws Exception {
                return remoteOssEnabled
            }

            @Override
            boolean isProTelemetryEnabled() throws Exception {
                return remoteProEnabled
            }
        }
        telemetryConfigurationFactory.register(mockConfig)
        def licenseServiceFactory = Scope.getCurrentScope().getSingleton(LicenseServiceFactory.class)
        def existingLicenseService = licenseServiceFactory.getLicenseService()
        licenseServiceFactory.unregister(existingLicenseService)

        def mockLicenseService = new LicenseService() {
            @Override
            int getPriority() {
                return PRIORITY_SPECIALIZED
            }

            @Override
            boolean licenseIsInstalled() {
                return true
            }

            @Override
            boolean licenseIsValid(String subject) {
                return hasLicenseKey
            }

            @Override
            String getLicenseInfo() {
                return null
            }

            @Override
            LicenseInfo getLicenseInfoObject() {
                return null
            }

            @Override
            LicenseInstallResult installLicense(Location... locations) {
                return null
            }

            @Override
            void disable() {

            }

            @Override
            boolean licenseIsAboutToExpire() {
                return false
            }

            @Override
            int daysTilExpiration() {
                return 0
            }
        }
        licenseServiceFactory.register(mockLicenseService)

        when:
        Map<String, ?> scopeKeys = new HashMap<>()
        scopeKeys.put(TelemetryArgs.ENABLED.getKey(), userCliOption)
        Boolean actuallyEnabled = Scope.child(scopeKeys, () -> {
            return TelemetryArgs.isTelemetryEnabled()
        } as Scope.ScopedRunnerWithReturn)

        then:
        actuallyEnabled == isEnabled

        cleanup:
        telemetryConfigurationFactory.removeInstance(mockConfig)
        telemetryConfigurationFactory.register(existingConfig)

        where:
        userCliOption | remoteProEnabled | remoteOssEnabled | hasLicenseKey | isEnabled
        true          | true             | true             | true          | true
        true          | true             | true             | false         | true
        true          | true             | false            | true          | true
        true          | true             | false            | false         | false
        true          | false            | true             | true          | false // is this right?
        true          | false            | true             | false         | true
        true          | false            | false            | true          | false
        true          | false            | false            | false         | false
        false         | true             | true             | true          | false
        false         | true             | true             | false         | false
        false         | true             | false            | true          | false
        false         | true             | false            | false         | false
        false         | false            | true             | true          | false
        false         | false            | true             | false         | false
        false         | false            | false            | true          | false
        false         | false            | false            | false         | false
        null          | true             | true             | true          | true
        null          | true             | true             | false         | true
        null          | true             | false            | true          | true
        null          | true             | false            | false         | false
        null          | false            | true             | true          | false // is this right?
        null          | false            | true             | false         | true
        null          | false            | false            | true          | false
        null          | false            | false            | false         | false
    }
}
