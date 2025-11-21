package liquibase.exception

import liquibase.change.core.LoadDataChange
import spock.lang.Specification

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertTrue

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
        null            | "change"             | "'field' is required for 'change'"
        ""              | "change"             | "'field' is empty for 'change'"
        " "             | "change"             | "'field' is empty for 'change'"
        new ArrayList() | "change"             | "No 'field' defined for 'change'"
        []              | new LoadDataChange() | "No 'field' defined for 'loadData'"
        " "             | new LoadDataChange() | "'field' is empty for 'loadData'"
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
        null            | "'field' is required"
        ""              | "'field' is empty"
        " "             | "'field' is empty"
        []              | "No 'field' defined"
        new ArrayList() | "No 'field' defined"
    }

    def "ValidationErrors.checkRequiredField no empty no change"() {
        when:
        ValidationErrors errors = new ValidationErrors()
        errors.checkRequiredField("field", ["a"])
        errors.checkRequiredField("field", "a")

        then:
        errors.getErrorMessages().size() == 0
    }

    def checkRequiredField_nullValue() {
        when:
        ValidationErrors errors = new ValidationErrors();
       then:
		 !errors.hasErrors()

        and:
        errors.checkRequiredField("testField", null);
        then:
        errors.hasErrors()
        errors.getErrorMessages().contains("'testField' is required")
    }

    def hasErrors() {
        when:
        ValidationErrors errors = new ValidationErrors()
        errors.addError("test message")
        then:
        errors.hasErrors()
    }
}
