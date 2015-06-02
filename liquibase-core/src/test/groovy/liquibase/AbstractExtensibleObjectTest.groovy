package liquibase

import liquibase.action.core.AddAutoIncrementAction
import liquibase.action.core.DropTableAction
import spock.lang.Specification
import static org.hamcrest.Matchers.containsInAnyOrder
import static spock.util.matcher.HamcrestSupport.that

class AbstractExtensibleObjectTest extends Specification {

    def "add works"() {
        given:
        def obj = new AbstractExtensibleObject() { }
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
        obj.get("wasObj", Collection) == ["Value 1a", "Value 1b", "Value 1c", ]
        obj.get("wasEmpty", Collection) == ["Value 2a", "Value 2b", "Value 2c", ]
        obj.get("wasNull", Collection) == ["Value 3a", "Value 3b", "Value 3c", ]

    }

    def "getStandardAttributeNames"() {
        expect:
        that new AddAutoIncrementAction().getStandardAttributeNames(), containsInAnyOrder(["catalogName", "schemaName", "startWith", "tableName", "columnDataType", "columnName", "incrementBy"] as String[])
        that new AddAutoIncrementAction().getStandardAttributeNames(), containsInAnyOrder(["catalogName", "schemaName", "startWith", "tableName", "columnDataType", "columnName", "incrementBy"] as String[]) //caching works

        that new DropTableAction().getStandardAttributeNames(), containsInAnyOrder(["tableName", "cascadeConstraints"] as String[])

        (new AbstractExtensibleObject() {}).getStandardAttributeNames().size() == 0
    }

    def "getExpectedAttributeType"() {
        expect:
        new AddAutoIncrementAction().getExpectedAttributeType("catalogName") == String
        new AddAutoIncrementAction().getExpectedAttributeType("schemaName") == String
        new AddAutoIncrementAction().getExpectedAttributeType("startWith") == BigInteger
        new AddAutoIncrementAction().getExpectedAttributeType("fakeAttr") == null

        new DropTableAction().getExpectedAttributeType("tableName") == null

        (new AbstractExtensibleObject() {}).getExpectedAttributeType("whatever") == null
    }
}
