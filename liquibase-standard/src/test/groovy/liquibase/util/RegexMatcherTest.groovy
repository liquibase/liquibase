package liquibase.util

import spock.lang.Specification

class RegexMatcherTest extends Specification {

    def testBadPatternFails() {
        when:
        new RegexMatcher("", ["a(j"] as String[])

        then:
        def e = thrown(Throwable)
        e.message.startsWith("Unclosed group near index 3")
    }

    def matchingInSequentialOrder() {
        when:
        def matcher = new RegexMatcher("Pulp Fiction\n" +
                "Reservoir Dogs\n" +
                "Kill Bill\n", patterns as String[])

        then:
        matcher.allMatchedInSequentialOrder() == matches

        where:
        patterns                      | matches
        ["Pulp", "Reservoir", "Kill"] | true
        ["Pulp", "ion"]               | true
        ["Pu.p", "^Ki.+ll\$"]         | true
        ["pulP", "kiLL"]              | true //case insensitive
        ["Reservoir", "Pulp", "Dogs"] | false //out of order
        ["Memento"]                   | false
    }

}
