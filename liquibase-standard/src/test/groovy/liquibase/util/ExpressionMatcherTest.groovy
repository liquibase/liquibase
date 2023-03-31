package liquibase.util

import spock.lang.Specification
import spock.lang.Unroll

class ExpressionMatcherTest extends Specification {

    @Unroll
    def matches() {
        expect:
        ExpressionMatcher.matches(expression, items) == expected

        where:
        expression              | items           | expected
        "x"                     | ["x"]           | true
        "y"                     | ["x", "y", "z"] | true
        "Y"                     | ["x", "y", "z"] | true
        "!y"                    | ["x", "y", "z"] | false
        "not y"                 | ["x", "y", "z"] | false
        "!a"                    | ["x", "y", "z"] | true
        "a or x"                | ["x", "y", "z"] | true
        "b or not a"            | ["x", "y", "z"] | true
        "a and x"               | ["x", "y", "z"] | false
        "y and x"               | ["x", "y", "z"] | true
        "(a or x) and (y or b)" | ["x", "y", "z"] | true
        "(a, x) and (y, b)"     | ["x", "y", "z"] | true
        "(a or x) and (c or d)" | ["x", "y", "z"] | false
        "(a, x) and (c, d)"     | ["x", "y", "z"] | false
        ""                      | []              | true
        ""                      | ["x"]           | false
    }
}
