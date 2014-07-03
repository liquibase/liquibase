package liquibase.statement.core

import liquibase.statement.AbstractStatementTest
import spock.lang.Specification

class RawSqlStatementTest extends AbstractStatementTest {

    def "constructor"() {
        when:
        def empty = new RawSqlStatement()
        def sqlOnly = new RawSqlStatement("SQL HERE")
        def sqlAndDelimiter = new RawSqlStatement("SQL HERE", "END DELIM")

        then:
        empty.sql == null
        empty.endDelimiter == ";"

        sqlOnly.sql == "SQL HERE"
        sqlOnly.endDelimiter == ";"

        sqlAndDelimiter.sql == "SQL HERE"
        sqlAndDelimiter.endDelimiter == "END DELIM"
    }

    def "getDelimiter"() {
        when:
        def statement = new RawSqlStatement()
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
