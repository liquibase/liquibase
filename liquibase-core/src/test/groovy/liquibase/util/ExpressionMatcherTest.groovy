package liquibase.util

import spock.lang.Specification
import spock.lang.Unroll

class ExpressionMatcherTest extends Specification {

    @Unroll
    def match() {
        expect:
        ExpressionMatcher.matches(expression, items) == expected

        where:
        expression                  | items             | expected
        // Cover all cases not including required expression operator ("@")
        ""                          | ["x"]             | false
        "x"                         | ["x"]             | true
        "y"                         | ["x", "y", "z"]   | true
        "Y"                         | ["x", "y", "z"]   | true
        "!y"                        | ["x", "y", "z"]   | false
        "not y"                     | ["x", "y", "z"]   | false
        "!a"                        | ["x", "y", "z"]   | true
        "a or x"                    | ["x", "y", "z"]   | true
        "b or not a"                | ["x", "y", "z"]   | true
        "a and x"                   | ["x", "y", "z"]   | false
        "y and x"                   | ["x", "y", "z"]   | true
        "(a or x) and (y or b)"     | ["x", "y", "z"]   | true
        "(a, x) and (y, b)"         | ["x", "y", "z"]   | true
        "(a or x) and (c or d)"     | ["x", "y", "z"]   | false
        "(a, x) and (c, d)"         | ["x", "y", "z"]   | false
        ""                          | []                | true
        "x"                         | []                | true
        "y"                         | []                | true
        "Y"                         | []                | true
        "!y"                        | []                | true
        "not y"                     | []                | true
        "!a"                        | []                | true
        "a or x"                    | []                | true
        "b or not a"                | []                | true
        "a and x"                   | []                | true
        "y and x"                   | []                | true
        "(a or x) and (y or b)"     | []                | true
        "(a, x) and (y, b)"         | []                | true
        "(a or x) and (c or d)"     | []                | true
        "(a, x) and (c, d)"         | []                | true
        // Cover all cases including required expression operator ("@")
        // where items are present
        "@x"                        | ["x"]             | true
        "@y"                        | ["x", "y", "z"]   | true
        "@Y"                        | ["x", "y", "z"]   | true
        "!@y"                       | ["x", "y", "z"]   | false
        "not @y"                    | ["x", "y", "z"]   | false
        "@not y"                    | ["x", "y", "z"]   | false
        "@not @y"                   | ["x", "y", "z"]   | false
        "@!y"                       | ["x", "y", "z"]   | false
        "@not y"                    | ["x", "y", "z"]   | false
        "!@a"                       | ["x", "y", "z"]   | true
        "@!a"                       | ["x", "y", "z"]   | true
        "@a or x"                   | ["x", "y", "z"]   | true
        "a or @x"                   | ["x", "y", "z"]   | true
        "@a or @x"                  | ["x", "y", "z"]   | true
        "@b or not a"               | ["x", "y", "z"]   | true
        "b or @not a"               | ["x", "y", "z"]   | true
        "b or not @a"               | ["x", "y", "z"]   | true
        "@b or @not a"              | ["x", "y", "z"]   | true
        "@b or not @a"              | ["x", "y", "z"]   | true
        "b or @not @a"              | ["x", "y", "z"]   | true
        "@b or @not @a"             | ["x", "y", "z"]   | true
        "@a and x"                  | ["x", "y", "z"]   | false
        "a and @x"                  | ["x", "y", "z"]   | false
        "@a and @x"                 | ["x", "y", "z"]   | false
        "@y and x"                  | ["x", "y", "z"]   | true
        "y and @x"                  | ["x", "y", "z"]   | true
        "@y and @x"                 | ["x", "y", "z"]   | true
        "(@a or x) and (y or b)"    | ["x", "y", "z"]   | true
        "(a or @x) and (y or b)"    | ["x", "y", "z"]   | true
        "(a or x) and (@y or b)"    | ["x", "y", "z"]   | true
        "(a or x) and (y or @b)"    | ["x", "y", "z"]   | true
        "(@a or @x) and (y or b)"   | ["x", "y", "z"]   | true
        "(@a or x) and (@y or b)"   | ["x", "y", "z"]   | true
        "(@a or x) and (y or @b)"   | ["x", "y", "z"]   | true
        "(a or @x) and (@y or b)"   | ["x", "y", "z"]   | true
        "(a or @x) and (y or @b)"   | ["x", "y", "z"]   | true
        "(a or x) and (@y or @b)"   | ["x", "y", "z"]   | true
        "(@a or @x) and (@y or b)"  | ["x", "y", "z"]   | true
        "(@a or @x) and (y or @b)"  | ["x", "y", "z"]   | true
        "(a or @x) and (@y or @b)"  | ["x", "y", "z"]   | true
        "(@a or @x) and (@y or @b)" | ["x", "y", "z"]   | true
        "(@a, x) and (y, b)"        | ["x", "y", "z"]   | true
        "(a, @x) and (y, b)"        | ["x", "y", "z"]   | true
        "(a, x) and (@y, b)"        | ["x", "y", "z"]   | true
        "(a, x) and (y, @b)"        | ["x", "y", "z"]   | true
        "(@a, @x) and (y, b)"       | ["x", "y", "z"]   | true
        "(@a, x) and (@y, b)"       | ["x", "y", "z"]   | true
        "(@a, x) and (y, @b)"       | ["x", "y", "z"]   | true
        "(a, @x) and (@y, b)"       | ["x", "y", "z"]   | true
        "(a, @x) and (y, @b)"       | ["x", "y", "z"]   | true
        "(a, x) and (@y, @b)"       | ["x", "y", "z"]   | true
        "(@a, @x) and (@y, b)"      | ["x", "y", "z"]   | true
        "(@a, @x) and (y, @b)"      | ["x", "y", "z"]   | true
        "(a, @x) and (@y, @b)"      | ["x", "y", "z"]   | true
        "(@a, @x) and (@y, @b)"     | ["x", "y", "z"]   | true
        "(@a or x) and (c or d)"    | ["x", "y", "z"]   | false
        "(a or @x) and (c or d)"    | ["x", "y", "z"]   | false
        "(a or x) and (@c or d)"    | ["x", "y", "z"]   | false
        "(a or x) and (c or @d)"    | ["x", "y", "z"]   | false
        "(@a or @x) and (c or d)"   | ["x", "y", "z"]   | false
        "(@a or x) and (@c or d)"   | ["x", "y", "z"]   | false
        "(@a or x) and (c or @d)"   | ["x", "y", "z"]   | false
        "(a or @x) and (@c or d)"   | ["x", "y", "z"]   | false
        "(a or @x) and (c or @d)"   | ["x", "y", "z"]   | false
        "(a or x) and (@c or @d)"   | ["x", "y", "z"]   | false
        "(@a or @x) and (@c or d)"  | ["x", "y", "z"]   | false
        "(@a or @x) and (c or @d)"  | ["x", "y", "z"]   | false
        "(a or @x) and (@c or @d)"  | ["x", "y", "z"]   | false
        "(@a or @x) and (@c or @d)" | ["x", "y", "z"]   | false
        "(@a, x) and (c, d)"        | ["x", "y", "z"]   | false
        "(a, @x) and (c, d)"        | ["x", "y", "z"]   | false
        "(a, x) and (@c, d)"        | ["x", "y", "z"]   | false
        "(a, x) and (c, @d)"        | ["x", "y", "z"]   | false
        "(@a, @x) and (c, d)"       | ["x", "y", "z"]   | false
        "(@a, x) and (@c, d)"       | ["x", "y", "z"]   | false
        "(@a, x) and (c, @d)"       | ["x", "y", "z"]   | false
        "(a, @x) and (@c, d)"       | ["x", "y", "z"]   | false
        "(a, @x) and (c, @d)"       | ["x", "y", "z"]   | false
        "(a, x) and (@c, @d)"       | ["x", "y", "z"]   | false
        "(@a, @x) and (@c, d)"      | ["x", "y", "z"]   | false
        "(@a, @x) and (c, @d)"      | ["x", "y", "z"]   | false
        "(a, @x) and (@c, @d)"      | ["x", "y", "z"]   | false
        "(@a, @x) and (@c, @d)"     | ["x", "y", "z"]   | false
        // Cover all cases including required expression operator ("@")
        // where items are not present
        "@x"                        | []                | false
        "@y"                        | []                | false
        "@Y"                        | []                | false
        "!@y"                       | []                | true
        "not @y"                    | []                | true
        "@not y"                    | []                | true
        "@not @y"                   | []                | true
        "@!y"                       | []                | true
        "@not y"                    | []                | true
        "!@a"                       | []                | true
        "@!a"                       | []                | true
        "@a or x"                   | []                | true
        "a or @x"                   | []                | true
        "@a or @x"                  | []                | false
        "@b or not a"               | []                | true
        "b or @not a"               | []                | true
        "b or not @a"               | []                | true
        "@b or @not a"              | []                | true
        "@b or not @a"              | []                | true
        "b or @not @a"              | []                | true
        "@b or @not @a"             | []                | true
        "@a and x"                  | []                | false
        "a and @x"                  | []                | false
        "@a and @x"                 | []                | false
        "@y and x"                  | []                | false
        "y and @x"                  | []                | false
        "@y and @x"                 | []                | false
        "(@a or x) and (y or b)"    | []                | true
        "(a or @x) and (y or b)"    | []                | true
        "(a or x) and (@y or b)"    | []                | true
        "(a or x) and (y or @b)"    | []                | true
        "(@a or @x) and (y or b)"   | []                | false
        "(@a or x) and (@y or b)"   | []                | true
        "(@a or x) and (y or @b)"   | []                | true
        "(a or @x) and (@y or b)"   | []                | true
        "(a or @x) and (y or @b)"   | []                | true
        "(a or x) and (@y or @b)"   | []                | false
        "(@a or @x) and (@y or b)"  | []                | false
        "(@a or @x) and (y or @b)"  | []                | false
        "(a or @x) and (@y or @b)"  | []                | false
        "(@a or @x) and (@y or @b)" | []                | false
        "(@a, x) and (y, b)"        | []                | true
        "(a, @x) and (y, b)"        | []                | true
        "(a, x) and (@y, b)"        | []                | true
        "(a, x) and (y, @b)"        | []                | true
        "(@a, @x) and (y, b)"       | []                | false
        "(@a, x) and (@y, b)"       | []                | true
        "(@a, x) and (y, @b)"       | []                | true
        "(a, @x) and (@y, b)"       | []                | true
        "(a, @x) and (y, @b)"       | []                | true
        "(a, x) and (@y, @b)"       | []                | false
        "(@a, @x) and (@y, b)"      | []                | false
        "(@a, @x) and (y, @b)"      | []                | false
        "(a, @x) and (@y, @b)"      | []                | false
        "(@a, @x) and (@y, @b)"     | []                | false
        "(@a or x) and (c or d)"    | []                | true
        "(a or @x) and (c or d)"    | []                | true
        "(a or x) and (@c or d)"    | []                | true
        "(a or x) and (c or @d)"    | []                | true
        "(@a or @x) and (c or d)"   | []                | false
        "(@a or x) and (@c or d)"   | []                | true
        "(@a or x) and (c or @d)"   | []                | true
        "(a or @x) and (@c or d)"   | []                | true
        "(a or @x) and (c or @d)"   | []                | true
        "(a or x) and (@c or @d)"   | []                | false
        "(@a or @x) and (@c or d)"  | []                | false
        "(@a or @x) and (c or @d)"  | []                | false
        "(a or @x) and (@c or @d)"  | []                | false
        "(@a or @x) and (@c or @d)" | []                | false
        "(@a, x) and (c, d)"        | []                | true
        "(a, @x) and (c, d)"        | []                | true
        "(a, x) and (@c, d)"        | []                | true
        "(a, x) and (c, @d)"        | []                | true
        "(@a, @x) and (c, d)"       | []                | false
        "(@a, x) and (@c, d)"       | []                | true
        "(@a, x) and (c, @d)"       | []                | true
        "(a, @x) and (@c, d)"       | []                | true
        "(a, @x) and (c, @d)"       | []                | true
        "(a, x) and (@c, @d)"       | []                | false
        "(@a, @x) and (@c, d)"      | []                | false
        "(@a, @x) and (c, @d)"      | []                | false
        "(a, @x) and (@c, @d)"      | []                | false
        "(@a, @x) and (@c, @d)"     | []                | false
    }

}
