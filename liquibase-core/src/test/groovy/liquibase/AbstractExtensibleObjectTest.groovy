package liquibase

import spock.lang.Specification

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
}
