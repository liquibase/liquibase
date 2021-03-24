package liquibase.configuration

import liquibase.Scope
import spock.lang.Specification

class LiquibaseConfigurationTest extends Specification {

    def "getCurrentConfiguredValue"() {
        when:
        System.setProperty("test.currentValue", "From system")
        def currentValue = Scope.child(["test.currentValue": "From scope"], new Scope.ScopedRunnerWithReturn<ConfiguredValue>() {
            @Override
            ConfiguredValue run() throws Exception {
                return Scope.currentScope.getSingleton(LiquibaseConfiguration).getCurrentConfiguredValue("test.currentValue")
            }
        })

        then:
        currentValue.value == "From scope"
        currentValue.providedValues*.describe() == ["Scoped value 'test.currentValue'", "System property 'test.currentValue'"]
    }

    def "getCurrentConfiguredValue with no value found"() {
        when:
        def currentValue = Scope.currentScope.getSingleton(LiquibaseConfiguration).getCurrentConfiguredValue("test.unknownValue")

        then:
        currentValue != null
        currentValue.getValue() == null
        currentValue.getProvidedValue().sourceDescription == "No configuration or default value found"
        currentValue.getProvidedValue().requestedKey == "test.unknownValue"
        currentValue.getProvidedValue().provider != null
    }


    def "autoRegisters and sorts providers"() {
        expect:
        Scope.getCurrentScope().getSingleton(LiquibaseConfiguration).configurationValueProviders*.getClass()*.getName().contains("liquibase.configuration.core.SystemPropertyValueProvider")
    }

    def "autoRegisters definitions"() {
        expect:
        Scope.getCurrentScope().getSingleton(LiquibaseConfiguration).getRegisteredDefinitions().size() > 10
    }

    def "getRegisteredDefinition for a key"() {
        when:
        def definition = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration).getRegisteredDefinition("liquibase.shouldRun")

        then:
        definition.key == "liquibase.shouldRun"
        definition.description == "Should Liquibase commands execute"

    }

    def "getRegisteredDefinition for an unknown key"() {
        when:
        def definition = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration).getRegisteredDefinition("test.invalid")

        then:
        definition == null

    }

}
