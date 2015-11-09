package liquibase.structure

import spock.lang.Specification
import spock.lang.Unroll

class ObjectReferenceTest extends Specification {

    @Unroll("#featureName: #expected")
    def "can construct with variable args"() {
        expect:
        objectName.toString() == expected

        where:
        objectName                                         | expected
        new ObjectReference()                                   | "#UNSET"
        new ObjectReference(null)                               | "#UNSET"
        new ObjectReference("a")                                | "a"
        new ObjectReference("a", "b")                           | "a.b"
        new ObjectReference("a", "b", "c")                      | "a.b.c"
        new ObjectReference(new ObjectReference("a", "b"))           | "a.b.#UNSET"
        new ObjectReference(new ObjectReference("a", "b"), "c")      | "a.b.c"
        new ObjectReference(new ObjectReference("a", "b"), "c", "d") | "a.b.c.d"
    }

    @Unroll("#featureName: #expected")
    def "getNameList"() {
        expect:
        objectName.asList() == expected

        where:
        objectName                                 | expected
        new ObjectReference()                           | []
        new ObjectReference("a")                        | ["a"]
        new ObjectReference("a", "b")                   | ["a", "b"]
        new ObjectReference("a", "b", "c")              | ["a", "b", "c"]
        new ObjectReference(null, "b", "c")             | ["b", "c"]
        new ObjectReference("a", null, "c")             | ["a", null, "c"]
        new ObjectReference("a", "b", null)             | ["a", "b", null]
        new ObjectReference("a", null, "c")             | ["a", null, "c"]
        new ObjectReference(null, null, "a", "b")       | ["a", "b"]  //top level nulls do not count toward name
        new ObjectReference(null, "a", null, null, "c") | ["a", null, null, "c"]

    }

    @Unroll("#featureName: #expected")
    def "toString works"() {
        expect:
        objectName.toString() == expected

        where:
        objectName                         | expected
        new ObjectReference()                   | "#UNSET"
        new ObjectReference("abc")              | "abc"
        new ObjectReference("ABC")              | "ABC"
        new ObjectReference("ABC", "xyz")       | "ABC.xyz"
        new ObjectReference("ABC", "XYZ", null) | "ABC.XYZ.#UNSET"
        new ObjectReference(null, "XYZ")        | "XYZ"
        new ObjectReference("a", "b", "c")      | "a.b.c"
        new ObjectReference("a", null, "c")     | "a.#UNSET.c"
    }

    @Unroll
    def depth() {
        expect:
        objectName.depth() == expected

        where:
        objectName                                 | expected
        new ObjectReference()                           | 0
        new ObjectReference("a")                        | 0
        new ObjectReference("a", "b")                   | 1
        new ObjectReference("a", "b", "c")              | 2
        new ObjectReference("a", null, "c")             | 2
        new ObjectReference(null, "a", "b")             | 1 //top level nulls do not count toward depth
        new ObjectReference(null, null, "a", "b")       | 1 //top level nulls do not count toward depth
        new ObjectReference(null, "a", null, "c")       | 2
        new ObjectReference(null, "a", null, null, "c") | 3
        new ObjectReference("a", "b", "c", "d")         | 3
    }

    @Unroll
    def parse() {
        expect:
        ObjectReference.parse(string).asList() == expected

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
        new ObjectReference()               | 2      | [null, null]
        new ObjectReference("a")            | 2      | [null, "a"]
        new ObjectReference("a", "b")       | 2      | ["a", "b"]
        new ObjectReference("a", "b", "c")  | 2      | ["b", "c"]
        new ObjectReference("a", null, "c") | 3      | ["a", null, "c"]
        new ObjectReference()               | 0      | []
        new ObjectReference("a")            | 0      | []
        new ObjectReference()               | 1      | [null]
        new ObjectReference("a")            | 1      | ["a"]
        new ObjectReference("a", "b")       | 1      | ["b"]
    }

