package liquibase.exception

import liquibase.change.core.LoadDataChange
import spock.lang.Specification

class ValidationErrorsTests extends Specification {
    def "ValidationErrors.checkRequiredField empty"() {
        when:
        ValidationErrors errors = new ValidationErrors(change)
        errors.checkRequiredField("field", value)

        then:
        errors.getErrorMessages().size() == 1
        errors.getErrorMessages().get(0) == expected

        where:
        value           | change               | expected
        null            | "change"             | "field is required for change"
        ""              | "change"             | "field is empty for change"
        " "             | "change"             | "field is empty for change"
        new ArrayList() | "change"             | "No field defined for change"
        []              | new LoadDataChange() | "No field defined for loadData"
        " "             | new LoadDataChange() | "field is empty for loadData"
    }

    def "ValidationErrors.checkRequiredField empty no change"() {
        when:
        ValidationErrors errors = new ValidationErrors()
        errors.checkRequiredField("field", value)

        then:
        errors.getErrorMessages().size() == 1
        errors.getErrorMessages().get(0) == expected

        where:
        value           | expected
        null            | "field is required"
        ""              | "field is empty"
        " "             | "field is empty"
        []              | "No field defined"
        new ArrayList() | "No field defined"
    }

    def "ValidationErrors.checkRequiredField no empty no change"() {
        when:
        ValidationErrors errors = new ValidationErrors()
        errors.checkRequiredField("field", ["a"])
        errors.checkRequiredField("field", "a")

        then:
        errors.getErrorMessages().size() == 0
    }
}
