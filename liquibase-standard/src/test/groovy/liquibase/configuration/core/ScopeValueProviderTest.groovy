package liquibase.configuration.core

import liquibase.Scope
import liquibase.configuration.ProvidedValue
import spock.lang.Specification
import spock.lang.Unroll

class ScopeValueProviderTest extends Specification {


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

        def passedKey = key
        ProvidedValue foundValue = Scope.child(env, new Scope.ScopedRunnerWithReturn<ProvidedValue>() {
            @Override
            ProvidedValue run() throws Exception {
                new ScopeValueProvider().getProvidedValue(passedKey)
            }
        })

        then:
        if (foundValue == null) {
            assert expectedValue == null
        } else {
            assert foundValue.value == expectedValue
            assert foundValue.describe() == expectedSource
        }


        where:
        key                | expectedValue   | expectedSource
        "lower"            | "saw lower"     | "Scoped value 'lower'"
        "LOWER"            | null            | null
        "upper"            | null            | null
        "UPPER"            | "saw upper"     | "Scoped value 'UPPER'"
        "lower.underscore" | null            | null
        "upper.dot"        | null            | null
        "UPPER.DOT"        | "saw upper dot" | "Scoped value 'UPPER.DOT'"
        "LOWER.UNDER.dot"  | null            | null
        "LOWER_UNDER_DOT"  | null            | null
        "invalid"          | null            | null
        null               | null            | null
    }
}
