package liquibase.sqlgenerator.core

import liquibase.database.Database
import liquibase.database.core.MSSQLDatabase
import liquibase.sql.Sql
import liquibase.sqlgenerator.SqlGeneratorChain
import liquibase.statement.core.CreateProcedureStatement
import spock.lang.Specification
import spock.lang.Unroll

class CreateProcedureGeneratorTest extends Specification {

    private static final String DEFAULT_SET_ANSI_STATEMENT = "SET ANSI_NULLS ON"
    private static final String DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT = "SET QUOTED_IDENTIFIER ON"

    @Unroll
    def "Verify CreateProcedureGenerator logic correctly generates create procedure sql statement based on given procedureText and replaceIfExists attributes for MSSQL"() {
        given:
        Database database = new MSSQLDatabase()
        SqlGeneratorChain chain = new SqlGeneratorChain(new TreeSet<>())
        CreateProcedureGenerator generator = new CreateProcedureGenerator()
        CreateProcedureStatement statement = new CreateProcedureStatement(null, schemaName, procedureName, procedureText, ";")
        statement.setReplaceIfExists(reaplceIfExists)

        when:
        Sql[] generatedSql = generator.generateSql(statement, database, chain)

        then:
        generatedSql.size() == expectedSql.size()
        generatedSql.collect { it.toSql() } == expectedSql

        where:
        procedureText                                                                                                                         | schemaName | procedureName       | reaplceIfExists | expectedSql
        "CREATE PROCEDURE [dbo].[procedure_1]"                                                                                                | "dbo"      | "procedure_1"       | true            | ["if object_id('[dbo].[procedure_1]', 'p') is null exec ('create procedure [dbo].[procedure_1] as select 1 a')", "ALTER PROCEDURE [dbo].[procedure_1]", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "CREATE PROCEDURE [dbo].[procedure_1] AS CREATE OR ALTER PROCEDURE dbo.inner_procedure"                                               | "dbo"      | "procedure_1"       | true            | ["if object_id('[dbo].[procedure_1]', 'p') is null exec ('create procedure [dbo].[procedure_1] as select 1 a')", "ALTER PROCEDURE [dbo].[procedure_1] AS CREATE OR ALTER PROCEDURE dbo.inner_procedure", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "CREATE PROCEDURE [dbo].[procedure_1] AS CREATE PROCEDURE dbo.inner_procedure"                                                        | "dbo"      | "procedure_1"       | true            | ["if object_id('[dbo].[procedure_1]', 'p') is null exec ('create procedure [dbo].[procedure_1] as select 1 a')", "ALTER PROCEDURE [dbo].[procedure_1] AS CREATE PROCEDURE dbo.inner_procedure", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "CREATE PROCEDURE [dbo].[procedure_1]"                                                                                                | "dbo"      | "procedure_1"       | false           | ["CREATE PROCEDURE [dbo].[procedure_1]", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "CREATE OR ALTER PROCEDURE [dbo].[procedure_1]"                                                                                       | "dbo"      | "procedure_1"       | true            | ["if object_id('[dbo].[procedure_1]', 'p') is null exec ('create procedure [dbo].[procedure_1] as select 1 a')", "CREATE OR ALTER PROCEDURE [dbo].[procedure_1]", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "CREATE OR ALTER PROCEDURE [dbo].[procedure_1] AS CREATE OR ALTER PROCEDURE dbo.inner_procedure"                                      | "dbo"      | "procedure_1"       | true            | ["if object_id('[dbo].[procedure_1]', 'p') is null exec ('create procedure [dbo].[procedure_1] as select 1 a')", "CREATE OR ALTER PROCEDURE [dbo].[procedure_1] AS CREATE OR ALTER PROCEDURE dbo.inner_procedure", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "CREATE OR ALTER PROCEDURE [dbo].[procedure_1] AS CREATE PROCEDURE dbo.inner_procedure"                                               | "dbo"      | "procedure_1"       | true            | ["if object_id('[dbo].[procedure_1]', 'p') is null exec ('create procedure [dbo].[procedure_1] as select 1 a')", "CREATE OR ALTER PROCEDURE [dbo].[procedure_1] AS CREATE PROCEDURE dbo.inner_procedure", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "CREATE OR ALTER PROCEDURE [dbo].[procedure_1]"                                                                                       | "dbo"      | "procedure_1"       | false           | ["CREATE OR ALTER PROCEDURE [dbo].[procedure_1]", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        // We can deploy ALTER PROCEDURE ... statement using 'createProcedure' change type
        "ALTER PROCEDURE [dbo].[procedure_1]"                                                                                                 | "dbo"      | "procedure_1"       | true            | ["if object_id('[dbo].[procedure_1]', 'p') is null exec ('create procedure [dbo].[procedure_1] as select 1 a')", "ALTER PROCEDURE [dbo].[procedure_1]", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "ALTER PROCEDURE [dbo].[procedure_1]"                                                                                                 | "dbo"      | "procedure_1"       | false           | ["ALTER PROCEDURE [dbo].[procedure_1]", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
        "ALTER PROCEDURE [dbo].create_table_proc @TableName NVARCHAR(128) AS BEGIN CREATE TABLE #tmpReporte(NroPreorden\tint\tNOT NULL); END" | "dbo"      | "create_table_proc" | true            | ["if object_id('[dbo].[create_table_proc]', 'p') is null exec ('create procedure [dbo].[create_table_proc] as select 1 a')", "ALTER PROCEDURE [dbo].create_table_proc @TableName NVARCHAR(128) AS BEGIN CREATE TABLE #tmpReporte(NroPreorden\tint\tNOT NULL); END", DEFAULT_SET_ANSI_STATEMENT, DEFAULT_SET_QUOTED_IDENTIFIER_STATEMENT]
    }

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
}
