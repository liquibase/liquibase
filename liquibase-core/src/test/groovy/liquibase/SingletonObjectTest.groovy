package liquibase

import liquibase.plugin.Plugin
import liquibase.util.TestUtil
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Modifier

class SingletonObjectTest extends Specification {

    @Unroll("#featureName: #object")
    def "implementations of singleton object should have non-public constructors"() {
        def constructors = object.getDeclaredConstructors()
        expect:
        assert constructors.size() > 0: "No non-default constructors"
        for (def constructor : constructors) {
            assert !Modifier.isPublic(constructor.getModifiers()): "Constructor " + constructor.toString() + " is public"
        }

        where:
        object << TestUtil.getClasses(SingletonObject).findAll {!it.getName().contains("Mock") && !Plugin.isAssignableFrom(it)}
    }


    @Unroll("#featureName: #object")
    def "implementations of singleton object have a Scope-only or empty constructor"() {
        when:
        if (object.getName().contains("Mock")) {
            return;
        }
        try {
            object.getDeclaredConstructor()
        } catch (NoSuchMethodException) {
            object.getDeclaredConstructor(Scope)
        }

        then:
        noExceptionThrown()

        where:
        object << TestUtil.getClasses(SingletonObject)
    }
}
