package liquibase.util


import spock.lang.Specification
import spock.lang.Unroll

class StringClausesTest extends Specification {

    def "isEmpty"() {
        expect:
        clause.isEmpty() == expected

        where:
        clause                                             | expected
        new StringClauses()                                | true
        new StringClauses().append("")                     | true
        new StringClauses().append("").append("something") | false
        new StringClauses().append("").append("")          | true
    }

    def append() {
        when:
        def clauses = new StringClauses()
        clauses.append("first").append("second").append("third")

        then:
        clauses.toString() == "first second third"

        when:
        clauses.append(new StringClauses.Comment("/* comment here */"))
        then:
        clauses.toString() == "first second third /* comment here */"

        when:
        clauses.append("added new1", "new1")
        then:
        clauses.toString() == "first second third /* comment here */ new1"

        when:
        clauses.append(StringClausesTestEnum.enum_key, "enumKey")
        then:
        clauses.toString() == "first second third /* comment here */ new1 enumKey"
    }

    def prepend() {
        when:
        def clauses = new StringClauses()
        clauses.append("first").prepend("second").append("third")

        then:
        clauses.toString() == "second first third"

        when:
        clauses.prepend(new StringClauses.Comment("/* comment here */"))
        then:
        clauses.toString() == "/* comment here */ second first third"

        when:
        clauses.prepend("added new1", "new1")
        then:
        clauses.toString() == "new1 /* comment here */ second first third"

        when:
        clauses.prepend(StringClausesTestEnum.enum_key, "enumKey")
        then:
        clauses.toString() == "enumKey new1 /* comment here */ second first third"
    }

    def remove() {
        when:
        def clauses = new StringClauses()
        clauses.append("first").prepend("second").append("third").append(StringClausesTestEnum.enum_key, "enumKey")
        clauses.remove("second")
                .remove(StringClausesTestEnum.enum_key)
                .remove("invalid key is no-op")

        then:
        clauses.toString() == "first third"
    }

    def replace() {
        when:
        def clauses = new StringClauses()
        clauses.append("first").append("second").append("third").append(StringClausesTestEnum.enum_key, "enumKey")

        clauses.replace("second", "new second")
        clauses.replace(StringClausesTestEnum.enum_key, "new enum")

        then:
        clauses.toString() == "first new second third new enum"

        when:
        clauses.replace("second", "new second2")
        clauses.replace(StringClausesTestEnum.enum_key, "new enum2")
        then:
        clauses.toString() == "first new second2 third new enum2"

        when:
        clauses.replace("invalid", "invalid value")
        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Key 'invalid' is not defined"
    }

    def contains() {
        when:
        def clauses = new StringClauses()
        clauses.append("first").append("second", "second value").append("third").append(StringClausesTestEnum.enum_key, "enumKey")

        then:
        clauses.contains("first")
        clauses.contains("second")
        !clauses.contains("second value")
        !clauses.contains("invalid")
    }

    def insertBefore() {
        when:
        def clauses = new StringClauses()
        clauses.append("first").append("second").append("third").append(StringClausesTestEnum.enum_key, "enumKey")
        clauses.insertBefore("second", "newKey", "new key")
                .insertBefore(StringClausesTestEnum.enum_key, "new enum")

        then:
        clauses.toString() == "first new key second third new enum enumKey"
    }

    def insertAfter() {
        when:
        def clauses = new StringClauses()
        clauses.append("first").append("second").append("third").append(StringClausesTestEnum.enum_key, "enumKey")
        clauses.insertAfter("second", "newKey", "new key")
                .insertAfter(StringClausesTestEnum.enum_key, "new enum")

        then:
        clauses.toString() == "first second new key third enumKey new enum"
    }

    @Unroll
    def get() {
        when:
        def clauses = new StringClauses()
        clauses.append("first").append("second", "second value").append("third").append(StringClausesTestEnum.enum_key, new StringClauses().append("sub1").append("sub2"))

        then:
        clauses.get(key) == expected

        where:
        key                            | expected
        "first"                        | "first"
        "FIRST"                        | "first"
        "second"                       | "second value"
        StringClausesTestEnum.enum_key | "sub1 sub2"
        "invalid"                      | null

    }

    def getSubclause() {
        when:
        def clauses = new StringClauses()
        clauses.append("first").append("second", "second value").append("third").append(StringClausesTestEnum.enum_key, new StringClauses().append("sub1").append("sub2"))

        then:
        clauses.getSubclause("first") == new StringClauses().append("first")
        clauses.getSubclause(StringClausesTestEnum.enum_key) == new StringClauses().append("sub1").append("sub2")
        clauses.get("invalid") == null
    }

    def "clauseIterator"() {
        when:
        def clauses = new StringClauses()
        clauses.append("first").append("second", "second value").append(new StringClauses.Whitespace(" ")).append("third").append(StringClausesTestEnum.enum_key, new StringClauses().append("sub1").append("sub2"))

        def iterator = clauses.getClauseIterator()

        then:
        iterator.next() == "first"
        iterator.nextNonWhitespace() == "second value"
        iterator.nextNonWhitespace() == "third"
        iterator.next() == new StringClauses().append("sub1").append("sub2")

    }

    def "toArray"() {
        when:
        def clauses = new StringClauses()
        clauses.append("first").append("second", "second value").append("third").append(StringClausesTestEnum.enum_key, new StringClauses().append("sub1").append("sub2"))

        then:
        clauses.toArray(true) == ["first", "second value", "third", "sub1 sub2"] as Object[]
        clauses.toArray(false) == ["first", "second value", "third", new StringClauses().append("sub1").append("sub2")] as Object[]
    }

    private enum StringClausesTestEnum {
        enum_key
    }
}
