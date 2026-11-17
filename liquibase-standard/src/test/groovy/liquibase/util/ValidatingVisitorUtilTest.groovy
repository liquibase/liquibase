package liquibase.util

import spock.lang.Specification
import spock.lang.Unroll

class ValidatingVisitorUtilTest extends Specification {

    @Unroll
    def "checkLiquibaseVersionIs(#version, 4, 21) is #expected"() {
        expect:
        ValidatingVisitorUtil.checkLiquibaseVersionIs(version, 4, 21) == expected

        where:
        version  | expected
        null     | false
        ""       | false
        "4.21.0" | true
        "4.21.1" | true
        "4.22.0" | false
        "4.21"   | false
        "a.b.c"  | false
    }
}
