package liquibase.configuration

import spock.lang.Specification

class ConfiguredValueModifierFactoryTest extends Specification {

    // Private constructor; on the core test classpath no ConfiguredValueModifiers are registered via SPI,
    // so a reflectively-constructed instance starts empty and lets us register controlled test modifiers.
    private static ConfiguredValueModifierFactory newFactory() {
        def ctor = ConfiguredValueModifierFactory.getDeclaredConstructor()
        ctor.setAccessible(true)
        return ctor.newInstance()
    }

    private static ConfiguredValue stringValue(String key, String value) {
        def cv = new ConfiguredValue(key, null, null)
        cv.override(value, "test")
        return cv
    }

    def "two modifiers with the same getOrder() both run (no silent drop)"() {
        given:
        def calls = []
        def factory = newFactory()
        factory.register(recording(100, "A", calls))
        factory.register(recording(100, "B", calls))

        when:
        factory.override(stringValue("k", "v"))

        then:
        calls.toSet() == ["A", "B"].toSet()
    }

    def "register de-duplicates by identity; unregister removes that instance"() {
        given:
        def calls = []
        def factory = newFactory()
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
        def factory = newFactory()
        factory.register(appending(100, "-low"))
        factory.register(appending(200, "-high"))

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
}