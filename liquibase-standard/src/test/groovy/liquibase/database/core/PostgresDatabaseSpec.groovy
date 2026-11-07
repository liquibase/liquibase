package liquibase.database.core

import liquibase.structure.core.Column
import liquibase.structure.core.Index
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.database.ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
import static liquibase.database.ObjectQuotingStrategy.QUOTE_ONLY_RESERVED_WORDS

class PostgresDatabaseSpec extends Specification {

    @Unroll("#featureName [#quotingStrategy], [#objectName, #objectType], [#expectedCorrect, #expectedEscape]")
    def "correctObjectName, escapeObjectName"() {
        given:
        def database = new PostgresDatabase().tap {
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
        null                      || 'Col1'           | Column     || 'Col1'           | '"Col1"'
        null                      || 'col with space' | Column     || 'col with space' | '"col with space"'
        null                      || 'COL WITH SPACE' | Column     || 'col with space' | '"COL WITH SPACE"'
        null                      || 'Col with space' | Column     || 'Col with space' | '"Col with space"'
        QUOTE_ONLY_RESERVED_WORDS || 'col1'           | Column     || 'col1'           | 'col1'
        QUOTE_ONLY_RESERVED_WORDS || 'COL1'           | Column     || 'col1'           | 'COL1'
        QUOTE_ONLY_RESERVED_WORDS || 'Col1'           | Column     || 'col1'           | 'Col1'
        QUOTE_ONLY_RESERVED_WORDS || 'col with space' | Column     || 'col with space' | '"col with space"'
        QUOTE_ONLY_RESERVED_WORDS || 'COL WITH SPACE' | Column     || 'col with space' | '"COL WITH SPACE"'
        QUOTE_ONLY_RESERVED_WORDS || 'Col with space' | Column     || 'col with space' | '"Col with space"'
        QUOTE_ALL_OBJECTS         || 'col1'           | Column     || 'col1'           | '"col1"'
        QUOTE_ALL_OBJECTS         || 'COL1'           | Column     || 'COL1'           | '"COL1"'
        QUOTE_ALL_OBJECTS         || 'Col1'           | Column     || 'Col1'           | '"Col1"'
        QUOTE_ALL_OBJECTS         || 'col with space' | Column     || 'col with space' | '"col with space"'
        QUOTE_ALL_OBJECTS         || 'COL WITH SPACE' | Column     || 'COL WITH SPACE' | '"COL WITH SPACE"'
        QUOTE_ALL_OBJECTS         || 'Col with space' | Column     || 'Col with space' | '"Col with space"'
        // #3876: QUOTE_ONLY_RESERVED_WORDS still folds index names, so idx-mixed finds Idx-Mixed.
        // LEGACY keeps mixed case (and hyphens) as written.
        null                      || 'Idx-Mixed'      | Index      || 'Idx-Mixed'      | '"Idx-Mixed"'
        QUOTE_ONLY_RESERVED_WORDS || 'Idx-Mixed'      | Index      || 'idx-mixed'      | '"Idx-Mixed"'
        QUOTE_ONLY_RESERVED_WORDS || 'idx-mixed'      | Index      || 'idx-mixed'      | '"idx-mixed"'
        QUOTE_ONLY_RESERVED_WORDS || 'IdxPlain'       | Index      || 'idxplain'       | 'IdxPlain'
    }
}
