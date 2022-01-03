package liquibase.sqlgenerator.core.util;

import liquibase.sql.Sql;
import liquibase.structure.core.View;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MSSQLUtilTest {
    private final static String data1 = "CREATE PROCEDURE [s1].[p10]   \n" +
            "AS   \n" +
            "    SELECT '10';\n" +
            "    SELECT '10\n";

    private final static String data1_quoted_ansi = "CREATE PROCEDURE [s1].[p10]   \n" +
            "AS   \n" +
            "    SET QUOTED_IDENTIFIER ON;    \n" +
            "    SET ANSI_NULLS ON; \n" +
            "\t        DROP PROCEDURE \n" +
            "\t        string';\n";

    private final static String data_amy = "SET QUOTED_IDENTIFIER OFF; \n" +
            "CREATE PROCEDURE [dbo].[SqlTest01]" +
            "AS \n" +
            "BEGIN \n" +
            "SELECT \"Amy\";\n"+
            "END;\n";

    private final static String test_proc = "SET ANSI_NULLS ON\n" +
            "SET QUOTED_IDENTIFIER ON\n" +
            ";\n" +
            "\n" +
            "ALTER PROCEDURE [dbo].test_proc AS\n" +
            "BEGIN\n" +
            "PRINT N'Hello, World! I am a MSSQL procedure.'\n" +
            "END]'\n";

    @Test
    public void addSqlStatementWithOneSet() {
        List<Sql> sqlList = new ArrayList<>();
        SqlGeneratorMSSQLUtil.addSqlStatementsToList(sqlList, data_amy, ";");
        Assert.assertEquals(sqlList.size(), 2);
        Assert.assertTrue(sqlList.get(0).toSql().contains("SET QUOTED_IDENTIFIER"));
        Assert.assertTrue(sqlList.get(1).toSql().contains("CREATE PROCEDURE"));
    }

    @Test
    public void addSqlStatementWithOneSet_testProc() {
        List<Sql> sqlList = new ArrayList<>();
        SqlGeneratorMSSQLUtil.addSqlStatementsToList(sqlList, test_proc, ";");
        Assert.assertEquals(sqlList.size(), 3);
        Assert.assertTrue(sqlList.get(0).toSql().contains("SET"));
        Assert.assertTrue(sqlList.get(1).toSql().contains("SET"));
        Assert.assertTrue(sqlList.get(2).toSql().contains("ALTER"));
        Assert.assertFalse(sqlList.get(2).toSql().contains("SET"));
        System.out.println("----------<testProc Output>-----------");
        sqlList.stream().forEach(System.out::println);
        System.out.println("----------<testProc Output>-----------");
    }

    @Test
    public void addSqlStatementWithOneSet_relationFunction() {
        List<Sql> sqlList = new ArrayList<>();
        SqlGeneratorMSSQLUtil.addSqlStatementsToList(sqlList, data_amy, new View("abc", "bcd", "efg"));
        System.out.println("--------------------------------");
        System.out.println("#### SIZE AMY: " + sqlList.size());
        Assert.assertEquals(sqlList.size(), 2);
        System.out.println("---------------------------------");
        Assert.assertTrue(sqlList.get(0).toSql().contains("SET QUOTED_IDENTIFIER"));
        Assert.assertTrue(sqlList.get(1).toSql().contains("CREATE PROCEDURE"));
    }

    @Test
    public void addSqlStatementsWithTwoSet() {
        List<Sql> sqlList = new ArrayList<>();
        SqlGeneratorMSSQLUtil.addSqlStatementsToList(sqlList, data1_quoted_ansi, ";");
        Assert.assertEquals(sqlList.size(), 3);
    }

    @Test
    public void addSqlStatementsWithTwoSet_relationFunction() {
        List<Sql> sqlList = new ArrayList<>();
        SqlGeneratorMSSQLUtil.addSqlStatementsToList(sqlList, data1_quoted_ansi, new View("abc", "bcd", "efg"));
        System.out.println("--------------------------------------");
        System.out.println("Data1_QUOTED_ANSI" + sqlList.size());
        System.out.println("--------------------------------------");
        Assert.assertEquals(sqlList.size(), 3);
    }
}
