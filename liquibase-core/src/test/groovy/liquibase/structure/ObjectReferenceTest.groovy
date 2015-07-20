package liquibase.structure

import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class ObjectReferenceTest extends Specification {

    def "can pass only the type and get a null objectName"() {
        when:
        def reference = new ObjectReference(Table)

        then:
        reference.objectType == Table;
        reference.objectName == null;
    }

    def "can create by strings"() {
        expect:
        new ObjectReference(Table, "a", "b").objectName.asList() == ["a", "b"]
    }

    @Unroll
    def "toString logic"() {
        expect:
        new ObjectReference(type, name).toString() == expected

        where:
        type  | name                          | expected
        Table | new ObjectName("a")           | "Table a"
        Table | new ObjectName("a", "b")      | "Table a.b"
        Table | new ObjectName("a", "b", "c") | "Table a.b.c"
        null  | new ObjectName("a", "b", "c") | "a.b.c"
        Table | null                          | "Table"
        null  | null                          | ""
    }
}
