package liquibase

import spock.lang.Specification

class JUnitScopeTest extends Specification {

    def "child functions return JUnitScope instances"() {
        expect:
        JUnitScope.instance.child([a: "1", b: "2"]) instanceof JUnitScope
        JUnitScope.instance.child("a", 1) instanceof JUnitScope
    }

    def "can override singleton"() {
        when:
        def childScope = JUnitScope.instance.overrideSingleton(String, "a");

        then:
        childScope.getSingleton(String) == "a"
        JUnitScope.instance.getSingleton(String) == ""
        childScope.child("test", "value").getSingleton(String) == "a"
    }
}
