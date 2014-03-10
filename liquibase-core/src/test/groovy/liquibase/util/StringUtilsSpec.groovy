package liquibase.util

import spock.lang.Specification

class StringUtilsSpec extends Specification {

    def "join with map"() {
        expect:
        StringUtils.join((Map) map as Map, delimiter) == value

        where:
        map | value | delimiter
        new HashMap() | "" | ", "
        [key1:"a"] | "key1=a" | ", "
        [key1:"a", key2:"b"] | "key1=a, key2=b" | ", "
        [key1:"a", key2:"b"] | "key1=aXkey2=b" | "X"
        [key1:"a", key2:"b", key3:"c"] | "key1=a, key2=b, key3=c" | ", "
    }
}
