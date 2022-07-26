package liquibase.configuration.core


import spock.lang.Specification
import spock.lang.Unroll

class SystemPropertyValueProviderTest extends Specification {

    @Unroll
    def "getProvidedValue"() {
        expect:
        new SystemPropertyValueProvider().getProvidedValue("USER.NAME").getValue() == System.getProperty("user.name")
    }
}
