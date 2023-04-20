package liquibase.util

import spock.lang.Specification
import spock.lang.Unroll

class SmartMapTest extends Specification {

    def "get passing type"() {
        when:
        def map = new SmartMap()
        map.put("integer", Integer.valueOf(3))
        map.put("string", "a string value")
        map.put("stringNumber", "456")

        then:
        map.get("integer", Integer) == 3
        map.get("integer", Long) == 3L
        map.get("stringNumber", Integer) == 456
    }

    @Unroll
    def "get passing default"() {
        when:
        def map = new SmartMap()
        map.put("integer", Integer.valueOf(3))
        map.put("string", "a string value")

        then:
        map.get(key, defaultValue) == expected

        where:
        key              | defaultValue | expected
        "integer"        | 15           | 3
        "integer"        | null         | 3
        "invalidInteger" | 15           | 15
        "invalidInteger" | null         | null
    }

    def "put"() {
        when:
        def map = new SmartMap()
        map.put("integer", Integer.valueOf(3))

        then:
        map.get("integer") == 3

        when:
        map.put("integer", null)

        then:
        !map.containsKey("integer")

    }

}