    @Unroll
    def "equals ignoring length differences"() {
        expect:
        name1.equals(name2, ignore) == expected

        where:
        name1                               | name2                               | ignore | expected
        new ObjectReference("a")                 | new ObjectReference("a")                 | true   | true
        new ObjectReference("a")                 | new ObjectReference("a")                 | false  | true
        new ObjectReference("a")                 | new ObjectReference("b")                 | true   | false
        new ObjectReference("a")                 | new ObjectReference("b")                 | false  | false
        new ObjectReference("a")                 | new ObjectReference("b", "a")            | true   | true
        new ObjectReference("a")                 | new ObjectReference("b", "a")            | false  | false
        new ObjectReference("b", "a")            | new ObjectReference("a")                 | true   | true
        new ObjectReference("b", "a")            | new ObjectReference("a")                 | false  | false
        new ObjectReference("b", "a")            | new ObjectReference("b", "a")            | true   | true
        new ObjectReference("b", "a")            | new ObjectReference("b", "a")            | false  | true
        new ObjectReference("c", "b", "a")       | new ObjectReference("c", "b", "a")       | true   | true
        new ObjectReference("c", "b", "a")       | new ObjectReference("c", "b", "a")       | false  | true
        new ObjectReference("c", "x", "a")       | new ObjectReference("c", "b", "a")       | true   | false
        new ObjectReference("c", "b", "a")       | new ObjectReference("c", "x", "a")       | true   | false
        new ObjectReference("c", null, "a")      | new ObjectReference("c", "b", "a")       | true   | false
        new ObjectReference("c", "b", "a")       | new ObjectReference("c", null, "a")      | true   | false
        new ObjectReference(null, "c", "b", "a") | new ObjectReference("c", "b", "a")       | true   | true
        new ObjectReference("c", "b", "a")       | new ObjectReference(null, "c", "b", "a") | true   | true
        new ObjectReference(null, "c", "b", "a") | new ObjectReference("c", "b", "a")       | false  | true

    }

    @Unroll("#featureName: #name1 vs #name2")
    def "matches"() {
        expect:
        name1.matches(name2) == expected

        where:
        name1                                                      | name2                                         | expected
        new ObjectReference("a")                                        | null                                          | true
        new ObjectReference("a")                                        | new ObjectReference()                              | true
        new ObjectReference("a")                                        | new ObjectReference(null)                          | true
        new ObjectReference("a")                                        | new ObjectReference("a")                           | true
        new ObjectReference("a")                                        | new ObjectReference("b")                           | false
        new ObjectReference(null, "a")                                  | new ObjectReference("a")                           | true
        new ObjectReference("a")                                        | new ObjectReference(null, "a")                     | true
        new ObjectReference("a")                                        | new ObjectReference(null, "b")                     | false
        new ObjectReference("a", null)                                  | new ObjectReference("a", "b")                      | true
        new ObjectReference("a", null, "c")                             | new ObjectReference("a", "b", "c")                 | true
        new ObjectReference("a", null, "c")                             | new ObjectReference("a", "b", "c")                 | true
        new ObjectReference(null, "a")                                  | new ObjectReference(null, "a")                     | true
        new ObjectReference("LIQUIBASE", "PUBLIC", "TABLE1", "COLUMN1") | new ObjectReference("PUBLIC", "TABLE1", "COLUMN1") | true;
        new ObjectReference("PUBLIC", "TABLE1", "COLUMN1") | new ObjectReference("LIQUIBASE", "PUBLIC", "TABLE1", "COLUMN1") | true;

    }

    @Unroll
    def "truncate"() {
        expect:
        name.truncate(length).toString() == expected

        where:
        name                          | length | expected
        new ObjectReference()              | 1      | "#UNSET"
        new ObjectReference()              | 2      | "#UNSET"
        new ObjectReference("a")           | 1      | "a"
        new ObjectReference("a")           | 2      | "a"
        new ObjectReference("a")           | 3      | "a"
        new ObjectReference("a", "b")      | 1      | "b"
        new ObjectReference("a", "b")      | 2      | "a.b"
        new ObjectReference("a", "b")      | 3      | "a.b"
        new ObjectReference("a", "b", "c") | 1      | "c"
        new ObjectReference("a", "b", "c") | 2      | "b.c"
        new ObjectReference("a", "b", "c") | 3      | "a.b.c"


    }
}

