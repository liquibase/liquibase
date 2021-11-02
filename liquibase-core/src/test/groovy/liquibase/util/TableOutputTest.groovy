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
         TableOutput.formatOutput(table as String[][], maxWidths as int[], leftJustified, new OutputStreamWriter(outputStream))

         then:
         outputStream.toString().trim().replaceAll("\r", "") == expected

         where:
         table                  | maxWidths     | leftJustified | expected
         [["data1","data2"]]    | [30,30]       | true          | "+-------+-------+\n| data1 | data2 |\n+-------+-------+"
    }
    def "Data array length must match widths"() {
        when:
        ConsoleUIService console = Scope.getCurrentScope().getUI() as ConsoleUIService
        def outputStream = new ByteArrayOutputStream()
        console.setOutputStream(new PrintStream(outputStream))
        TableOutput.formatOutput(table as String[][], maxWidths as int[], leftJustified, new OutputStreamWriter(outputStream))

        then:
        thrown RuntimeException

        where:
        table                  | maxWidths     | leftJustified | expected
        [["data1","data2"]]    | [30]          | true          | "+-------+-------+\n| data1 | data2 |\n+-------+-------+"
    }
}
