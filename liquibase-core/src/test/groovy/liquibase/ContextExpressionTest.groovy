package liquibase

import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.*

class ContextExpressionTest extends Specification {

    @Unroll("featureName: '#string'")
    def "string ContextExpression constructor splits on commas correctly"() {
        expect:
        def expression = new ContextExpression(string).getContexts()
        expression containsInAnyOrder(expected.toArray())

        where:
        string    | expected
        ""        | []
        "   "     | []
        "a,b"     | ["a", "b"]
        "a, b"    | ["a", "b"]
        " a , b " | ["a", "b"]
    }

    @Unroll("#featureName. context: #testContexts")
    def "contexts running in no passed context always match"() {
        expect:
        def contexts = new ContextExpression(testContexts)
        assert contexts.matches(null)
        assert contexts.matches(new Contexts([]))

        where:
        testContexts << [null, [], ["test"], ["test1", "test2"]]
    }

    def "Empty contexts with no set values always match current context"() {
        expect:
        def contexts = new ContextExpression()
        assert contexts.matches(null)
        assert contexts.matches(new Contexts([]))
        assert contexts.matches(new Contexts(["test"]))
        assert contexts.matches(new Contexts(["test", "prod"]))
    }

    @Unroll("#featureName: testContexts #testContexts currentContexts: #currentContexts")
    def "simple contexts run when at least one current context matches"() {
        expect:
        assert new ContextExpression(testContexts).matches(new Contexts(currentContexts)) == expectedResult

        where:
        testContexts   | currentContexts  | expectedResult
        "test"         | "test"           | true
        "test"         | "TEST"           | true
        "TEST"         | "test"           | true
        "test"         | "prod"           | false
        "prod"         | "test"           | false
        "test, aug"    | "test"           | true
        "test, aug"    | "aug"            | true
        "test, AUG"    | "Aug"            | true
        "test, aug"    | "sept"           | false
        "test, aug"    | "aug, test"      | true
        "test, aug"    | "jan, feb, test" | true
        "test context" | "test context"   | true
        "test context" | "test"           | false
        "test"         | "test context"   | false
    }

    @Unroll("#featureName: testContexts #testContexts currentContexts: #currentContexts")
    def "'not' contexts"() {
        expect:
        assert new ContextExpression(testContexts).matches(new Contexts(currentContexts)) == expectedResult

        where:
        testContexts | currentContexts | expectedResult
        "!test"      | "test"          | false
        "!test"      | "TEST"          | false
        "!TEST"      | "test"          | false
        "!test"      | "prod"          | true
        "!test"      | ""              | true
        "!test"      | null            | true
    }

    @Unroll("#featureName: testContexts #testContexts currentContexts: #currentContexts")
    def "'and' contexts"() {
        expect:
        assert new ContextExpression(testContexts).matches(new Contexts(currentContexts)) == expectedResult

        where:
        testContexts      | currentContexts | expectedResult
        "a and b"         | "a"             | false
        "a and b"         | "b"             | false
        "a and b"         | "a,b"           | true
        "a and b"         | "a,b,c"         | true
        "a and b"         | "a,c"           | false
        "a and b"         | "c"             | false
        "b and a"         | "a,b"           | true
        "a AND b"         | "a"             | false
        "a AND b"         | "a,b"           | true
        "a and !b"        | "a"             | true
        "a and !b"        | "a,c"           | true
        "a and !b"        | "a,b"           | false
        "a and !b"        | "b"             | false
        "  a   and   b  " | "a"             | false
        "  a   and   b  " | "b"             | false
        "  a   and   b  " | "a,b"           | true
        "x y and t u"     | "x y"           | false
        "x y and t u"     | "t u"           | false
        "x y and t u"     | "x y, t u"      | true
    }

    @Unroll("#featureName: testContexts #testContexts currentContexts: #currentContexts")
    def "'or' contexts"() {
        expect:
        assert new ContextExpression(testContexts).matches(new Contexts(currentContexts)) == expectedResult

        where:
        testContexts    | currentContexts | expectedResult
        "a or b"        | "a"             | true
        "a or b"        | "b"             | true
        "a or b"        | "a,b"           | true
        "a or b"        | "a,b,c"         | true
        "a or b"        | "c"             | false
        "a or b"        | "c,d"           | false
        "a OR b"        | "a"             | true
        "a OR b"        | "c"             | false
        "b or a"        | "a,b"           | true
        "a or !b"       | "a"             | true
        "a or !b"       | "a,c"           | true
        "a or !b"       | "a,b"           | true
        "a or !b"       | "b"             | false
        "a    or   b  " | "a"             | true
        " a   or   b  " | "b"             | true
        " a   or   b  " | "c"             | false
        "x y or 1 2"    | "x y"           | true
        "x y or 1 2"    | "1 2"           | true
        "x y or 1 2"    | "a b"           | false
        "x y or 1 2"    | "x 1"           | false
    }

    @Unroll("#featureName: testContexts #testContexts currentContexts: #currentContexts")
    def "complex contexts"() {
        expect:
        assert new ContextExpression(testContexts).matches(new Contexts(currentContexts)) == expectedResult

        where:
        testContexts      | currentContexts | expectedResult
        "a and b or c"    | "a"             | false
        "a and b or c"    | "a,b"           | true
        "a and b or c"    | "c"             | true
        "a and b or c"    | "a,c"           | true
        "a and (b or c)"  | "a"             | false
        "a and (b or c)"  | "b"             | false
        "a and (b or c)"  | "c"             | false
        "a and (b or c)"  | "a,b"           | true
        "a and (b or c)"  | "a,c"           | true
        "a and (b or c)"  | "a,b,c"         | true
        "a or b and c"    | "a"             | true
        "a or b and c"    | "b"             | false
        "a or b and c"    | "b,c"           | true
        "a or b and c"    | "a,c"           | true
        "a or b and c"    | "a,b,c"         | true
        "!a and b or c"   | "a"             | false
        "!a and b or c"   | "b,d"           | true
        "!a and b or c"   | "d"             | false
        "a and !b or c"   | "a,b"           | false
        "a and !b or c"   | "a,d"           | true
        "a and !b or c"   | "c"             | true
        "a and b or c, d" | "a,b"           | true
        "a and b or c, d" | "d"             | true
        "a and b or c, d" | "e"             | false
    }

    @Unroll
    def isEmpty() {
        expect:
        assert new ContextExpression(expression).isEmpty() == expected

        where:
        expression     | expected
        null           | true
        ""             | true
        "test"         | false
        "test1, test2" | false

    }
}
