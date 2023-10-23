package liquibase.util

import liquibase.exception.LiquibaseException
import liquibase.exception.UnexpectedLiquibaseException
import spock.lang.Specification

class ValidateTest extends Specification {

    def "notNull"() {
        when:
        Validate.notNull("x", "msg")
        then:
        notThrown(LiquibaseException)

        when:
        Validate.notNull(null, "msg")
        then:
        def e = thrown(UnexpectedLiquibaseException)
        e.message == "msg"
    }

    def "isTrue"() {
        when:
        Validate.isTrue(true, "msg")
        then:
        notThrown(LiquibaseException)

        when:
        Validate.isTrue(false, "msg")
        then:
        def e = thrown(UnexpectedLiquibaseException)
        e.message == "msg"
    }

    def "fail"() {
        when:
        Validate.fail("msg")

        then:
        def e = thrown(UnexpectedLiquibaseException)
        e.message == "msg"
    }
}
