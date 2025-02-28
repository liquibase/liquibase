package liquibase.configuration.core


import org.apache.commons.lang3.SystemProperties
import spock.lang.Specification
import spock.lang.Unroll

class SystemPropertyValueProviderTest extends Specification {

    @Unroll
    def "getProvidedValue"() {
        expect:
        new SystemPropertyValueProvider().getProvidedValue("USER.NAME").getValue() == SystemProperties.getUserName()
    }
}
