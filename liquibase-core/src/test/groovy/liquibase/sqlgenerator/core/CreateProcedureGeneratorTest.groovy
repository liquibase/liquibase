package liquibase.sqlgenerator.core

import spock.lang.Specification
import spock.lang.Unroll

class CreateProcedureGeneratorTest extends Specification {

    @Unroll
    def "removeTrailingDelimiter"() {
        expect:
        CreateProcedureGenerator.removeTrailingDelimiter(text, delimiter) == expected

        where:
        text                               | delimiter | expected
        null                               | ";"       | null
        ""                                 | ";"       | ""
        "null delimiter;"                  | null      | "null delimiter;"
        "no delimiter"                     | ";"       | "no delimiter"
        "no delimiter"                     | "\n/"     | "no delimiter"
        "with delimiter\n/"                | "\n/"     | "with delimiter"
        "with delimiter\n/\n   \n \n"      | "\n/"     | "with delimiter"
        "other stuff\n/and more"           | "\n/"     | "other stuff\n/and more"
        "semicolon delimiter;"             | ";"       | "semicolon delimiter"
        "semicolon delimiter\n\n;\n\r  \n" | ";"       | "semicolon delimiter\n\n"
        "mid-semicolon;delimiter"          | ";"       | "mid-semicolon;delimiter"
        "mid-semicolon;delimiter;"         | ";"       | "mid-semicolon;delimiter"
    }
}
