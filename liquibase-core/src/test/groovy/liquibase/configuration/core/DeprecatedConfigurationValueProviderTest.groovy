package liquibase.configuration.core

import spock.lang.Specification

class DeprecatedConfigurationValueProviderTest extends Specification {

    def "can set and clear values"() {
        when:
        DeprecatedConfigurationValueProvider.setData("test.key", "test value")

        then:
        new DeprecatedConfigurationValueProvider().getProvidedValue("test.key").getValue() == "test value"
        new DeprecatedConfigurationValueProvider().getProvidedValue("invalid.key") == null

        when:
        DeprecatedConfigurationValueProvider.clearData()

        then:
        new DeprecatedConfigurationValueProvider().getProvidedValue("test.key") == null

    }
}
