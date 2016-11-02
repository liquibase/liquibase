package liquibase

import spock.lang.Specification
import spock.lang.Unroll

import static org.hamcrest.Matchers.containsInAnyOrder

class LabelExpressionTest extends Specification {

    @Unroll("featureName: '#string'")
    def "string LabelExpression constructor splits on commas correctly"() {
        expect:
        def expression = new LabelExpression(string).getLabels()
        expression containsInAnyOrder(expected.toArray())

        where:
        string    | expected
        ""        | []
        "   "     | []
        "a,b"     | ["a", "b"]
        "a, b"    | ["a", "b"]
        " a , b " | ["a", "b"]
    }

    @Unroll("#featureName. label: #testLabels")
    def "labels running in no passed label always match"() {
        expect:
        def labels = new LabelExpression(testLabels)
        assert labels.matches(null)
        assert labels.matches(new Labels([]))

        where:
        testLabels << [null, [], ["test"], ["test1", "test2"]]
    }

    def "Empty labels with no set values always match current label"() {
        expect:
        def labels = new LabelExpression()
        assert labels.matches(null)
        assert labels.matches(new Labels([]))
        assert labels.matches(new Labels(["test"]))
        assert labels.matches(new Labels(["test", "prod"]))
    }

    @Unroll("#featureName: testLabels #testLabels currentLabels: #currentLabels")
    def "simple labels run when at least one current label matches"() {
        expect:
        assert new LabelExpression(testLabels).matches(new Labels(currentLabels)) == expectedResult

        where:
        testLabels   | currentLabels    | expectedResult
        "test"       | "test"           | true
        "test"       | "TEST"           | true
        "TEST"       | "test"           | true
        "test"       | "prod"           | false
        "prod"       | "test"           | false
        "test, aug"  | "test"           | true
        "test, aug"  | "aug"            | true
        "test, AUG"  | "Aug"            | true
        "test, aug"  | "sept"           | false
        "test, aug"  | "aug, test"      | true
        "test, aug"  | "jan, feb, test" | true
        "test label" | "test label"     | true
        "test label" | "test"           | false
        "test"       | "test label"     | false
    }

    @Unroll("#featureName: testLabels #testLabels currentLabels: #currentLabels")
    def "'not' labels"() {
        expect:
        assert new LabelExpression(testLabels).matches(new Labels(currentLabels)) == expectedResult

        where:
        testLabels | currentLabels | expectedResult
        "!test"    | "test"        | false
        "!test"    | "TEST"        | false
        "!TEST"    | "test"        | false
        "!test"    | "prod"        | true
        "!test"    | ""            | true
        "!test"    | null          | true
        "!test"    | "test,prod"   | false
        "!F"       | "A,C,F"       | false
        "NOT(F)"   | "A,C,F"       | false
        "NOT(F)"   | "A,C,X"       | true
    }

    @Unroll("#featureName: testLabels #testLabels currentLabels: #currentLabels")
    def "'and' labels"() {
        expect:
        assert new LabelExpression(testLabels).matches(new Labels(currentLabels)) == expectedResult

        where:
        testLabels        | currentLabels | expectedResult
        "a and b"         | "a"           | false
        "a and b"         | "b"           | false
        "a and b"         | "a,b"         | true
        "a and b"         | "a,b,c"       | true
        "a and b"         | "a,c"         | false
        "a and b"         | "c"           | false
        "b and a"         | "a,b"         | true
        "a AND b"         | "a"           | false
        "a AND b"         | "a,b"         | true
        "a and !b"        | "a"           | true
        "a and !b"        | "a,c"         | true
        "a and !b"        | "a,b"         | false
        "a and !b"        | "b"           | false
        "  a   and   b  " | "a"           | false
        "  a   and   b  " | "b"           | false
        "  a   and   b  " | "a,b"         | true
        "x y and t u"     | "x y"         | false
        "x y and t u"     | "t u"         | false
        "x y and t u"     | "x y, t u"    | true
    }

    @Unroll("#featureName: testLabels #testLabels currentLabels: #currentLabels")
    def "'or' labels"() {
        expect:
        assert new LabelExpression(testLabels).matches(new Labels(currentLabels)) == expectedResult

        where:
        testLabels      | currentLabels | expectedResult
        "a or b"        | "a"           | true
        "a or b"        | "b"           | true
        "a or b"        | "a,b"         | true
        "a or b"        | "a,b,c"       | true
        "a or b"        | "c"           | false
        "a or b"        | "c,d"         | false
        "a OR b"        | "a"           | true
        "a OR b"        | "c"           | false
        "b or a"        | "a,b"         | true
        "a or !b"       | "a"           | true
        "a or !b"       | "a,c"         | true
        "a or !b"       | "a,b"         | true
        "a or !b"       | "b"           | false
        "a    or   b  " | "a"           | true
        " a   or   b  " | "b"           | true
        " a   or   b  " | "c"           | false
        "x y or 1 2"    | "x y"         | true
        "x y or 1 2"    | "1 2"         | true
        "x y or 1 2"    | "a b"         | false
        "x y or 1 2"    | "x 1"         | false
    }

    @Unroll("#featureName: testLabels #testLabels currentLabels: #currentLabels")
    def "complex labels"() {
        expect:
        assert new LabelExpression(testLabels).matches(new Labels(currentLabels)) == expectedResult

        where:
        testLabels        | currentLabels | expectedResult
        "a and b or c"    | "a"           | false
        "a and b or c"    | "a,b"         | true
        "a and b or c"    | "c"           | true
        "a and b or c"    | "a,c"         | true
        "a and (b or c)"  | "a"           | false
        "a and (b or c)"  | "b"           | false
        "a and (b or c)"  | "c"           | false
        "a and (b or c)"  | "a,b"         | true
        "a and (b or c)"  | "a,c"         | true
        "a and (b or c)"  | "a,b,c"       | true
        "(a or b) and (c or d)"  | "a"           | false
        "(a or b) and (c or d)"  | "a,b"           | false
        "(a or b) and (c or d)"  | "a,b,c"           | true
        "((a or b) and (c or d))"  | "a,b,c"           | true
        "a or b and c"    | "a"           | true
        "a or b and c"    | "b"           | false
        "a or b and c"    | "b,c"         | true
        "a or b and c"    | "a,c"         | true
        "a or b and c"    | "a,b,c"       | true
        "!a and b or c"   | "a"           | false
        "!a and b or c"   | "b,d"         | true
        "!a and b or c"   | "d"           | false
        "a and !b or c"   | "a,b"         | false
        "a and !b or c"   | "a,d"         | true
        "a and !b or c"   | "c"           | true
        "a and b or c, d" | "a,b"         | true
        "a and b or c, d" | "d"           | true
        "a and b or c, d" | "e"           | false
    }

    @Unroll
    def isEmpty() {
        expect:
        assert new LabelExpression(expression).isEmpty() == expected

        where:
        expression     | expected
        null           | true
        ""             | true
        "test"         | false
        "test1, test2" | false

    }
}
