package liquibase.database.core

import liquibase.structure.core.Column
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.database.ObjectQuotingStrategy.QUOTE_ALL_OBJECTS
import static liquibase.database.ObjectQuotingStrategy.QUOTE_ONLY_RESERVED_WORDS

class OracleDatabaseSpec extends Specification {

    @Unroll("#featureName [#quotingStrategy], [#objectName, #objectType], [#expectedCorrect, #expectedEscape]")
    def "correctObjectName, escapeObjectName"() {
        given:
        def database = new OracleDatabase().tap {
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
        quotingStrategy           || objectName              | objectType || expectedCorrect         | expectedEscape
        null                      || 'col1'                  | Column     || 'COL1'                  | 'col1'
        null                      || 'COL1'                  | Column     || 'COL1'                  | 'COL1'
        null                      || 'Col1'                  | Column     || 'COL1'                  | 'Col1'
        null                      || 'col with space'        | Column     || 'COL WITH SPACE'        | '"col with space"'
        QUOTE_ONLY_RESERVED_WORDS || 'col1'                  | Column     || 'COL1'                  | 'col1'
        QUOTE_ONLY_RESERVED_WORDS || 'COL1'                  | Column     || 'COL1'                  | 'COL1'
        QUOTE_ONLY_RESERVED_WORDS || 'Col1'                  | Column     || 'COL1'                  | 'Col1'
        QUOTE_ONLY_RESERVED_WORDS || 'col with space'        | Column     || 'COL WITH SPACE'        | '"col with space"'
        QUOTE_ALL_OBJECTS         || 'col1'                  | Column     || 'col1'                  | '"col1"'
        QUOTE_ALL_OBJECTS         || 'COL1'                  | Column     || 'COL1'                  | '"COL1"'
        QUOTE_ALL_OBJECTS         || 'Col1'                  | Column     || 'Col1'                  | '"Col1"'
        QUOTE_ALL_OBJECTS         || 'col with space'        | Column     || 'col with space'        | '"col with space"'
        // #7018: "int"/"integer" type names should map to NUMBER(*, 0)
        null                      || 'int'                   | Column     || 'NUMBER(*, 0)'          | 'int'
        null                      || 'integer'               | Column     || 'NUMBER(*, 0)'          | 'integer'
        null                      || 'INT'                   | Column     || 'NUMBER(*, 0)'          | 'INT'
        null                      || 'INTEGER'               | Column     || 'NUMBER(*, 0)'          | 'INTEGER'
        // #7018: column names starting with "int" must NOT be converted
        null                      || 'internalPhoneNumber'   | Column     || 'INTERNALPHONENUMBER'   | 'internalPhoneNumber'
        null                      || 'internal_payload_id'   | Column     || 'INTERNAL_PAYLOAD_ID'   | 'internal_payload_id'
        null                      || 'integration_type'      | Column     || 'INTEGRATION_TYPE'      | 'integration_type'
    }
}
