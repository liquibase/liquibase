package liquibase.configuration

import liquibase.Scope
import liquibase.configuration.core.SystemPropertyValueProvider
import spock.lang.Specification

class LiquibaseConfigurationTest extends Specification {

    def "getCurrentConfiguredValue"() {
        when:
        System.setProperty("test.currentValue", "From system")
        def currentValue = Scope.child(["test.currentValue": "From scope"], new Scope.ScopedRunnerWithReturn<ConfiguredValue>() {
            @Override
            ConfiguredValue run() throws Exception {
                return Scope.currentScope.getSingleton(LiquibaseConfiguration).getCurrentConfiguredValue(null, null, "test.currentValue")
            }
        })

        then:
        currentValue.value == "From scope"
        currentValue.providedValues*.describe() == ["Scoped value 'test.currentValue'", "System property 'test.currentValue'"]
    }

    def "getCurrentConfiguredValue with no value found"() {
        when:
        def currentValue = Scope.currentScope.getSingleton(LiquibaseConfiguration).getCurrentConfiguredValue(null, null, "test.unknownValue")

        then:
        currentValue != null
        currentValue.getValue() == null
        currentValue.getProvidedValue().sourceDescription == "No configured value found"
        currentValue.getProvidedValue().requestedKey == "test.unknownValue"
        currentValue.getProvidedValue().provider != null
    }

    def "getCurrentConfiguredValue value can be modified"() {
        given:
        def testModifier = new TestModifier()
        def modifierFactory = Scope.getCurrentScope().getSingleton(ConfiguredValueModifierFactory.class)

        modifierFactory.register(testModifier)

        when:
        def currentValue = Scope.child(["requested.key": "From scope"], new Scope.ScopedRunnerWithReturn<ConfiguredValue>() {
            @Override
            ConfiguredValue run() throws Exception {
                return Scope.currentScope.getSingleton(LiquibaseConfiguration).getCurrentConfiguredValue(null, null, "requested.key")
            }
        })

        then:
        currentValue.found()
        currentValue.getValue() == "modified 'From scope'"
        currentValue.getProvidedValue().getSourceDescription() == "From TestModifier"

        cleanup:
        modifierFactory.unregister(testModifier)
    }

    def "configured value is modified with proper priority"() {
        given:
        def testModifier = new TestModifier()
        def higherModifier = new TestModifierHigherOrder();
        def modifierFactory = Scope.getCurrentScope().getSingleton(ConfiguredValueModifierFactory.class)

        modifierFactory.register(testModifier)
        modifierFactory.register(higherModifier)

        when:
        def currentValue = Scope.child(["requested.key": "From scope"], new Scope.ScopedRunnerWithReturn<ConfiguredValue>() {
            @Override
            ConfiguredValue run() throws Exception {
                return Scope.currentScope.getSingleton(LiquibaseConfiguration).getCurrentConfiguredValue(null, null, "requested.key")
            }
        })

        then:
        currentValue.found();
        currentValue.getValue() == "order 200 'modified 'From scope''"

        cleanup:
        Scope.getCurrentScope().getSingleton(ConfiguredValueModifierFactory.class).unregister(testModifier)
        Scope.getCurrentScope().getSingleton(ConfiguredValueModifierFactory.class).unregister(higherModifier)
    }

    def "autoRegisters and sorts providers"() {
        expect:
        Scope.getCurrentScope().getSingleton(LiquibaseConfiguration).configurationValueProviders*.getClass()*.getName().contains("liquibase.configuration.core.SystemPropertyValueProvider")
    }

    def "autoRegisters definitions"() {
        expect:
        Scope.getCurrentScope().getSingleton(LiquibaseConfiguration).getRegisteredDefinitions(false).size() > 10
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


    class TestModifier implements ConfiguredValueModifier<String> {
        @Override
        int getOrder() {
            return 100
        }

        @Override
        void override(ConfiguredValue<String> configuredValue) {
            def value = configuredValue.getProvidedValue()

            configuredValue.override(new ProvidedValue(
                    value.getRequestedKey(),
                    value.getActualKey(),
                    "modified '" + value.getValue() + "'",
                    "From TestModifier",
                    value.getProvider()
            ))
        }
    }

    class TestModifierHigherOrder implements ConfiguredValueModifier<String> {
        @Override
        int getOrder() {
            return 200
        }

        @Override
        void override(ConfiguredValue<String> configuredValue) {
            def value = configuredValue.getProvidedValue()

            configuredValue.override(new ProvidedValue(
                    value.getRequestedKey(),
                    value.getActualKey(),
                    "order 200 '" + value.getValue() + "'",
                    value.getSourceDescription(),
                    new SystemPropertyValueProvider()
            ))
        }

    }
}
