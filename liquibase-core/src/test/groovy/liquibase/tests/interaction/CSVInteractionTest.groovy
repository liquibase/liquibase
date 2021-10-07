package liquibase.tests.interaction

import liquibase.util.csv.CSVReader
import liquibase.util.csv.CSVWriter
import spock.lang.Specification;

class CSVInteractionTest extends Specification {

    def "readers and writers are compatible"() {
        when:
        def data = [
                ["1", "Jane", "123 4th St"],
                ["2", "Joe", "888 Murray"],
        ]

        def out = new StringWriter()

        def writer = new CSVWriter(out)
        for (def line : data) {
            writer.writeNext(line as String[])
        }

        writer.flush()

        then:
        out.toString().contains("123 4th St")

        when:
        def input = new StringReader(out.toString())
        def reader = new CSVReader(input)

        def outData = []
        String[] line
        while ((line = reader.readNext()) != null) {
            outData.add(line)
        }

        then:
        outData == data
    }
}
