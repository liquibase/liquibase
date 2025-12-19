package liquibase.extension.testing.testsystem;

import spock.lang.Specification
import spock.lang.Unroll;

class TestSystemDefinitionTest extends Specification {

    @Unroll
    def parse() {
        expect:
        TestSystem.Definition.parse(input).toString() == expected

        where:
        input                                  | expected
        "test"                                 | "test"
        "test:prof1"                           | "test:prof1"
        "test:prof1,prof2"                     | "test:prof1,prof2"
        "test:prof2,prof1"                     | "test:prof2,prof1"
        "test?key1=val1"                       | "test?key1=val1"
        "test?key1=val1&key2=val2"             | "test?key1=val1&key2=val2"
        "test?key2=val2&key1=val1"             | "test?key1=val1&key2=val2"
        "test:prof1?key2=val2&key1=val1"       | "test:prof1?key1=val1&key2=val2"
        "test:prof1,prof2?key2=val2&key1=val1" | "test:prof1,prof2?key1=val1&key2=val2"
    }

    def "parse null"() {
        expect:
        TestSystem.Definition.parse(null) == null
    }

    @Unroll
    def "equals, compareTo, and hashCode"() {
        when:
        def obj1 = TestSystem.Definition.parse(def1)
        def obj2 = TestSystem.Definition.parse(def2)

        then:
        obj1.equals(obj2) == equal

        if (equal) {
            assert obj1.compareTo(obj2) == 0
            assert obj1.hashCode() == obj2.hashCode()
        } else {
            assert obj1.compareTo(obj2) != 0
            if (obj2 != null) {
                assert obj1.hashCode() != obj2.hashCode()
            }
        }

        where:
        def1         | def2         | equal
        "test"       | "test"       | true
        "test:x"     | "test:x"     | true
        "test"       | "test:x"     | false
        "test:x"     | "test"       | false
        "test:x,y"   | "test:y,x"   | false
        "test?k=v"   | "test?k=v"   | true
        "test:x?k=v" | "test:x?k=v" | true
        "test:x?k=X" | "test:x?k=v" | false
        "test"       | null         | false
    }

    def getProfiles() {
        when:
        def xyDefinition = TestSystem.Definition.parse("test:x,y")

        then:
        TestSystem.Definition.parse("test").getProfiles().size() == 0
        Arrays.toString(xyDefinition.getProfiles() == "[x, y]")

        !xyDefinition.getProfiles().is(xyDefinition.getProfiles())

    }
}
