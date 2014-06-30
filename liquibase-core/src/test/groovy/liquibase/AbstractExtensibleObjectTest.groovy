package liquibase

import liquibase.exception.UnexpectedLiquibaseException
import liquibase.util.ObjectUtil
import liquibase.util.StringUtils
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Abstract test class to extend from when testing AbstractExtensibleObjects.
 */
abstract class AbstractExtensibleObjectTest extends Specification {

    @Unroll("#featureName: #propertyName")
    def "convenience get/set methods work correctly"() {
        expect:
        def defaultValue = getDefaultPropertyValue(propertyName)
        def newValue = getTestPropertyValue(propertyName)

        def obj = createObject();
        def upperCasePropertyName = StringUtils.upperCaseFirst(propertyName)

        def getMethod = "get$upperCasePropertyName"
        if (obj.getMetaClass().getMetaMethod(getMethod) == null) {
            getMethod = "is$upperCasePropertyName"

            if (obj.getMetaClass().getMetaMethod(getMethod) == null) {
                throw new UnexpectedLiquibaseException("Cannot find getter method for $propertyName")
            }
        }

        obj."$getMethod"() == defaultValue


        def returnFromSet = obj."set$upperCasePropertyName"(newValue)
        returnFromSet == obj

        obj."$getMethod"() == newValue

        assert obj.getAttribute(propertyName, Object.class) == newValue

        if (newValue == true || newValue == false) {
            obj."set$upperCasePropertyName"(!newValue)
            assert obj."$getMethod"() == !newValue
        }

        where:
        propertyName << getStandardProperties()
    }

    @Unroll("#featureName: #propertyName")
    def "convenience set method return 'this'"() {
        when:
        def obj = createObject()
        def type = obj.getMetaClass().getMetaProperty(propertyName).type
        def setMethod = obj.getMetaClass().getMetaMethod("set${StringUtils.upperCaseFirst(propertyName)}", type)

        then:
        setMethod.returnType.isAssignableFrom(obj.class)

        where:
        propertyName << getStandardProperties()
    }

    def "attribute methods work correctly"() {
        when:
        def TEST_ATTRIBUTE_NAME = "test_attribute_name"
        def obj = createObject()

        then:
        obj.getAttribute(TEST_ATTRIBUTE_NAME, Object.class) == null
        obj.getAttribute(TEST_ATTRIBUTE_NAME, null) == null
        obj.getAttribute(TEST_ATTRIBUTE_NAME, 5) == 5
        assert !obj.getAttributes().contains(TEST_ATTRIBUTE_NAME)

        when:
        obj.setAttribute(TEST_ATTRIBUTE_NAME, "10")

        then:
        obj.getAttribute(TEST_ATTRIBUTE_NAME, Object.class) == "10"
        obj.getAttribute(TEST_ATTRIBUTE_NAME, String.class) == "10"
        obj.getAttribute(TEST_ATTRIBUTE_NAME, Integer.class) == 10
        obj.getAttribute(TEST_ATTRIBUTE_NAME, null) == "10"
        obj.getAttribute(TEST_ATTRIBUTE_NAME, 5) == 10
        assert obj.getAttributes().contains(TEST_ATTRIBUTE_NAME)

        when:
        obj.setAttribute(TEST_ATTRIBUTE_NAME, null)

        then:
        obj.getAttribute(TEST_ATTRIBUTE_NAME, Object.class) == null
    }

    /**
     * Creates a standard object for use in tests. Override if a no-arg constructor isn't the best way to construct your test object.
     */
    protected AbstractExtensibleObject createObject() {
        return Class.forName(getClass().getName().replaceFirst("Test\$", "")).newInstance()
    }

    /**
     * Returns a list of standard properties. Standard properties have get/set methods and are backed by attributes
     */
    protected List<String> getStandardProperties() {
        def list = createObject().getMetaClass().getProperties().collect({it.name})
        list.remove("class")
        list.remove("attributes")
        list.remove("attributeMap")
        return list;
    }

    protected Object getDefaultPropertyValue(String propertyName) {
      return null
    }

    protected Object getTestPropertyValue(String propertyName) {
        def object = createObject()
        def type = object.getMetaClass().getMetaProperty(propertyName).type
        if (type == String.class) {
            return "A test value"
        } else if (type == Boolean.class || type == boolean) {
            return true
        } else if (Number.isAssignableFrom(type)) {
            return ObjectUtil.convert("12", type);
        } else if (type == Object.class) {
            return "Some type of object"
        } else {
            throw new UnexpectedLiquibaseException("No default testPropertyValue for $propertyName of type ${type.simpleName}")
        }
    }

}
