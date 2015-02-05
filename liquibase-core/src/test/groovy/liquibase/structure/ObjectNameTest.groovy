package liquibase.structure

import spock.lang.Specification
import spock.lang.Unroll

class ObjectNameTest extends Specification {

    @Unroll("#featureName: #expected")
    def "can construct with variable args"() {
        expect:
        objectName.toString() == expected

        where:
        objectName                    | expected
        new ObjectName()              | "#DEFAULT"
        new ObjectName(null)          | "#DEFAULT"
        new ObjectName("a")           | "a"
        new ObjectName("a", "b")      | "a.b"
        new ObjectName("a", "b", "c") | "a.b.c"
    }
}
