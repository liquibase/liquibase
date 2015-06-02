package liquibase.exception

import liquibase.action.AbstractAction
import liquibase.structure.ObjectName
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
        "noContainer"    | new ObjectName("a")            | true
        "oneContainer"   | new ObjectName("a", "b")       | false
        "twoContainer"   | new ObjectName("a", "b", "c")  | false
        "splitContainer" | new ObjectName("a", null, "c") | true
        "nullContainer"  | new ObjectName(null, "c")      | true
        "emptyObject"    | new ObjectName()               | true
        "nullObject"     | new ObjectName(null)           | true
        "null"           | null                           | false

    }
}
