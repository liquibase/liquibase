package liquibase.configuration

import liquibase.Scope
import liquibase.servicelocator.ServiceLocator
import spock.lang.Specification

class ConfiguredValueModifierFactoryTest extends Specification {

    // Build the factory with a fake ServiceLocator that returns exactly the given modifiers, so tests are
    // isolated from whatever ConfiguredValueModifiers happen to be on the classpath.
    private static ConfiguredValueModifierFactory factoryWith(List<ConfiguredValueModifier> modifiers) {
        def locator = locatorReturning(ConfiguredValueModifier, modifiers)
        return Scope.child([(Scope.Attr.serviceLocator.name()): locator], {
            def ctor = ConfiguredValueModifierFactory.getDeclaredConstructor()
            ctor.setAccessible(true)
            ctor.newInstance()
        } as Scope.ScopedRunnerWithReturn)
    }

    private static ConfiguredValue stringValue(String key, String value) {
        def cv = new ConfiguredValue(key, null, null)
        cv.override(value, "test")
        return cv
    }

    def "two modifiers with the same getOrder() both run (no silent drop)"() {
        given:
        def calls = []
        def factory = factoryWith([recording(100, "A", calls), recording(100, "B", calls)])

        when:
        factory.override(stringValue("k", "v"))

        then:
        calls.toSet() == ["A", "B"].toSet()
    }

    def "register de-duplicates by identity; unregister removes that instance"() {
        given:
        def calls = []
        def factory = factoryWith([])
        def a = recording(100, "A", calls)
        def b = recording(100, "B", calls)
        factory.register(a)
        factory.register(a)   // same instance again -> ignored
        factory.register(b)

        when:
        factory.override(stringValue("k", "v"))
        then: "each registered instance ran exactly once"
        calls.count { it == "A" } == 1
        calls.count { it == "B" } == 1

        when:
        calls.clear()
        factory.unregister(a)
        factory.override(stringValue("k", "v"))
        then: "only the surviving instance runs"
        calls == ["B"]
    }

    def "override(String) applies the highest-order modifier first and stops at the first change"() {
        given:
        def factory = factoryWith([appending(100, "-low"), appending(200, "-high")])

        expect:
        factory.override("x") == "x-high"
    }

    // --- helpers ---

    private static ConfiguredValueModifier recording(int order, String tag, List<String> log) {
        return new ConfiguredValueModifier<String>() {
            @Override int getOrder() { order }
            @Override void override(ConfiguredValue o) { log.add(tag) }
        }
    }

    private static ConfiguredValueModifier appending(int order, String suffix) {
        return new ConfiguredValueModifier<String>() {
            @Override int getOrder() { order }
            @Override void override(ConfiguredValue o) { o.override(String.valueOf(o.getValue()) + suffix, "test") }
        }
    }

    private static ServiceLocator locatorReturning(Class type, List instances) {
        return new ServiceLocator() {
            @Override int getPriority() { 0 }
            @Override List findInstances(Class interfaceType) { interfaceType == type ? instances : [] }
        }
    }
}