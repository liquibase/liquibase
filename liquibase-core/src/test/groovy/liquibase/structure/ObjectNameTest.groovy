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

    @Unroll("#featureName: #expected")
    def "getNameList"() {
        expect:
        objectName.getNameList() == expected

        where:
        objectName                     | expected
        new ObjectName()               | [null]
        new ObjectName("a")            | ["a"]
        new ObjectName("a", "b")       | ["a", "b"]
        new ObjectName("a", "b", "c")  | ["a", "b", "c"]
        new ObjectName(null, "b", "c") | [null, "b", "c"]
        new ObjectName("a", null, "c") | ["a", null, "c"]
        new ObjectName("a", "b", null) | ["a", "b", null]
    }

    @Unroll("#featureName: #expected")
    def "toString works"() {
        expect: objectName.toString() == expected

        where:
        objectName | expected
        new ObjectName() | "#DEFAULT"
        new ObjectName("abc") | "abc"
        new ObjectName("ABC") | "ABC"
        new ObjectName("ABC", "xyz") | "ABC.xyz"
        new ObjectName("ABC", "XYZ", null) | "ABC.XYZ.#DEFAULT"
        new ObjectName(null, "XYZ") | "#DEFAULT.XYZ"
        new ObjectName("a", "b", "c") | "a.b.c"
        new ObjectName("a", null, "c") | "a.#DEFAULT.c"
    }
}

