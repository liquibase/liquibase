package liquibase

import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class GlobalConfigurationTest extends Specification {

    @Unroll
    def "file_encoding"() {
        expect:
        GlobalConfiguration.FILE_ENCODING.valueConverter.convert(input) == expected

        where:
        input      | expected
        "UTF-8"    | StandardCharsets.UTF_8
        "utf-8"    | StandardCharsets.UTF_8
        "utf-16"   | StandardCharsets.UTF_16
        "UTF-16BE" | StandardCharsets.UTF_16BE
        "UTF-16lE" | StandardCharsets.UTF_16LE
        "ascii"    | StandardCharsets.US_ASCII
        "us-ASCII" | StandardCharsets.US_ASCII
        "os"       | Charset.defaultCharset()
        null       | StandardCharsets.UTF_8
    }
}
