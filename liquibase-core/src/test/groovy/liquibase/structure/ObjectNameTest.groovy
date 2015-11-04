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
        objectName                                         | expected
        new ObjectName()                                   | "#UNSET"
        new ObjectName(null)                               | "#UNSET"
        new ObjectName("a")                                | "a"
        new ObjectName("a", "b")                           | "a.b"
        new ObjectName("a", "b", "c")                      | "a.b.c"
        new ObjectName(new ObjectName("a", "b"))           | "a.b.#UNSET"
        new ObjectName(new ObjectName("a", "b"), "c")      | "a.b.c"
        new ObjectName(new ObjectName("a", "b"), "c", "d") | "a.b.c.d"
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
        new ObjectName()                   | "#UNSET"
        new ObjectName("abc")              | "abc"
        new ObjectName("ABC")              | "ABC"
        new ObjectName("ABC", "xyz")       | "ABC.xyz"
        new ObjectName("ABC", "XYZ", null) | "ABC.XYZ.#UNSET"
        new ObjectName(null, "XYZ")        | "XYZ"
        new ObjectName("a", "b", "c")      | "a.b.c"
        new ObjectName("a", null, "c")     | "a.#UNSET.c"
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

    @Unroll("#featureName: #name1 vs #name2")
    def "matches"() {
        expect:
        name1.matches(name2) == expected

        where:
        name1                                                      | name2                                         | expected
        new ObjectName("a")                                        | null                                          | true
        new ObjectName("a")                                        | new ObjectName()                              | true
        new ObjectName("a")                                        | new ObjectName(null)                          | true
        new ObjectName("a")                                        | new ObjectName("a")                           | true
        new ObjectName("a")                                        | new ObjectName("b")                           | false
        new ObjectName(null, "a")                                  | new ObjectName("a")                           | true
        new ObjectName("a")                                        | new ObjectName(null, "a")                     | true
        new ObjectName("a")                                        | new ObjectName(null, "b")                     | false
        new ObjectName("a", null)                                  | new ObjectName("a", "b")                      | true
        new ObjectName("a", null, "c")                             | new ObjectName("a", "b", "c")                 | true
        new ObjectName("a", null, "c")                             | new ObjectName("a", "b", "c")                 | true
        new ObjectName(null, "a")                                  | new ObjectName(null, "a")                     | true
        new ObjectName("LIQUIBASE", "PUBLIC", "TABLE1", "COLUMN1") | new ObjectName("PUBLIC", "TABLE1", "COLUMN1") | true;
        new ObjectName("PUBLIC", "TABLE1", "COLUMN1") | new ObjectName("LIQUIBASE", "PUBLIC", "TABLE1", "COLUMN1") | true;

    }

    @Unroll
    def "truncate"() {
        expect:
        name.truncate(length).toString() == expected

        where:
        name                          | length | expected
        new ObjectName()              | 1      | "#UNSET"
        new ObjectName()              | 2      | "#UNSET"
        new ObjectName("a")           | 1      | "a"
        new ObjectName("a")           | 2      | "a"
        new ObjectName("a")           | 3      | "a"
        new ObjectName("a", "b")      | 1      | "b"
        new ObjectName("a", "b")      | 2      | "a.b"
        new ObjectName("a", "b")      | 3      | "a.b"
        new ObjectName("a", "b", "c") | 1      | "c"
        new ObjectName("a", "b", "c") | 2      | "b.c"
        new ObjectName("a", "b", "c") | 3      | "a.b.c"


    }
}

