package liquibase.configuration.core


import spock.lang.Specification
import spock.lang.Unroll

class SystemPropertyValueProviderTest extends Specification {

    @Unroll
    def "GetValue"() {
        when:
        def properties = new Properties()
        properties.setProperty("lower", "saw lower")
        properties.setProperty("lower.dot", "saw lower dot")
        properties.setProperty("lower_under.dot", "saw lower word dot")
        properties.setProperty("UPPER", "saw upper")
        properties.setProperty("UPPER.DOT", "saw upper dot")
        properties.setProperty("Mixed.Case", "saw mixed case")

        def provider = new SystemPropertyValueProvider() {
            @Override
            protected Properties getSystemProperties() {
                return properties
            }
        }

        def value = provider.getValue(key)

        then:
        if (value == null) {
            assert expectedValue == null
        } else {
            assert value.value == expectedValue
            assert value.describe() == expectedSource
        }

        where:
        key                | expectedValue   | expectedSource
        "lower"            | "saw lower"     | "System property 'lower'"
        "LOWER"            | "saw lower"     | "System property 'lower'"
        "upper"            | "saw upper"     | "System property 'UPPER'"
        "UPPER"            | "saw upper"     | "System property 'UPPER'"
        "lower.underscore" | null            | null
        "upper.dot"        | "saw upper dot" | "System property 'UPPER.DOT'"
        "UPPER.DOT"        | "saw upper dot" | "System property 'UPPER.DOT'"
        "LOWER.UNDER.dot"  | null            | null
        "LOWER_UNDER_DOT"  | null            | null
        "invalid"          | null            | null
        null               | null            | null
    }
}
