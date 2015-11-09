package liquibase.exception

import liquibase.action.AbstractAction
import liquibase.structure.ObjectReference
import spock.lang.Specification
import spock.lang.Unroll

class ValidationErrorsTest extends Specification {

    @Unroll("#featureName #field")
    def "checkForRequiredContainer"() {
        when:
        def action = new AbstractAction() {}
        action.set(field, objectName)

        then:
        new ValidationErrors().checkForRequiredContainer("a message", field, action).hasErrors() == expectError

        where:
        field            | objectName                     | expectError
        "noContainer"    | new ObjectReference("a")            | true
        "oneContainer"   | new ObjectReference("a", "b")       | false
        "twoContainer"   | new ObjectReference("a", "b", "c")  | false
        "splitContainer" | new ObjectReference("a", null, "c") | true
        "nullContainer"  | new ObjectReference(null, "c")      | true
        "emptyObject"    | new ObjectReference()               | true
        "nullObject"     | new ObjectReference(null)           | true
        "null"           | null                           | false

    }
}
