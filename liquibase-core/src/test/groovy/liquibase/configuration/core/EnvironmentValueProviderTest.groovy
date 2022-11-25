package liquibase.configuration.core

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.hub.HubConfiguration
import liquibase.license.LicenseInstallResult
import liquibase.license.LicenseService
import liquibase.license.LicenseServiceFactory
import liquibase.license.Location
import spock.lang.Specification
import spock.lang.Unroll

class EnvironmentValueProviderTest extends Specification {
    static boolean licenseValid = false;
    static final mockLicenseService = new LicenseService() {
        @Override
        int getPriority() {
            return Integer.MAX_VALUE
        }

        @Override
        boolean licenseIsInstalled() {
            return false
        }

        @Override
        boolean licenseIsValid(String subject) {
            return licenseValid
        }

        @Override
        String getLicenseInfo() {
            return "Mock license"
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
            return 100
        }

        void setLicenseValid(boolean aBoolean) {
            this.licenseValid = aBoolean
        }
    }

    def setup() {
        def licenseFactory = Scope.currentScope.getSingleton(LicenseServiceFactory)
        licenseFactory.register(mockLicenseService)
    }

    def cleanup() {
        def licenseFactory = Scope.currentScope.getSingleton(LicenseServiceFactory)
        licenseFactory.unregister(mockLicenseService)
    }

    @Unroll
    def "getProvidedValue doesn't require license"() {
        setup:
        mockLicenseService.setLicenseValid(false)

        expect:
        new EnvironmentValueProvider().getProvidedValue("java.home") != null
    }

    @Unroll
    def "getProvidedValue with license"() {
        setup:
        mockLicenseService.setLicenseValid(true)

        when:
        def provider = new EnvironmentValueProvider() {
            @Override
            protected Map<?, ?> getMap() {
                return [
                        "LIQUIBASE_HUB_URL"                 : "http://example.com",
                        "LIQUIBASE_CHANGELOG_LOCK_POLL_RATE": "5",
                        "LIQUIBASE_PRO_MARK_UNUSED_NOT_DROP": "true",
                ]
            }
        }

        then:
        provider.getProvidedValue(input).value == expected

        where:
        input                                                   | expected
        HubConfiguration.LIQUIBASE_HUB_URL.getKey()             | "http://example.com"
        GlobalConfiguration.CHANGELOGLOCK_POLL_RATE.getKey()    | "5"
        "LIQUIBASE_PRO_MARK_UNUSED_NOT_DROP"                    | "true"

    }

    @Unroll
    def "keyMatches"() {
        expect:
        assert new EnvironmentValueProvider().keyMatches(wantedKey, storedKey) == matches

        where:
        wantedKey        | storedKey         | matches
        "single"         | "SINGLE"          | true
        "parent.child"   | "parent.CHILD"    | true
        "parent.child"   | "parent-CHILD"    | true
        "parent.child"   | "parent_CHILD"    | true
        "parent.twoWord" | "parent_two_word" | true
        "parent.twoWord" | "parent_twoword"  | false
        "invalid"        | "parent_child"    | false
        "no.space"       | "no_space "       | true
    }
}
