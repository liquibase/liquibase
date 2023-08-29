package liquibase.database.core

import liquibase.structure.core.Column
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.database.ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
import static liquibase.database.ObjectQuotingStrategy.QUOTE_ONLY_RESERVED_WORDS

class MariaDBDatabaseSpec extends Specification {

    @Unroll("#featureName [#quotingStrategy], [#objectName, #objectType], [#expectedCorrect, #expectedEscape]")
    def "correctObjectName, escapeObjectName"() {
        given:
        def database = new MariaDBDatabase().tap {
            if (quotingStrategy != null) {
                it.objectQuotingStrategy = quotingStrategy
            }
        }

        expect:
        verifyAll {
            database.correctObjectName(objectName, objectType) == expectedCorrect
            database.escapeObjectName(objectName, objectType) == expectedEscape
        }

        where:
        quotingStrategy           || objectName       | objectType || expectedCorrect  | expectedEscape
        null                      || 'col1'           | Column     || 'col1'           | 'col1'
        null                      || 'COL1'           | Column     || 'col1'           | 'COL1'
        null                      || 'Col1'           | Column     || 'col1'           | 'Col1'
        null                      || 'col with space' | Column     || 'col with space' | '`col with space`'
        null                      || 'period'         | Column     || 'period'         | '`period`'
        null                      || 'PERIOD'         | Column     || 'period'         | '`PERIOD`'
        QUOTE_ONLY_RESERVED_WORDS || 'col1'           | Column     || 'col1'           | 'col1'
        QUOTE_ONLY_RESERVED_WORDS || 'COL1'           | Column     || 'col1'           | 'COL1'
        QUOTE_ONLY_RESERVED_WORDS || 'Col1'           | Column     || 'col1'           | 'Col1'
        QUOTE_ONLY_RESERVED_WORDS || 'col with space' | Column     || 'col with space' | '`col with space`'
        QUOTE_ONLY_RESERVED_WORDS || 'period'         | Column     || 'period'         | '`period`'
        QUOTE_ONLY_RESERVED_WORDS || 'PERIOD'         | Column     || 'period'         | '`PERIOD`'
        QUOTE_ALL_OBJECTS         || 'col1'           | Column     || 'col1'           | '`col1`'
        QUOTE_ALL_OBJECTS         || 'COL1'           | Column     || 'col1'           | '`COL1`'
        QUOTE_ALL_OBJECTS         || 'Col1'           | Column     || 'col1'           | '`Col1`'
        QUOTE_ALL_OBJECTS         || 'col with space' | Column     || 'col with space' | '`col with space`'
        QUOTE_ALL_OBJECTS         || 'period'         | Column     || 'period'         | '`period`'
        QUOTE_ALL_OBJECTS         || 'PERIOD'         | Column     || 'period'         | '`PERIOD`'
    }
}
