package liquibase.configuration.core

import spock.lang.Specification
import spock.lang.Unroll

class EnvironmentVariableProviderTest extends Specification {

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
        def provider = new EnvironmentVariableProvider() {
            @Override
            protected String getEnvironmentVariable(String name) {
                return env[name]
            }
        }

        then:
        provider.getValue(key) == expected

        where:
        key                | expected
        "lower"            | "saw lower"
        "LOWER"            | "saw lower"
        "upper"            | "saw upper"
        "UPPER"            | "saw upper"
        "lower_underscore" | "saw lower underscore"
        "lower.underscore" | "saw lower underscore"
        "lower_UNDERSCORE" | "saw lower underscore"
        "upper_underscore" | "saw upper underscore"
        "UPPER_UNDERSCORE" | "saw upper underscore"
        "upper_dot"        | null
        "upper.dot"        | "saw upper dot"
        "UPPER.DOT"        | "saw upper dot"
        "lower_under.dot"  | "saw lower word dot"
        "LOWER.UNDER.dot"  | null
        "LOWER_UNDER_DOT"  | null
        "invalid"          | null
        null               | null
    }
}
