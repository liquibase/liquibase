package liquibase.util

import spock.lang.Specification
import spock.lang.Unroll

class StringUtilsSpec extends Specification {

    def "join with map"() {
        expect:
        StringUtils.join((Map) map as Map, delimiter) == value

        where:
        map                               | value                    | delimiter
        new HashMap()                     | ""                       | ", "
        [key1: "a"]                       | "key1=a"                 | ", "
        [key1: "a", key2: "b"]            | "key1=a, key2=b"         | ", "
        [key1: "a", key2: "b"]            | "key1=aXkey2=b"          | "X"
        [key1: "a", key2: "b", key3: "c"] | "key1=a, key2=b, key3=c" | ", "
    }

    @Unroll
    def "join sorted"() {
        expect:
        StringUtils.join(array, ",", sorted) == expected

        where:
        array           | sorted | expected
        ["a", "c", "b"] | true   | "a,b,c"
        ["a", "c", "b"] | false  | "a,c,b"
    }

    @Unroll
    def "join with formatter sorted"() {
        expect:
        StringUtils.join(array, ",", new StringUtils.ToStringFormatter(), sorted) == expected

        where:
        array     | sorted | expected
        [1, 3, 2] | true   | "1,2,3"
        [1, 3, 2] | false  | "1,3,2"
    }


    def "pad"() {
        expect:
        StringUtils.pad(input, pad) == output

        where:
        input   | pad | output
        null    | 0   | ""
        null    | 3   | "   "
        ""      | 0   | ""
        ""      | 3   | "   "
        " "     | 3   | "   "
        "abc"   | 2   | "abc"
        "abc"   | 3   | "abc"
        "abc  " | 3   | "abc"
        "abc"   | 5   | "abc  "
        "abc "  | 5   | "abc  "
    }
}
