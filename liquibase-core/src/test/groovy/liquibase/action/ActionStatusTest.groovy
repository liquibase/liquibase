package liquibase.action

import liquibase.AbstractExtensibleObject
import spock.lang.Specification
import spock.lang.Unroll

class ActionStatusTest extends Specification {

    @Unroll
    def "status logic works correctly"() {
        expect:
        status.toString() == expected

        where:
        status                                                                                         | expected
        new ActionStatus()                                                                             | "Unknown"
        new ActionStatus().assertCorrect(true, "Don't show")                                           | "Applied"
        new ActionStatus().assertApplied(true, "Don't show")                                           | "Applied"
        new ActionStatus().assertApplied(true, "Don't show")                                           | "Applied"
        new ActionStatus().assertApplied(false, "Applied Error")                                       | "Not Applied: Applied Error"
        new ActionStatus().unknown("An error")                                                         | "Unknown: An error"
        new ActionStatus().unknown(new RuntimeException("Exception message"))                          | "Unknown: Exception message (java.lang.RuntimeException)"
        new ActionStatus().cannotVerify("Too tired")                                                   | "Cannot Verify: Too tired"
        new ActionStatus().assertApplied(false, "Applied Error").assertApplied(false, "Another error") | "Not Applied: Another error, Applied Error"
        new ActionStatus().assertCorrect(false, "Correct Error")                                       | "Incorrect: Correct Error"
        new ActionStatus().assertCorrect(false, "Correct Error").assertApplied(false, "Applied Error") | "Not Applied: Applied Error"
        new ActionStatus().assertCorrect(false, "Correct Error").assertApplied(false, "Applied Error") | "Not Applied: Applied Error"
        new ActionStatus().unknown("Something wrong").assertApplied(false, "Applied Error")            | "Unknown: Something wrong"
        new ActionStatus().unknown("Something wrong").cannotVerify("Verify error")                     | "Cannot Verify: Verify error"
    }

    @Unroll
    def "assertCorrect using parameterName"() {
        when:
        def object1 = new AbstractExtensibleObject()
        def object2 = new AbstractExtensibleObject()
        object1.set(propertyName, object1Value)
        object2.set(propertyName, object2Value)

        then:
        new ActionStatus().assertCorrect(object1, object2, propertyName).toString() == expected

        where:
        propertyName | object1Value | object2Value | expected
        "prop"       | "a"          | "a"          | "Applied"
        "prop"       | "a"          | "b"          | "Incorrect: 'prop' is incorrect ('a' vs 'b')"
        "prop"       | "a"          | null         | "Incorrect: 'prop' is incorrect ('a' vs 'null')"
        "prop"       | null         | "a"          | "Applied"
        "prop"       | null         | null         | "Applied"

    }
}
