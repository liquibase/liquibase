package liquibase.precondition.core

import spock.lang.Specification

class ChangeSetExecutedPreconditionTest extends Specification {

    def "validate reports an error when changeLogFile is missing"() {
        given:
        def precondition = new ChangeSetExecutedPrecondition()

        when:
        def errors = precondition.validate(null)

        then:
        errors.hasErrors()
        errors.getErrorMessages().any {
            it.contains("changeLogFile") && it.contains("required")
        }
    }

    def "validate reports an error when changeLogFile is empty"() {
        given:
        def precondition = new ChangeSetExecutedPrecondition()
        precondition.setChangeLogFile("   ")

        when:
        def errors = precondition.validate(null)

        then:
        errors.hasErrors()
        errors.getErrorMessages().any {
            it.contains("changeLogFile") && it.contains("empty")
        }
    }

    def "validate succeeds when changeLogFile is provided"() {
        given:
        def precondition = new ChangeSetExecutedPrecondition()
        precondition.setChangeLogFile("db/changelog.xml")

        when:
        def errors = precondition.validate(null)

        then:
        !errors.hasErrors()
    }
}
