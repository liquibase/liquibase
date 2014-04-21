package liquibase

import spock.lang.Specification
import spock.lang.Unroll

class ContextExpressionTest extends Specification {

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
        def contexts = new ContextExpression(testContexts)

        assert contexts.matches(new Contexts(currentContexts)) == expectedResult

        where:
        testContexts | currentContexts | expectedResult
        ["test"]        | ["test"]               | true
        ["test"]        | ["TEST"]               | true
        ["TEST"]        | ["test"]               | true
        ["test"]        | ["prod"]               | false
        ["prod"]        | ["test"]               | false
        ["test", "aug"] | ["test"]               | true
        ["test", "aug"] | ["aug"]                | true
        ["test", "AUG"] | ["Aug"]                | true
        ["test", "aug"] | ["sept"]               | false
        ["test", "aug"] | ["aug", "test"]        | true
        ["test", "aug"] | ["jan", "feb", "test"] | true
    }

    @Unroll
    def isEmpty() {
        expect:
        assert new ContextExpression(expression).isEmpty() == expected

        where:
        expression | expected
        null           | true
        ""             | true
        "test"         | false
        "test1, test2" | false

    }
}
