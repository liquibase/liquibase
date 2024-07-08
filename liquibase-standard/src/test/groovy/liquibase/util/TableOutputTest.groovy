package liquibase.util

import liquibase.Scope
import liquibase.ui.ConsoleUIService
import spock.lang.Specification

class TableOutputTest extends Specification {
    def "Table Output"() {
         when:
         ConsoleUIService console = Scope.getCurrentScope().getUI() as ConsoleUIService
         def outputStream = new ByteArrayOutputStream()
         console.setOutputStream(new PrintStream(outputStream))
         TableOutput.formatOutput(table as String[][], maxWidths as List<Integer>, leftJustified, new OutputStreamWriter(outputStream))

         then:
         outputStream.toString().trim().replaceAll("\r", "") == expected

         where:
         table                  | maxWidths     | leftJustified | expected
         [["data1","data2"]]    | [30,30]       | true          | "+-------+-------+\n| data1 | data2 |\n+-------+-------+"
         [["really_really_really_really_really_really_really_really_really_long_string"+System.lineSeparator()+"anotherString","data2"]]    | [30,30]       | true          |
"""+--------------------------------+-------+
| really_really_really_really_re | data2 |
| ally_really_really_really_real |       |
| ly_long_string                 |       |
| anotherString                  |       |
+--------------------------------+-------+"""
         [["data1","Alerts when a changeset ID does not follow the 8-4-4-4-12 pattern of UUID or GUID."]]    | [30,30]       | true          |
"""+-------+--------------------------------+
| data1 | Alerts when a changeset ID     |
|       | does not follow the 8-4-4-4-12 |
|       | pattern of UUID or GUID.       |
+-------+--------------------------------+"""
    }

    def "Data array length must match widths"() {
        when:
        ConsoleUIService console = Scope.getCurrentScope().getUI() as ConsoleUIService
        def outputStream = new ByteArrayOutputStream()
        console.setOutputStream(new PrintStream(outputStream))
        TableOutput.formatOutput(table as String[][], maxWidths as List<Integer>, leftJustified, new OutputStreamWriter(outputStream))

        then:
        thrown RuntimeException

        where:
        table                  | maxWidths     | leftJustified | expected
        [["data1","data2"]]    | [30]          | true          | "+-------+-------+\n| data1 | data2 |\n+-------+-------+"
    }

    def "Computes max width for each table column"() {
        given:
        def data = [
                ["a", "bb", "ccc"],
                ["aaa", "b", "cc"],
        ]

        when:
        def result = TableOutput.computeMaxWidths(data)

        then:
        result == [3, 2, 3]
    }

    def "Computes no widths if table is empty"() {
        given:
        def data = []

        when:
        def result = TableOutput.computeMaxWidths(data)

        then:
        result.isEmpty()
    }

    def "Fails to compute max if column count differs between rows"() {
        given:
        def data = [
                ["a", "bb", "ccc"],
                ["aaa", "b", "cc", "oopsie"],
        ]

        when:
        def result = TableOutput.computeMaxWidths(data)

        then:
        def exception = thrown(RuntimeException)
        exception.message == "could not compute table width: heterogeneous tables are not supported. Expected each row to have 3 column(s), found 4"
    }
}
