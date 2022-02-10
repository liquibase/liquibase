package liquibase.sqlgenerator.core;

import liquibase.database.core.MSSQLDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.CreateProcedureGenerator.MssqlSplitStatements;
import liquibase.statement.core.CreateProcedureStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class CreateProcedureGeneratorJunitTest {

    private CreateProcedureGenerator generator;

    @Before
    public void setUp() {
        generator = new CreateProcedureGenerator();
    }

    @Test
    public void generateSql_mssqlWithSetStatements_addsSetStatementsInProperOrder() {
        String functionBody = "" +
                "SET ANSI_NULLS OFF\n" +
                "SET QUOTED_IDENTIFIER OFF\n" +
                "CREATE PROCEDURE [dbo].[SqlTest01]\n" +
                "   AS\n" +
                "   BEGIN\n" +
                "SET ANSI_NULLS ON;\n" +
                "SET QUOTED_IDENTIFIER ON;\n" +
                "       SELECT \"Amy\";\n" +
                "SET ANSI_NULLS OFF;\n" +
                "SET QUOTED_IDENTIFIER OFF;\n" +
                "   END;";

        CreateProcedureStatement statement =
                new CreateProcedureStatement("", "dbo", "SqlTest01", functionBody, ";");
        statement.setReplaceIfExists(true);

        Sql[] sqls = generator.generateSql(statement, new MSSQLDatabase(), new SqlGeneratorChain(new TreeSet<>()));


        assertEquals(6, sqls.length);
        assertEquals("if object_id('[dbo].[SqlTest01]', 'p') is null exec ('create procedure [dbo].[SqlTest01] as select 1 a')", sqls[0].toSql());
        assertEquals("SET ANSI_NULLS ON", sqls[1].toSql());
        assertEquals("SET QUOTED_IDENTIFIER ON", sqls[2].toSql());
        assertEquals("SET ANSI_NULLS OFF", sqls[3].toSql());
        assertEquals("SET QUOTED_IDENTIFIER OFF", sqls[4].toSql());
        assertEquals(
                "ALTER PROCEDURE [dbo].[SqlTest01]\n" +
                        "   AS\n" +
                        "   BEGIN\n" +
                        "SET ANSI_NULLS ON;\n" +
                        "SET QUOTED_IDENTIFIER ON;\n" +
                        "       SELECT \"Amy\";\n" +
                        "SET ANSI_NULLS OFF;\n" +
                        "SET QUOTED_IDENTIFIER OFF;\n" +
                        "   END",
                sqls[5].toSql());
    }

    @Test
    public void moveSetStatementsOut_setStatementsOutOfFuncBodyWithoutSemicolons_splitsSetStatements() {
        String functionWithSetStatementsOutOfBodyWithSemicolon = "" +
                "SET ANSI_NULLS OFF\n" +
                "SET QUOTED_IDENTIFIER OFF\n" +
                "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                "  BEGIN\n" +
                "  DECLARE @greeting VARCHAR\n" +
                "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                "  RETURN @greeting\n" +
                "  END;";

        MssqlSplitStatements mssqlSplitStatements = CreateProcedureGenerator
                .splitSetStatementsOutForMssql(functionWithSetStatementsOutOfBodyWithSemicolon, ";", Collections.singletonList("FUNCTION"));

        assertEquals(4, mssqlSplitStatements.getSetStatementsBefore().size());
        assertEquals("SET ANSI_NULLS ON", mssqlSplitStatements.getSetStatementsBefore().get(0));
        assertEquals("SET QUOTED_IDENTIFIER ON", mssqlSplitStatements.getSetStatementsBefore().get(1));
        assertEquals("SET ANSI_NULLS OFF", mssqlSplitStatements.getSetStatementsBefore().get(2));
        assertEquals("SET QUOTED_IDENTIFIER OFF", mssqlSplitStatements.getSetStatementsBefore().get(3));
        assertEquals(
                "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                        "  BEGIN\n" +
                        "  DECLARE @greeting VARCHAR\n" +
                        "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                        "  RETURN @greeting\n" +
                        "  END;",
                mssqlSplitStatements.getBody());
    }

    @Test
    public void moveSetStatementsOut_setStatementsOutOfFuncBodyWithSemicolons_splitsSetStatements() {
        String functionWithSetStatementsOutOfBodyWithoutSemicolon = "" +
                "SET ANSI_NULLS OFF;\n" +
                "SET QUOTED_IDENTIFIER OFF;\n" +
                "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                "  BEGIN\n" +
                "  DECLARE @greeting VARCHAR\n" +
                "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                "  RETURN @greeting\n" +
                "  END;";

        MssqlSplitStatements mssqlSplitStatements = CreateProcedureGenerator
                .splitSetStatementsOutForMssql(functionWithSetStatementsOutOfBodyWithoutSemicolon, ";", Collections.singletonList("FUNCTION"));

        assertEquals(4, mssqlSplitStatements.getSetStatementsBefore().size());
        assertEquals("SET ANSI_NULLS ON", mssqlSplitStatements.getSetStatementsBefore().get(0));
        assertEquals("SET QUOTED_IDENTIFIER ON", mssqlSplitStatements.getSetStatementsBefore().get(1));
        assertEquals("SET ANSI_NULLS OFF", mssqlSplitStatements.getSetStatementsBefore().get(2));
        assertEquals("SET QUOTED_IDENTIFIER OFF", mssqlSplitStatements.getSetStatementsBefore().get(3));
        assertEquals(
                "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                        "  BEGIN\n" +
                        "  DECLARE @greeting VARCHAR\n" +
                        "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                        "  RETURN @greeting\n" +
                        "  END;",
                mssqlSplitStatements.getBody());
    }

    @Test
    public void moveSetStatementsOut_setStatementsInsideOfFuncBodyWithSemicolons_doesNotSplitSetStatements() {
        String functionWithSetStatementsInsideOfBodyWithSemicolon = "" +
                "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                "  BEGIN\n" +
                "SET ANSI_NULLS OFF;\n" +
                "SET QUOTED_IDENTIFIER OFF;\n" +
                "  DECLARE @greeting VARCHAR\n" +
                "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                "  RETURN @greeting\n" +
                "SET ANSI_NULLS ON;\n" +
                "SET QUOTED_IDENTIFIER ON;\n" +
                "  END;";

        MssqlSplitStatements mssqlSplitStatements = CreateProcedureGenerator
                .splitSetStatementsOutForMssql(functionWithSetStatementsInsideOfBodyWithSemicolon, ";", Collections.singletonList("FUNCTION"));

        assertEquals(2, mssqlSplitStatements.getSetStatementsBefore().size());
        assertEquals("SET ANSI_NULLS ON", mssqlSplitStatements.getSetStatementsBefore().get(0));
        assertEquals("SET QUOTED_IDENTIFIER ON", mssqlSplitStatements.getSetStatementsBefore().get(1));
        assertEquals(
                "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                        "  BEGIN\n" +
                        "SET ANSI_NULLS OFF;\n" +
                        "SET QUOTED_IDENTIFIER OFF;\n" +
                        "  DECLARE @greeting VARCHAR\n" +
                        "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                        "  RETURN @greeting\n" +
                        "SET ANSI_NULLS ON;\n" +
                        "SET QUOTED_IDENTIFIER ON;\n" +
                        "  END;",
                mssqlSplitStatements.getBody());
    }

    @Test
    public void moveSetStatementsOut_setStatementsInsideOfFuncBodyWithoutSemicolons_doesNotSplitSetStatements() {
        String functionWithSetStatementsInsideOfBodyWithoutSemicolon = "" +
                "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                "  BEGIN\n" +
                "SET ANSI_NULLS OFF\n" +
                "SET QUOTED_IDENTIFIER OFF\n" +
                "  DECLARE @greeting VARCHAR\n" +
                "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                "  RETURN @greeting\n" +
                "SET ANSI_NULLS ON\n" +
                "SET QUOTED_IDENTIFIER ON\n" +
                "  END;";

        MssqlSplitStatements mssqlSplitStatements = CreateProcedureGenerator
                .splitSetStatementsOutForMssql(functionWithSetStatementsInsideOfBodyWithoutSemicolon, ";", Collections.singletonList("FUNCTION"));

        assertEquals(2, mssqlSplitStatements.getSetStatementsBefore().size());
        assertEquals("SET ANSI_NULLS ON", mssqlSplitStatements.getSetStatementsBefore().get(0));
        assertEquals("SET QUOTED_IDENTIFIER ON", mssqlSplitStatements.getSetStatementsBefore().get(1));
        assertEquals(
                "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                        "  BEGIN\n" +
                        "SET ANSI_NULLS OFF\n" +
                        "SET QUOTED_IDENTIFIER OFF\n" +
                        "  DECLARE @greeting VARCHAR\n" +
                        "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                        "  RETURN @greeting\n" +
                        "SET ANSI_NULLS ON\n" +
                        "SET QUOTED_IDENTIFIER ON\n" +
                        "  END;",
                mssqlSplitStatements.getBody());
    }

    @Test
    public void moveSetStatementsOut_noSetStatements_doesNotSplitAnything() {
        String functionWithoutSetStatements = "" +
                "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                "  BEGIN\n" +
                "  DECLARE @greeting VARCHAR\n" +
                "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                "  RETURN @greeting\n" +
                "  END;";

        MssqlSplitStatements mssqlSplitStatements = CreateProcedureGenerator
                .splitSetStatementsOutForMssql(functionWithoutSetStatements, ";", Collections.singletonList("FUNCTION"));

        assertEquals(2, mssqlSplitStatements.getSetStatementsBefore().size());
        assertEquals("SET ANSI_NULLS ON", mssqlSplitStatements.getSetStatementsBefore().get(0));
        assertEquals("SET QUOTED_IDENTIFIER ON", mssqlSplitStatements.getSetStatementsBefore().get(1));
        assertEquals(
                "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                        "  BEGIN\n" +
                        "  DECLARE @greeting VARCHAR\n" +
                        "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                        "  RETURN @greeting\n" +
                        "  END;",
                mssqlSplitStatements.getBody());
    }

    @Test
    public void moveSetStatementsOut_setStatementWhichIsNotRequiredToBeSplitOut_splitsOnlyRequiredSetStatements() {
        String functionWithSetStatementWhichIsNotRequiredToBeSplitOut = "" +
                "SET ANSI_NULLS OFF;\n" +
                "SET DATEFIRST 7;\n" +
                "SET QUOTED_IDENTIFIER OFF;\n" +
                "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                "  BEGIN\n" +
                "  DECLARE @greeting VARCHAR\n" +
                "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                "  RETURN @greeting\n" +
                "  END;";

        MssqlSplitStatements mssqlSplitStatements = CreateProcedureGenerator
                .splitSetStatementsOutForMssql(functionWithSetStatementWhichIsNotRequiredToBeSplitOut, ";", Collections.singletonList("FUNCTION"));

        assertEquals(4, mssqlSplitStatements.getSetStatementsBefore().size());
        assertEquals("SET ANSI_NULLS ON", mssqlSplitStatements.getSetStatementsBefore().get(0));
        assertEquals("SET QUOTED_IDENTIFIER ON", mssqlSplitStatements.getSetStatementsBefore().get(1));
        assertEquals("SET ANSI_NULLS OFF", mssqlSplitStatements.getSetStatementsBefore().get(2));
        assertEquals("SET QUOTED_IDENTIFIER OFF", mssqlSplitStatements.getSetStatementsBefore().get(3));
        assertEquals(
                "SET DATEFIRST 7;\n" +
                        "  \n" +
                        "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                        "  BEGIN\n" +
                        "  DECLARE @greeting VARCHAR\n" +
                        "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                        "  RETURN @greeting\n" +
                        "  END;",
                mssqlSplitStatements.getBody());
    }

    @Test
    public void moveSetStatementsOut_containingBeginWordInComment_doesNotCountItAsFunctionStart() {
        String functionWithSetStatementWhichIsNotRequiredToBeSplitOut = "" +
                "-- Begin creation of new function comment\n" +
                "SET ANSI_NULLS OFF;\n" +
                "SET QUOTED_IDENTIFIER OFF;\n" +
                "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                "  BEGIN\n" +
                "  DECLARE @greeting VARCHAR\n" +
                "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                "  RETURN @greeting\n" +
                "  END;";

        MssqlSplitStatements mssqlSplitStatements = CreateProcedureGenerator
                .splitSetStatementsOutForMssql(functionWithSetStatementWhichIsNotRequiredToBeSplitOut, ";", Collections.singletonList("FUNCTION"));

        assertEquals(4, mssqlSplitStatements.getSetStatementsBefore().size());
        assertEquals("SET ANSI_NULLS ON", mssqlSplitStatements.getSetStatementsBefore().get(0));
        assertEquals("SET QUOTED_IDENTIFIER ON", mssqlSplitStatements.getSetStatementsBefore().get(1));
        assertEquals("SET ANSI_NULLS OFF", mssqlSplitStatements.getSetStatementsBefore().get(2));
        assertEquals("SET QUOTED_IDENTIFIER OFF", mssqlSplitStatements.getSetStatementsBefore().get(3));
        assertEquals(
                "-- Begin creation of new function comment\n" +
                        "  \n" +
                        "  \n" +
                        "CREATE FUNCTION [dbo].some_function() RETURNS VARCHAR(100) AS\n" +
                        "  BEGIN\n" +
                        "  DECLARE @greeting VARCHAR\n" +
                        "  SET @greeting=''Hi! I am a MSSQL function!''\n" +
                        "  RETURN @greeting\n" +
                        "  END;",
                mssqlSplitStatements.getBody());
    }
}
