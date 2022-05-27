package liquibase.configuration

import liquibase.Scope
import liquibase.configuration.core.ScopeValueProvider
import liquibase.configuration.core.SystemPropertyValueProvider
import spock.lang.Specification

class ConfiguredValueTest extends Specification {

    def "empty"() {
        when:
        def configuredValue = new ConfiguredValue("requested.key", null, null)

        then:
        !configuredValue.found()
        configuredValue.getValue() == null
        configuredValue.getProvidedValues()*.describe() == ["No configured value found 'requested.key'"]
        configuredValue.getProvidedValue().describe() == "No configured value found 'requested.key'"
    }

    def "one override"() {
        when:
        def configuredValue = new ConfiguredValue(null, null, null)
        configuredValue.override(new ProvidedValue("requested.key", "actual.key", "value", "first override", new SystemPropertyValueProvider()))

        then:
        configuredValue.found()
        configuredValue.getValue() == "value"
        configuredValue.getProvidedValue().describe() == "first override 'actual.key'"
        configuredValue.getProvidedValue().requestedKey == "requested.key"
        configuredValue.getProvidedValue().actualKey == "actual.key"
        configuredValue.getProvidedValues()*.describe() == ["first override 'actual.key'"]
        configuredValue.getProvidedValues()*.provider*.class.name == [SystemPropertyValueProvider.name]
    }

    def "two overrides"() {
        when:
        def configuredValue = new ConfiguredValue(null, null, null)
        configuredValue.override(new ProvidedValue("requested.key", "actual.key", "value", "first override", new SystemPropertyValueProvider()))
        configuredValue.override(null)
        configuredValue.override(new ProvidedValue("requested.key", "other.actual.key", "second value", "second override", new ScopeValueProvider()))

        then:
        configuredValue.found()
        configuredValue.getValue() == "second value"
        configuredValue.getProvidedValue().describe() == "second override 'other.actual.key'"
        configuredValue.getProvidedValue().requestedKey == "requested.key"
        configuredValue.getProvidedValue().actualKey == "other.actual.key"
        configuredValue.getProvidedValues()*.provider*.class.name == [ScopeValueProvider.name, SystemPropertyValueProvider.name]
        configuredValue.getProvidedValues()*.describe() == ["second override 'other.actual.key'", "first override 'actual.key'"]
    }

    def "configured value can be modified"() {
        when:
        def configuredValue = new ConfiguredValue(null, null, null);
        configuredValue.override(new ProvidedValue("requested.key", "actual.key", "value", "first override", new ScopeValueProvider()))

        def modifierFactory = Scope.getCurrentScope().getSingleton(ConfiguredValueModifierFactory.class);
        def testModifier = new TestModifier();
        modifierFactory.register(testModifier);

        then:
        configuredValue.found()
        configuredValue.getValue() == "modified value"

        cleanup:
        Scope.getCurrentScope().getSingleton(ConfiguredValueModifierFactory.class).removeInstance(testModifier)
    }

    def "configured value is modified with proper priority"() {
        when:
        def configuredValue = new ConfiguredValue(null, null, null);
        configuredValue.override(new ProvidedValue("requested.key", "actual.key", "value", "first override", new ScopeValueProvider()))

        def modifierFactory = Scope.getCurrentScope().getSingleton(ConfiguredValueModifierFactory.class);
        def testModifier = new TestModifier();
        def higherPriorityModifier = new TestModifierHigherPriority();
        modifierFactory.register(testModifier);
        modifierFactory.register(higherPriorityModifier);

        then:
        configuredValue.found();
        configuredValue.getValue() == "priority 200 value";

        cleanup:
        Scope.getCurrentScope().getSingleton(ConfiguredValueModifierFactory.class).removeInstance(testModifier)
        Scope.getCurrentScope().getSingleton(ConfiguredValueModifierFactory.class).removeInstance(higherPriorityModifier)
    }
}

class TestModifier implements ConfiguredValueModifier {
    @Override
    int getPriority() {
        return 100
    }

    @Override
    Object modify(ProvidedValue object) {
        if (ProvidedValue.isInstance(object)) {
            def value = (ProvidedValue) object
            return new ProvidedValue(
                    value.getRequestedKey(),
                    value.getActualKey(),
                    "modified value",
                    value.getSourceDescription(),
                    new SystemPropertyValueProvider()
            )
        }
        return null
    }
}

class TestModifierHigherPriority implements ConfiguredValueModifier {
    @Override
    int getPriority() {
        return 200
    }

    @Override
    Object modify(ProvidedValue object) {
        if (ProvidedValue.isInstance(object)) {
            def value = (ProvidedValue) object
            return new ProvidedValue(
                    value.getRequestedKey(),
                    value.getActualKey(),
                    "priority 200 value",
                    value.getSourceDescription(),
                    new SystemPropertyValueProvider()
            )
        }
        return null
    }
}