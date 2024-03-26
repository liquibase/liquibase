package liquibase.configuration.core

import spock.lang.Specification

class SystemPropertyValueProviderTest extends Specification {

    def "getProvidedValue"() {
        expect:
        new SystemPropertyValueProvider().getProvidedValue("USER.NAME").getValue() == System.getProperty("user.name")
    }
}
