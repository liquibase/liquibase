package liquibase.sqlgenerator.core

import liquibase.configuration.LiquibaseConfiguration
import liquibase.database.core.MSSQLDatabase
import liquibase.database.core.OracleDatabase
import liquibase.parser.ChangeLogParserCofiguration
import liquibase.sqlgenerator.SqlGeneratorFactory
import liquibase.statement.core.CreateProcedureStatement
import spock.lang.Specification
import spock.lang.Unroll

class CreateProcedureGeneratorTest extends Specification {

    @Unroll
    def "removeTrailingDelimiter"() {
        expect:
        CreateProcedureGenerator.removeTrailingDelimiter(text, delimiter) == expected

        where:
        text                                                                                                                                                | delimiter | expected
        null                                                                                                                                                | ";"       | null
        ""                                                                                                                                                  | ";"       | ""
        "null delimiter;"                                                                                                                                   | null      | "null delimiter;"
        "no delimiter"                                                                                                                                      | ";"       | "no delimiter"
        "no delimiter"                                                                                                                                      | "\n/"     | "no delimiter"
        "with delimiter\n/"                                                                                                                                 | "\n/"     | "with delimiter"
        "with delimiter\n/\n   \n \n"                                                                                                                       | "\n/"     | "with delimiter"
        "with delimiter\n/\n   \n \n"                                                                                                                       | "/"       | "with delimiter\n"
        "other stuff\n/and more"                                                                                                                            | "\n/"     | "other stuff\n/and more"
        "semicolon delimiter;"                                                                                                                              | ";"       | "semicolon delimiter"
        "semicolon delimiter\n\n;\n\r  \n"                                                                                                                  | ";"       | "semicolon delimiter\n\n"
        "mid-semicolon;delimiter"                                                                                                                           | ";"       | "mid-semicolon;delimiter"
        "mid-semicolon;delimiter;"                                                                                                                          | ";"       | "mid-semicolon;delimiter"
        "no delimiter -- some comments"                                                                                                                     | "/"       | "no delimiter -- some comments"
        "no delimiter\n -- some comments"                                                                                                                   | "/"       | "no delimiter\n -- some comments"
        "with delimiter;\n/ \n-- some comments"                                                                                                             | "/"       | "with delimiter;\n"
        "with delimiter;\n/ \n/** some block comments **/ "                                                                                                 | "/"       | "with delimiter;\n"
        "with delimiter;\n/ \n/**\n some \nblock comments\n **/ "                                                                                           | "/"       | "with delimiter;\n"
        // no delimiter
        "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;--last comment"                                                   | "/"       | "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;--last comment"
        "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;"                                                                 | "/"       | "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;"
        "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;--last comment\n/*****another\nblock\n***/"                       | "/"       | "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;--last comment\n/*****another\nblock\n***/"
        "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;\t--last comment\n\n\n\t--another\n/*****another\nblock\n***/"    | "/"       | "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;\t--last comment\n\n\n\t--another\n/*****another\nblock\n***/"
        // with delimiter
        "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;\n/"                                                              | "/"       | "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;\n"
        "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;\n/--last comment"                                                | "/"       | "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;\n"
        "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;\n/\n--last comment\n/*****another\nblock\n***/"                  | "/"       | "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;\n"
        "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;\n/\t--last comment\n\n\n\t--another\n/*****another\nblock\n***/" | "/"       | "some txt; \n-- line cmt \n--another \n /* and block\\/ */\\t\n\ndefine something;\n"

    }

    @Unroll
    def "addSchemaToText for databases"() {
      when:
      String sql = CreateProcedureGenerator.addSchemaToText(body, "MYSCHEMA", "PROCEDURE", database)
      then:
      sql != null && sql.contains(expectedSchema)

      where:
      [database, body, expectedSchema] <<
      [[new MSSQLDatabase(),
      """
      CREATE PROCEDURE [procPrintHelloWorld] AS
      BEGIN
      PRINT N'Hello, World! I am a MSSQL procedure.'
      END
       """, "MYSCHEMA.[procPrintHelloWorld]"],
       [new OracleDatabase(),
       """
        CREATE OR REPLACE PACKAGE PKG1 AS
        PROCEDURE add_test (col1_in NUMBER, col2_in CHAR);
        PROCEDURE del_test (col1_in NUMBER);
        END PKG1;
        """,
        "PACKAGE PKG1"
      ]]
    }
}
