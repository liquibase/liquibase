package liquibase.configuration.core


import spock.lang.Specification
import spock.lang.Unroll

class SystemEnvironmentValueProviderTest extends Specification {

    @Unroll
    def "GetValue"() {
        when:
        def env = [
                "lower"           : "saw lower",
                "lower_underscore": "saw lower underscore",
                "lower.dot"       : "saw lower dot",
                "lower_under.dot" : "saw lower word dot",
                "UPPER"           : "saw upper",
                "UPPER_UNDERSCORE": "saw upper underscore",
                "UPPER.DOT"       : "saw upper dot",
                "UPPER_UNDER_DOT" : "saw under dot",
        ]
        def provider = new SystemEnvironmentValueProvider() {
            @Override
            protected String getEnvironmentVariable(String name) {
                return env[name]
            }
        }

        def value = provider.getValue(key)

        then:
        if (value == null) {
            assert expectedValue == null
        } else {
            assert value.value == expectedValue
            assert value.describe() == expectedDescription
        }

        where:
        key                | expectedValue          | expectedDescription
        "lower"            | "saw lower"            | "Environment variable 'lower'"
        "LOWER"            | "saw lower"            | "Environment variable 'lower'"
        "upper"            | "saw upper"            | "Environment variable 'UPPER'"
        "UPPER"            | "saw upper"            | "Environment variable 'UPPER'"
        "lower_underscore" | "saw lower underscore" | "Environment variable 'lower_underscore'"
        "lower.underscore" | "saw lower underscore" | "Environment variable 'lower_underscore'"
        "lower_UNDERSCORE" | "saw lower underscore" | "Environment variable 'lower_underscore'"
        "upper_underscore" | "saw upper underscore" | "Environment variable 'UPPER_UNDERSCORE'"
        "UPPER_UNDERSCORE" | "saw upper underscore" | "Environment variable 'UPPER_UNDERSCORE'"
        "upper_dot"        | null                   | null
        "upper.dot"        | "saw upper dot"        | "Environment variable 'UPPER.DOT'"
        "UPPER.DOT"        | "saw upper dot"        | "Environment variable 'UPPER.DOT'"
        "lower_under.dot"  | "saw lower word dot"   | "Environment variable 'lower_under.dot'"
        "LOWER.UNDER.dot"  | null                   | "Environment variable 'lower_under.dot'"
        "LOWER_UNDER_DOT"  | null                   | "Environment variable 'lower_under.dot'"
        "invalid"          | null                   | null
        null               | null                   | null
    }
}
