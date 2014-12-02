package liquibase.util

import spock.lang.Specification

class SmartMapTest extends Specification {

    def "collection operations work as expected"() {
        expect:
        def map = new SmartMap();

        map.size() == 0
        assert !map.containsKey("x")
        assert !map.containsValue("a value")
        map.entrySet().size() == 0
        map.values().size() == 0

        assert map.put("key", "key val") == null
        map.size() == 1
        assert !map.containsKey("x")
        assert map.containsKey("key")
        assert map.containsValue("key val")

        assert map.put("key2", "key val 2") == null
        map.size() == 2
        assert !map.containsKey("x")
        assert map.containsKey("key")
        assert map.containsKey("key2")
        assert map.containsValue("key val")
        assert map.containsValue("key val 2")

        assert map.put("key", "new key val") == "key val"
        map.size() == 2
        assert !map.containsKey("x")
        assert map.containsKey("key")
        assert map.containsKey("key2")
        assert !map.containsValue("key val")
        assert map.containsValue("key val 2")
        assert map.containsValue("new key val")

        map.get("x") == null
        map.get("key") == "new key val"
        map.get("key2") == "key val 2"
    }

    def "get() with target class"() {
        when:
        def map = new SmartMap()
        map.put("x", 1)
        map.put("y", "t")
        map.put("z", "4.32")

        then:
        map.get("none", Integer) == null
        map.get("x", Integer) == 1I
        map.get("x", String) == "1"
        map.get("x", Float) == 1.0F

        map.get("y", String) == "t"
        assert map.get("y", Boolean)

        map.get("z", Float) == 4.32F
        map.get("z", String) == "4.32"
    }

    def "get() with default value"() {
        when:
        def map = new SmartMap()
        map.put("x", 1)
        map.put("y", "t")
        map.put("z", "4.32")

        then:
        map.get("none", 42) == 42
        map.get("none", "a value") == "a value"
        map.get("x", 3I) == 1I
        assert map.get("y", false)
        map.get("z", 100f) == 4.32f
    }
}
