package liquibase.configuration

import spock.lang.Specification
import spock.lang.Unroll

import java.util.logging.Level

class ConfigurationValueConverterTest extends Specification {

    @Unroll
    def "LOG_LEVEL instance"() {
        expect:
        ConfigurationValueConverter.LOG_LEVEL.convert(input) == expected

        where:
        input     | expected
        "fine"    | Level.FINE
        "WARNing" | Level.WARNING
        "severE"  | Level.SEVERE
        "oFf"     | Level.OFF
        "debug"   | Level.FINE
        "WARN"    | Level.WARNING
        "error"   | Level.SEVERE
        null      | null
    }

    @Unroll
    def "STRING instance"() {
        expect:
        ConfigurationValueConverter.STRING.convert(input) == expected

        where:
        input      | expected
        null       | null
        ""         | ""
        "a string" | "a string"
        123        | "123"
    }

    @Unroll
    def "CLASS instance"() {
        expect:
        ConfigurationValueConverter.CLASS.convert(input) == expected

        where:
        input             | expected
        null              | null
        Integer.getName() | Integer
    }
}
