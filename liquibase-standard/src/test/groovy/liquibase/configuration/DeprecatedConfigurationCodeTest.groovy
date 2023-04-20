package liquibase.configuration

import liquibase.configuration.core.DeprecatedConfigurationValueProvider
import spock.lang.Specification

/**
 * Test deprecated styles of code to ensure we are API compatible with them.
 * Be liberal with type casts so ensure we are using the correct APIs
 */
class DeprecatedConfigurationCodeTest extends Specification {

    def "cleanup"() {
        DeprecatedConfigurationValueProvider.clearData()
    }

    def "4_3 getter style configuration lookup"() {
        expect:
        LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding() == "UTF-8"
    }

    def "4_3 setter style configuration setting"() {
        when:
        def config = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class)

        config.setOutputEncoding("ASCII")

        then:
        config.getOutputEncoding() == "ASCII"

        when:
        DeprecatedConfigurationValueProvider.clearData()

        then:
        config.getOutputEncoding() == "UTF-8"
    }

    def "4_3 ConfigurationContainer custom implementations"() {
        when:
        def config = LiquibaseConfiguration.getInstance().getConfiguration(DeprecatedConfigurationConfig.class)
        def property = (ConfigurationProperty) config.getProperty(DeprecatedConfigurationConfig.TEST_PROPERTY)

        then:
        config.getProperties()*.getName() == ["testProperty"]
        property.description == "A test property"
        property.getValue() == "default value"
        !property.getWasOverridden()
    }

    private static class DeprecatedConfigurationConfig extends AbstractConfigurationContainer {

        private static final String TEST_PROPERTY = "testProperty"

        DeprecatedConfigurationConfig() {
            super("test");

            getContainer().addProperty(TEST_PROPERTY, String.class)
                    .setDescription("A test property")
                    .setDefaultValue("default value")
                    .addAlias("mock.property");
        }

        /**
         * Should Liquibase execute
         */
        boolean getTestProperty() {
            return getContainer().getValue(TEST_PROPERTY, Boolean.class);
        }

        DeprecatedConfigurationConfig setTestProperty(boolean value) {
            getContainer().setValue(TEST_PROPERTY, value);
            return this;
        }
    }

}
