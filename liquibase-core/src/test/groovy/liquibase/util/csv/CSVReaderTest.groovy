package liquibase.util.csv

import spock.lang.Specification
import spock.lang.Unroll

class CSVReaderTest extends Specification {

    @Unroll
    def "readNext"() {
        when:
        def reader = new CSVReader(new StringReader("id${separator}name${separator}address\n" + input), separator as char, quote as char)

        then:
        reader.readNext() == ["id", "name", "address"] as String[]
        reader.readNext() == expected as String[]
        reader.readNext() == null

        where:
        separator                   | quote                             | input                                 | expected
        CSVReader.DEFAULT_SEPARATOR | CSVReader.DEFAULT_QUOTE_CHARACTER | "1,first name,123 4th st"             | ["1", "first name", "123 4th st"]
        CSVReader.DEFAULT_SEPARATOR | CSVReader.DEFAULT_QUOTE_CHARACTER | "\"1\",\"first name\",\"123 4th st\"" | ["1", "first name", "123 4th st"]
        "X"                         | CSVReader.DEFAULT_QUOTE_CHARACTER | "1Xfirst nameX123 4th st"             | ["1", "first name", "123 4th st"]
        CSVReader.DEFAULT_SEPARATOR | CSVReader.DEFAULT_QUOTE_CHARACTER | "1,,123 4th st"                       | ["1", "", "123 4th st"]
        CSVReader.DEFAULT_SEPARATOR | CSVReader.DEFAULT_QUOTE_CHARACTER | "1,null,123 4th st"                   | ["1", "null", "123 4th st"]
    }
}
