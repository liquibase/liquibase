package liquibase.sqlgenerator.core.util;

import liquibase.sql.Sql;
import liquibase.structure.core.View;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class SqlGeneratorMssqlUtilTest {

    private static final String DATA_WITH_TWO_SETS =
            "    SET QUOTED_IDENTIFIER ON;    \n" +
            "    SET ANSI_NULLS ON; \n" +
            "CREATE PROCEDURE [s1].[p10]  AS \n" +
            "BEGIN \n" +
            "\t  select * from [dbo].[t1] \n" +
            "END";

    private final static String PROCEDURE_WITH_SET_QUOTED_IDENTIFIER = "SET QUOTED_IDENTIFIER OFF; \n" +
            "CREATE PROCEDURE [dbo].[SqlTest01]" +
            "AS \n" +
            "BEGIN \n" +
            "SELECT \"Amy\";\n"+
            "END;\n";

    private static final String CREATE_PROCEDURE_DDL_WITH_USER_SETTINGS = "SET ANSI_NULLS ON\n" +
            "SET QUOTED_IDENTIFIER ON\n" +
            ";\n" +
            "\n" +
            "ALTER PROCEDURE [dbo].test_proc AS\n" +
            "BEGIN\n" +
            "PRINT N'Hello, World! I am a MSSQL procedure.'\n" +
            "END;'\n";

    private static final String LOWER_CASE_SET_STATEMENT= "set ansi_nulls on\n" +
            "set quoted_identifier on\n" +
            ";\n" +
            "\n" +
            "create procedure[dbo].test_proc AS\n" +
            "begin \n" +
            "PRINT N'Hello, World! I am a MSSQL procedure.'\n" +
            "end;'\n";

    private static final String SAME_LINE_STATEMENT = "SET ANSI_NULLS ON; CREATE \n" +
            "PROCEDURE [dbo].[test] AS \n" +
            "BEGIN \n" +
            "insert into [dbo].[hello] values(1) \n" +
            "END \n";

    private static final String CORRECT_ORDER_STATEMENTS = "SET ANSI_NULLS OFF\n" +
            "\n" +
            "SET QUOTED_IDENTIFIER OFF\n" +
            "\n" +
            "CREATE PROCEDURE [dbo].[SqlTest01]\n" +
            "  AS\n" +
            "    BEGIN\n" +
            "      SELECT \"Amy\";\n" +
            "  END;\n" +
            "\n" +
            "SET ANSI_NULLS ON\n" +
            "\n" +
            "SET QUOTED_IDENTIFIER ON\n";

    @Test
    public void addSqlStatementWithOneSet() {
        List<Sql> sqlList = new ArrayList<>();

        SqlGeneratorMssqlUtil.addSqlStatementsToList(sqlList, PROCEDURE_WITH_SET_QUOTED_IDENTIFIER, ";");

        assertEquals(sqlList.size(), 2);
        Assert.assertTrue(sqlList.get(0).toSql().contains("SET QUOTED_IDENTIFIER"));
        Assert.assertTrue(sqlList.get(1).toSql().contains("CREATE PROCEDURE"));
    }

    @Test
    public void addSqlStatementWithOneSetTestProc() {
        List<Sql> sqlList = new ArrayList<>();

        SqlGeneratorMssqlUtil.addSqlStatementsToList(sqlList, CREATE_PROCEDURE_DDL_WITH_USER_SETTINGS, ";");

        assertEquals(sqlList.size(), 3);
        Assert.assertTrue(sqlList.get(0).toSql().contains("SET"));
        Assert.assertTrue(sqlList.get(1).toSql().contains("SET"));
        Assert.assertTrue(sqlList.get(2).toSql().contains("ALTER"));
        Assert.assertFalse(sqlList.get(2).toSql().contains("SET"));
    }

    @Test
    public void addSqlStatementWithOneSetRelationFunction() {
        List<Sql> sqlList = new ArrayList<>();

        SqlGeneratorMssqlUtil.addSqlStatementsToList(sqlList, PROCEDURE_WITH_SET_QUOTED_IDENTIFIER, new View("abc", "bcd", "efg"));

        assertEquals(sqlList.size(), 2);
        Assert.assertTrue(sqlList.get(0).toSql().contains("SET QUOTED_IDENTIFIER"));
        Assert.assertTrue(sqlList.get(1).toSql().contains("CREATE PROCEDURE"));
    }

    @Test
    public void addSqlStatementsWithTwoSet() {
        List<Sql> sqlList = new ArrayList<>();

        SqlGeneratorMssqlUtil.addSqlStatementsToList(sqlList, DATA_WITH_TWO_SETS, ";");

        assertEquals(sqlList.size(), 3);
    }

    @Test
    public void addSqlStatementsWithTwoSetRelationFunction() {
        List<Sql> sqlList = new ArrayList<>();

        SqlGeneratorMssqlUtil.addSqlStatementsToList(sqlList, DATA_WITH_TWO_SETS, new View("abc", "bcd", "efg"));

        assertEquals(sqlList.size(), 3);
    }

    @Test
    public void addSqlStatementsWithLowerCaseSet() {
        List<Sql> sqlList = new ArrayList<>();

        SqlGeneratorMssqlUtil.addSqlStatementsToList(sqlList, LOWER_CASE_SET_STATEMENT, new View("abc", "bcd", "efg"));

        assertEquals(sqlList.size(), 3);
    }

    @Test
    public void addSqlStatementsWithJoinedCaseSet() {
        List<Sql> sqlList = new ArrayList<>();

        SqlGeneratorMssqlUtil.addSqlStatementsToList(sqlList, SAME_LINE_STATEMENT, new View("abc", "bcd", "efg"));

        assertEquals(sqlList.size(), 2);
    }

    @Test
    public void addSqlStatementsToListShouldKeepOriginalOrdering() {
        List<Sql> sqlList = new ArrayList<>();

        SqlGeneratorMssqlUtil.addSqlStatementsToList(sqlList, CORRECT_ORDER_STATEMENTS, new View("abc", "bcd", "efg"));

        assertEquals(sqlList.size(), 5);
        assertThat(sqlList.get(0).toString(), equalTo("SET ANSI_NULLS OFF;"));
        assertThat(sqlList.get(1).toString(), equalTo("SET QUOTED_IDENTIFIER OFF;"));
        assertThat(sqlList.get(2).toString(), containsString("CREATE PROCEDURE [dbo].[SqlTest01]"));
        assertThat(sqlList.get(3).toString(), equalTo("SET ANSI_NULLS ON;"));
        assertThat(sqlList.get(4).toString(), equalTo("SET QUOTED_IDENTIFIER ON;"));
    }


    @Test
    public void addSqlStatementsToListShouldProceedSqlStatementsOnly() {
        String setsOnly = "SET ANSI_NULLS OFF\n" +
                "    " +
                "SET QUOTED_IDENTIFIER OFF\n" +
                "    ";
        List<Sql> sqlList = new ArrayList<>();

        SqlGeneratorMssqlUtil.addSqlStatementsToList(sqlList, setsOnly, new View("abc", "bcd", "efg"));

        assertEquals(sqlList.size(), 2);
        assertThat(sqlList.get(0).toString(), equalTo("SET ANSI_NULLS OFF;"));
        assertThat(sqlList.get(1).toString(), equalTo("SET QUOTED_IDENTIFIER OFF;"));
    }
}
