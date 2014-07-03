package liquibase.statement.core

import liquibase.statement.AbstractStatementTest

class RawDatabaseCommandStatementTest extends AbstractStatementTest {

    def "constructor"() {
        when:
        def empty = new RawDatabaseCommandStatement()
        def commandOnly = new RawDatabaseCommandStatement("COMMAND HERE")
        def commandAndDelimiter = new RawDatabaseCommandStatement("COMMAND HERE", "END DELIM")

        then:
        empty.command == null
        empty.endDelimiter == ";"

        commandOnly.command == "COMMAND HERE"
        commandOnly.endDelimiter == ";"

        commandAndDelimiter.command == "COMMAND HERE"
        commandAndDelimiter.endDelimiter == "END DELIM"
    }

    def "getDelimiter"() {
        when:
        def statement = new RawDatabaseCommandStatement()
        statement.setEndDelimiter(passed)

        then:
        statement.getEndDelimiter() == expected

        where:
        passed                                   | expected
        null                                     | ";"
        ""                                       | ""
        "end delim"                              | "end delim"
        "one \r with \n nested chars \r here"    | "one \r with \n nested chars \r here"
        "one \\r with \\n nested chars \\r here" | "one \r with \n nested chars \r here"
    }

    @Override
    protected Object getDefaultPropertyValue(String propertyName) {
        if (propertyName == "endDelimiter") {
            return ";"
        }
        return super.getDefaultPropertyValue(propertyName)
    }

    @Override
    protected boolean expectAtLeastOneGenerator() {
        return false
    }
}
