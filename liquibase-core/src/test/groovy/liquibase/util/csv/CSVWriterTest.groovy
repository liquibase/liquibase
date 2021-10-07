package liquibase.util.csv

import spock.lang.Specification
import spock.lang.Unroll

class CSVWriterTest extends Specification {

    @Unroll
    def writeNext() {
        when:
        def out = new StringWriter()
        new CSVWriter(out).writeNext(input as String[])

        then:
        out.toString() == expected

        where:
        input           | expected
        ["a", 1, "abc"] | "\"a\",\"1\",\"abc\"\n"
        ["a", null, "abc"] | "\"a\",,\"abc\"\n"
        ["a", "", "abc"] | "\"a\",\"\",\"abc\"\n"
    }
}
