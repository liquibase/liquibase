package liquibase.structure

import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Array

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
        objectName.asList() == expected

        where:
        objectName                                 | expected
        new ObjectName()                           | []
        new ObjectName("a")                        | ["a"]
        new ObjectName("a", "b")                   | ["a", "b"]
        new ObjectName("a", "b", "c")              | ["a", "b", "c"]
        new ObjectName(null, "b", "c")             | ["b", "c"]
        new ObjectName("a", null, "c")             | ["a", null, "c"]
        new ObjectName("a", "b", null)             | ["a", "b", null]
        new ObjectName("a", null, "c")             | ["a", null, "c"]
        new ObjectName(null, null, "a", "b")       | ["a", "b"]  //top level nulls do not count toward name
        new ObjectName(null, "a", null, null, "c") | ["a", null, null, "c"]

    }

    @Unroll("#featureName: #expected")
    def "toString works"() {
        expect:
        objectName.toString() == expected

        where:
        objectName                         | expected
        new ObjectName()                   | "#DEFAULT"
        new ObjectName("abc")              | "abc"
        new ObjectName("ABC")              | "ABC"
        new ObjectName("ABC", "xyz")       | "ABC.xyz"
        new ObjectName("ABC", "XYZ", null) | "ABC.XYZ.#DEFAULT"
        new ObjectName(null, "XYZ")        | "XYZ"
        new ObjectName("a", "b", "c")      | "a.b.c"
        new ObjectName("a", null, "c")     | "a.#DEFAULT.c"
    }

    @Unroll
    def depth() {
        expect:
        objectName.depth() == expected

        where:
        objectName                                 | expected
        new ObjectName()                           | 0
        new ObjectName("a")                        | 0
        new ObjectName("a", "b")                   | 1
        new ObjectName("a", "b", "c")              | 2
        new ObjectName("a", null, "c")             | 2
        new ObjectName(null, "a", "b")             | 1 //top level nulls do not count toward depth
        new ObjectName(null, null, "a", "b")       | 1 //top level nulls do not count toward depth
        new ObjectName(null, "a", null, "c")       | 2
        new ObjectName(null, "a", null, null, "c") | 3
        new ObjectName("a", "b", "c", "d")         | 3
    }

    @Unroll
    def parse() {
        expect:
        ObjectName.parse(string).asList() == expected

        where:
        string  | expected
        null    | []
        "abc"   | ["abc"]
        "a.b"   | ["a", "b"]
        "a.b.c" | ["a", "b", "c"]
    }

    @Unroll
    def "asList with length"() {
        expect:
        objectName.asList(length) == expected

        where:
        objectName                     | length | expected
        new ObjectName()               | 2      | [null, null]
        new ObjectName("a")            | 2      | [null, "a"]
        new ObjectName("a", "b")       | 2      | ["a", "b"]
        new ObjectName("a", "b", "c")  | 2      | ["b", "c"]
        new ObjectName("a", null, "c") | 3      | ["a", null, "c"]
        new ObjectName()               | 0      | []
        new ObjectName("a")            | 0      | []
        new ObjectName()               | 1      | [null]
        new ObjectName("a")            | 1      | ["a"]
        new ObjectName("a", "b")       | 1      | ["b"]
    }

    @Unroll
    def "equals ignoring length differences"() {
        expect:
        name1.equals(name2, ignore) == expected

        where:
        name1                               | name2                               | ignore | expected
        new ObjectName("a")                 | new ObjectName("a")                 | true   | true
        new ObjectName("a")                 | new ObjectName("a")                 | false  | true
        new ObjectName("a")                 | new ObjectName("b")                 | true   | false
        new ObjectName("a")                 | new ObjectName("b")                 | false  | false
        new ObjectName("a")                 | new ObjectName("b", "a")            | true   | true
        new ObjectName("a")                 | new ObjectName("b", "a")            | false  | false
        new ObjectName("b", "a")            | new ObjectName("a")                 | true   | true
        new ObjectName("b", "a")            | new ObjectName("a")                 | false  | false
        new ObjectName("b", "a")            | new ObjectName("b", "a")            | true   | true
        new ObjectName("b", "a")            | new ObjectName("b", "a")            | false  | true
        new ObjectName("c", "b", "a")       | new ObjectName("c", "b", "a")       | true   | true
        new ObjectName("c", "b", "a")       | new ObjectName("c", "b", "a")       | false  | true
        new ObjectName("c", "x", "a")       | new ObjectName("c", "b", "a")       | true   | false
        new ObjectName("c", "b", "a")       | new ObjectName("c", "x", "a")       | true   | false
        new ObjectName("c", null, "a")      | new ObjectName("c", "b", "a")       | true   | false
        new ObjectName("c", "b", "a")       | new ObjectName("c", null, "a")      | true   | false
        new ObjectName(null, "c", "b", "a") | new ObjectName("c", "b", "a")       | true   | true
        new ObjectName("c", "b", "a")       | new ObjectName(null, "c", "b", "a") | true   | true
        new ObjectName(null, "c", "b", "a") | new ObjectName("c", "b", "a")       | false  | true

    }
}

