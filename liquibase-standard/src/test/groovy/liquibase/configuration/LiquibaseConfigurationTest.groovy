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

    def "scoped value provider is visible inside its scope and gone outside"() {
        given:
        def config = Scope.currentScope.getSingleton(LiquibaseConfiguration)
        def provider = new TestValueProvider(["test.scopedKey": "scoped value"], 300)

        when:
        def insideValue = Scope.child([(LiquibaseConfiguration.SCOPED_VALUE_PROVIDERS_KEY): [provider]], {
            return config.getCurrentConfiguredValue(null, null, "test.scopedKey").getValue()
        } as Scope.ScopedRunnerWithReturn)

        then:
        insideValue == "scoped value"
        config.getCurrentConfiguredValue(null, null, "test.scopedKey").getValue() == null
    }

    def "scoped provider ranks by its own precedence against registered providers"() {
        given:
        def config = Scope.currentScope.getSingleton(LiquibaseConfiguration)
        def registered = new TestValueProvider(["test.precedenceKey": "from registered"], registeredPrecedence)
        def scoped = new TestValueProvider(["test.precedenceKey": "from scoped"], scopedPrecedence)
        config.registerProvider(registered)

        when:
        def value = Scope.child([(LiquibaseConfiguration.SCOPED_VALUE_PROVIDERS_KEY): [scoped]], {
            return config.getCurrentConfiguredValue(null, null, "test.precedenceKey").getValue()
        } as Scope.ScopedRunnerWithReturn)

        then:
        value == expected

        cleanup:
        config.unregisterProvider(registered)

        where:
        registeredPrecedence | scopedPrecedence | expected
        400                  | 50               | "from registered"
        50                   | 400              | "from scoped"
    }

    def "concurrent scopes only see their own scoped providers"() {
        given:
        def config = Scope.currentScope.getSingleton(LiquibaseConfiguration)
        def results = Collections.synchronizedMap([:])
        def gate = new java.util.concurrent.CountDownLatch(1)

        when:
        def threads = (1..2).collect { n ->
            Thread.start {
                def provider = new TestValueProvider(["test.concurrentKey": "value" + n], 300)
                Scope.child([(LiquibaseConfiguration.SCOPED_VALUE_PROVIDERS_KEY): [provider]], {
                    gate.await()
                    50.times {
                        results.put(n + "-" + it, config.getCurrentConfiguredValue(null, null, "test.concurrentKey").getValue())
                    }
                } as Scope.ScopedRunner)
            }
        }
        gate.countDown()
        threads*.join()

        then:
        (1..2).every { n -> (0..49).every { results[n + "-" + it] == "value" + n } }
    }

    def "getEffectiveProviders merges the scoped providers into the registered ones"() {
        given:
        def config = Scope.currentScope.getSingleton(LiquibaseConfiguration)
        def registered = new TestValueProvider([:], 400)
        def scoped = new TestValueProvider([:], 50)
        config.registerProvider(registered)

        when:
        def inside = Scope.child([(LiquibaseConfiguration.SCOPED_VALUE_PROVIDERS_KEY): [scoped]], {
            return config.getEffectiveProviders()
        } as Scope.ScopedRunnerWithReturn)

        then:
        inside.contains(scoped)
        inside.contains(registered)
        inside.indexOf(scoped) < inside.indexOf(registered)   // sorted by precedence, not appended
        !config.getEffectiveProviders().contains(scoped)
        !config.getProviders().contains(scoped)               // getProviders stays registered-only

        cleanup:
        config.unregisterProvider(registered)
    }

    class TestValueProvider extends liquibase.configuration.AbstractMapConfigurationValueProvider {
        private final Map<String, Object> values
        private final int precedence

        TestValueProvider(Map<String, Object> values, int precedence) {
            this.values = values
            this.precedence = precedence
        }

        @Override
        protected Map<?, ?> getMap() { return values }

        @Override
        protected String getSourceDescription() { return "Test provider" }

        @Override
        int getPrecedence() { return precedence }
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
