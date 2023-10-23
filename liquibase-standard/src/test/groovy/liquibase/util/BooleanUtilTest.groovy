package liquibase.util

import spock.lang.Specification
import spock.lang.Unroll

class BooleanUtilTest extends Specification {

    def "isTrue"() {
        expect:
        BooleanUtil.isTrue(input) == expected

        where:
        input         | expected
        Boolean.TRUE  | true
        Boolean.FALSE | false
        true          | true
        false         | false
    }

    @Unroll
    def "parseBoolean"() {
        expect:
        BooleanUtil.parseBoolean(input) == expected

        where:
        input              | expected
        "-1"               | false
        "0"                | false
        "1"                | true
        "2"                | true
        "true"             | true
        "True"             | true
        "TRUE"             | true
        "t"                | true
        "T"                | true
        "y"                | true
        "Y"                | true
        "yes"              | true
        "Yes"              | true
        "YES"              | true
        "false"            | false
        "False"            | false
        "FALSE"            | false
        "f"                | false
        "F"                | false
        "n"                | false
        "N"                | false
        "no"               | false
        "No"               | false
        "NO"               | false
        null               | false
        " any dummy text!" | false
    }
}
