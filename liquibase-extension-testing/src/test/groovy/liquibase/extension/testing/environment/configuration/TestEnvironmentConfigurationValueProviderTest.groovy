package liquibase.extension.testing.environment.configuration

import spock.lang.Specification

class TestEnvironmentConfigurationValueProviderTest extends Specification {

    def "getProvidedValue"() {
        when:
        def provider = new TestEnvironmentConfigurationValueProvider()

        then:
        provider.getProvidedValue("liquibase.sdk.env.postgresql.username").getValue() == "lbuser"
    }
}
