package liquibase.configuration

import liquibase.configuration.core.DeprecatedConfigurationValueProvider
import spock.lang.Specification

class ConfigurationPropertyTest extends Specification {

    def "setup"() {
        DeprecatedConfigurationValueProvider.clearData()
    }

    def "cleanup"() {
        DeprecatedConfigurationValueProvider.clearData()
    }

    def "Works correctly"() {
        when:
        def builder = new ConfigurationDefinition.Builder("test").define("property", String)
        def property = new ConfigurationProperty("test", builder)
                .addAlias("test.other.property")
                .setDescription("test property desc")

        then:
        property.getNamespace() == "test"
        property.getName() == "property"
        property.getType() == String
        property.getDescription() == "test property desc"
        property.getValue() == null

        when:
        property.setDefaultValue("default value")
        then:
        property.getDefaultValue() == "default value"
        property.getValue() == "default value"
        !property.getWasOverridden()

        when:
        DeprecatedConfigurationValueProvider.setData("test.property", "set value")
        then:
        property.getValue(String) == "set value"
        property.getWasOverridden()


        when:
        property.setValue("other value")
        then:
        property.getValue() == "other value"

        when:
        DeprecatedConfigurationValueProvider.clearData()
        DeprecatedConfigurationValueProvider.setData("test.other.property", "alias value")
        then:
        property.getValue() == "alias value"
    }
}
