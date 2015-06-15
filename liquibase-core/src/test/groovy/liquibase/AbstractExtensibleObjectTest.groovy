package liquibase

import liquibase.action.core.AddAutoIncrementAction
import liquibase.action.core.DropTableAction
import liquibase.structure.ObjectName
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import spock.lang.Specification

import static org.hamcrest.Matchers.containsInAnyOrder
import static spock.util.matcher.HamcrestSupport.that

class AbstractExtensibleObjectTest extends Specification {

    def "add works"() {
        given:
        def obj = new AbstractExtensibleObject() {}
        obj.set("wasObj", "Value 1a")
        obj.set("wasEmpty", [])

        when:
        obj.add("wasObj", "Value 1b")
        obj.add("wasObj", "Value 1c")
        obj.add("wasEmpty", "Value 2a")
        obj.add("wasEmpty", "Value 2b")
        obj.add("wasEmpty", "Value 2c")
        obj.add("wasNull", "Value 3a")
        obj.add("wasNull", "Value 3b")
        obj.add("wasNull", "Value 3c")

        then:
        obj.get("wasObj", Collection) == ["Value 1a", "Value 1b", "Value 1c",]
        obj.get("wasEmpty", Collection) == ["Value 2a", "Value 2b", "Value 2c",]
        obj.get("wasNull", Collection) == ["Value 3a", "Value 3b", "Value 3c",]

    }

    def "getAttributeNames"() {
        when:
        def nonFieldObject = new AbstractExtensibleObject() {};
        nonFieldObject.set("value1", "One")
        nonFieldObject.set("value2", "Two")
        nonFieldObject.set("value3", 3)
        nonFieldObject.set("valueNull", null)

        def fieldObject = new AddAutoIncrementAction();
        fieldObject.set("value1", "One")
        fieldObject.set("value2", 2)
        fieldObject.set("startWith", 12)
        fieldObject.incrementBy = new BigInteger(32)
        fieldObject.columnName = new ObjectName("x", "y")

        then:
        that nonFieldObject.getAttributeNames(), containsInAnyOrder(["value1", "value2", "value3"] as String[])
        that fieldObject.getAttributeNames(), containsInAnyOrder(["value1", "value2", "startWith", "incrementBy", "columnName"] as String[])
    }

    def "getStandardAttributeNames"() {
        expect:
        that new AddAutoIncrementAction().getStandardAttributeNames(), containsInAnyOrder(["startWith", "columnDataType", "columnName", "incrementBy"] as String[])
        that new AddAutoIncrementAction().getStandardAttributeNames(), containsInAnyOrder(["startWith", "columnDataType", "columnName", "incrementBy"] as String[]) //caching works

        that new DropTableAction().getStandardAttributeNames(), containsInAnyOrder(["tableName", "cascadeConstraints"] as String[])

        (new AbstractExtensibleObject() {}).getStandardAttributeNames().size() == 0
    }

    def "get/set works with non-field values"() {
        when:
        def obj = new AbstractExtensibleObject() {};
        obj.set("value1", "One")
        obj.set("value2", "Two")
        obj.set("value3", 3)
        obj.set("valueNull", null)

        then:
        obj.get("value1", String) == "One"
        obj.get("value2", String) == "Two"
        obj.get("value3", String) == "3"
        obj.get("value3", Integer) == 3
        obj.get("valueNull", Integer) == null
        obj.get("valueUndefined", Integer) == null
    }

    def "get/set works with field values"() {
        when:
        def obj = new AddAutoIncrementAction();
        obj.set("value1", "One")
        obj.set("value2", 2)
        obj.set("startWith", 12)
        obj.incrementBy = new BigInteger(32)

        then:
        obj.get("value1", String) == "One"
        obj.get("value2", String) == "2"
        obj.get("value2", Integer) == 2

        obj.get("startWith", Integer) == 12
        obj.get("startWith", String) == "12"
        obj.startWith == new BigInteger("12")
        assert obj.has("startWith")

        obj.get("incrementBy", Integer) == 32
        obj.get("incrementBy", String) == "32"
        obj.incrementBy == new BigInteger("32")
        assert obj.has("incrementBy")

        obj.columnDataType == null;
        obj.get("columnDataType", String) == null
        assert !obj.has("columnDataType")
    }

    def "get/set works with field values on parent objects"() {
        when:
        def obj = new Table();
        obj.set("value1", "One")
        obj.set("value2", 2)
        obj.columns = [new Column(new ObjectName("a")), new Column(new ObjectName("b"))]
        obj.name = new ObjectName("testTable")

        then:
        obj.get("value2", String) == "2"
        obj.columns*.toString() == ["a", "b"]
        obj.get("columns", List)*.toString() == ["a", "b"]
        obj.name.toString() == "testTable"
        obj.get("name", ObjectName).toString() == "testTable"

        when:
        obj.set("columns", [new Column(new ObjectName("x")), new Column(new ObjectName("y"))])
        obj.set("name", new ObjectName("newTableName"))

        then:
        obj.get("value2", String) == "2"
        obj.columns*.toString() == ["x", "y"]
        obj.get("columns", List)*.toString() == ["x", "y"]
        obj.name.toString() == "newTableName"
        obj.get("name", ObjectName).toString() == "newTableName"

    }
}
