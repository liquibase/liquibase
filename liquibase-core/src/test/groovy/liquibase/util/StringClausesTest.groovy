package liquibase.util

import spock.lang.Specification

class StringClausesTest extends Specification {

    def "isEmpty"() {
        expect:
        clause.isEmpty() == expected

        where:
        clause                                             | expected
        new StringClauses()                                | true
        new StringClauses().append("")                     | true
        new StringClauses().append("").append("something") | false
        new StringClauses().append("").append("")          | true
    }
}
