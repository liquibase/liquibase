package liquibase.structure

import liquibase.structure.core.Table
import spock.lang.Specification

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
}
