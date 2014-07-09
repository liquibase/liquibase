package liquibase.action.core

import spock.lang.Specification
import spock.lang.Unroll

class SingleLineCommentTest extends Specification {

    @Unroll("#featureName: #expected")
    def "describe"() {
        when:
        def action = new SingleLineComment(message, lineComment)
        then:
        action.describe() == expected

        where:
        message | lineComment | expected
        "hi there" | null | "hi there"
        "hi there" | "--" | "-- hi there"
        null | "#" | "#"
    }
}
