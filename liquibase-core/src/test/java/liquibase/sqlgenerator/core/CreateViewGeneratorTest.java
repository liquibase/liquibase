package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateViewStatement;
import liquibase.util.SqlParser;
import liquibase.util.StringClauses;
import org.junit.Before;
import org.junit.Test;

import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class CreateViewGeneratorTest {
    private static final String LSP = System.lineSeparator();

    private CreateViewGenerator generator;
    Database database;


    @Before
    public void setUp() {
        generator = new CreateViewGenerator();
        database = new OracleDatabase();
    }

    @Test
    public void generateSql_mssqlWithSetStatements_addsSetInProperOrder() {
        String viewBody = "" +
                "SET ANSI_NULLS OFF\n" +
                "SET QUOTED_IDENTIFIER OFF\n" +
                "CREATE VIEW dbo.some_view \n" +
                "AS \n" +
                "   SELECT LastName FROM dbo.Persons\n" +
                ";\n";

        CreateViewStatement statement =
                new CreateViewStatement("", "dbo", "some_view", viewBody, true);

        Sql[] sqls = generator.generateSql(statement, new MSSQLDatabase(), new SqlGeneratorChain(new TreeSet<>()));


        assertEquals(6, sqls.length);
        assertEquals("IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'[DBO].[some_view]'))\n" +
                "    EXEC sp_executesql N'CREATE VIEW [DBO].[some_view] AS SELECT ''This is a code stub which will be replaced by an Alter Statement'' as [code_stub]'", sqls[0].toSql());
        assertEquals("SET ANSI_NULLS OFF", sqls[1].toSql());
        assertEquals("SET QUOTED_IDENTIFIER OFF", sqls[2].toSql());
        assertEquals(
                "CREATE VIEW [dbo].[some_view] AS ALTER VIEW dbo.some_view \n" +
                        "AS \n" +
                        "   SELECT LastName FROM dbo.Persons\n" +
                        ";",
                sqls[3].toSql());
        assertEquals("SET ANSI_NULLS ON", sqls[4].toSql());
        assertEquals("SET QUOTED_IDENTIFIER ON", sqls[5].toSql());
    }


    @Test
    public void cleanUpSqlString_noTrailingComment_slashCleanUpDone() {
        String initialSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE" + LSP + "     /";
        String expectedCleanSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE" + LSP + "     ";

        StringClauses viewDefinition = SqlParser.parse(initialSql, true, true);

        assertEquals(expectedCleanSql, generator.getViewDefinition(viewDefinition, database));
    }

    @Test
    public void cleanUpSqlString_noTrailingComment_semicolonCleanUpDone() {
        String initialSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE;";

        StringClauses viewDefinition = SqlParser.parse(initialSql, true, true);

        String expectedCleanSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE";
        assertEquals(expectedCleanSql, generator.getViewDefinition(viewDefinition, database));
    }

    @Test
    public void cleanUpSqlString_withTrailingComment_slashCleanUpDone() {
        String initialSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE" + LSP + "     /" + LSP +
                "   -- trailing comment";

        StringClauses viewDefinition = SqlParser.parse(initialSql, true, true);
        String expectedCleanSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE" + LSP + "     " + LSP +
                "   -- trailing comment";

        assertEquals(expectedCleanSql, generator.getViewDefinition(viewDefinition, database));
    }

    @Test
    public void cleanUpSqlString_withTrailingComment_slashAndSemicolonCleanUpDone() {
        String initialSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE;" + LSP + "     /" + LSP +
                "   -- trailing comment";

        StringClauses viewDefinition = SqlParser.parse(initialSql, true, true);
        String expectedCleanSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE" + LSP + "     " + LSP +
                "   -- trailing comment";

        assertEquals(expectedCleanSql, generator.getViewDefinition(viewDefinition, database));
    }

    @Test
    public void cleanUpSqlString_noTrailingCommentNoLineSeparatorBeforeSlas_slashCleanUpDone() {
        String initialSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE/    ";

        StringClauses viewDefinition = SqlParser.parse(initialSql, true, true);

        String expectedCleanSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE    ";
        assertEquals(expectedCleanSql, generator.getViewDefinition(viewDefinition, database));
    }

    @Test
    public void cleanUpSqlString_noTrailingCommentNoLineSeparatorBeforeSlas_slashAndSemicolonCleanUpDone() {
        String initialSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE;/    ";

        StringClauses viewDefinition = SqlParser.parse(initialSql, true, true);

        String expectedCleanSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE    ";
        assertEquals(expectedCleanSql, generator.getViewDefinition(viewDefinition, database));
    }

    @Test
    public void cleanUpSqlString_noTrailingCommentNoLineSeparatorBeforeSlasAndComment_slashAndSemicolonCleanUpDone() {
        String initialSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE;/ -- trailing comment   ";

        StringClauses viewDefinition = SqlParser.parse(initialSql, true, true);
        String expectedCleanSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE -- trailing comment   ";

        assertEquals(expectedCleanSql, generator.getViewDefinition(viewDefinition, database));
    }

    @Test
    public void cleanUpSqlString_veryUglyValidCase_slashAndSemicolonCleanUpDone() {
        String initialSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE;;;/;///; -- trailing comment inline" + LSP + "     /" + LSP +
                "///" + LSP + ";;" + LSP + ";/;/;/;" + LSP +
                "   -- trailing comment next line";

        StringClauses viewDefinition = SqlParser.parse(initialSql, true, true);
        String expectedCleanSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE -- trailing comment inline" + LSP + "     " + LSP +
                LSP + LSP + LSP +
                "   -- trailing comment next line";

        assertEquals(expectedCleanSql, generator.getViewDefinition(viewDefinition, database));
    }

    @Test
    public void cleanUpSqlString_veryUglyValidCase_slashAndSemicolonCleanUpDoneAndDontTouchSlashAndSemicolonInOracleLiteral() {
        String initialSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE WHERE foo = '/bar;';;;/;///; -- trailing comment inline" + LSP + "     /" + LSP +
                "///" + LSP + ";;" + LSP + ";/;/;/;" + LSP +
                "   -- trailing comment next line";

        StringClauses viewDefinition = SqlParser.parse(initialSql, true, true);
        String expectedCleanSql = "   CREATE VIEW SOME_VIEW AS SELECT * FROM SOME_TABLE WHERE foo = '/bar;' -- trailing comment inline" + LSP + "     " + LSP +
                LSP + LSP + LSP +
                "   -- trailing comment next line";

        assertEquals(expectedCleanSql, generator.getViewDefinition(viewDefinition, database));
    }

    @Test
    public void cleanUpSqlString_veryValidCaseWithInnerStatements_slashAndSemicolonCleanUpDoneAndDontTouchSlashAndSemicolonInOracleCommentsAndInternalStatements() {
        //here create statement for function is used due to that reason that it wasn't found example for create view statement with 'inner' statement delimiter usage,
        //so it is for extremely rare, almost impossible case
        String initialSql = "CREATE OR REPLACE FUNCTION get_total_sales(" + LSP +
                "    in_year PLS_INTEGER" + LSP +
                ") " + LSP +
                "RETURN NUMBER" + LSP +
                "IS" + LSP +
                "    l_total_sales NUMBER := 0" + LSP +
                ";" + LSP + //here we have 'inner' hanging delimiter that should not be touched
                "BEGIN" + LSP +
                "    -- get total sales ///" + LSP +
                "    SELECT SUM(unit_price * quantity)" + LSP +
                "    INTO l_total_sales" + LSP +
                "    FROM order_items" + LSP +
                "    INNER JOIN orders USING (order_id)" + LSP +
                "    WHERE status = 'Shipped'" + LSP +
                "    GROUP BY EXTRACT(YEAR FROM order_date)" + LSP +
                "    HAVING EXTRACT(YEAR FROM order_date) = in_year;" + LSP +
                "    " + LSP +
                "    -- return the total sales ;;;" + LSP +
                "    RETURN l_total_sales;" + LSP +
                "END;" +  LSP + "/" + LSP;

        StringClauses viewDefinition = SqlParser.parse(initialSql, true, true);
        String expectedCleanSql = "CREATE OR REPLACE FUNCTION get_total_sales(" + LSP +
                "    in_year PLS_INTEGER" + LSP +
                ") " + LSP +
                "RETURN NUMBER" + LSP +
                "IS" + LSP +
                "    l_total_sales NUMBER := 0" + LSP +
                ";" + LSP + //here we have 'inner' hanging delimiter that should not be touched
                "BEGIN" + LSP +
                "    -- get total sales ///" + LSP +
                "    SELECT SUM(unit_price * quantity)" + LSP +
                "    INTO l_total_sales" + LSP +
                "    FROM order_items" + LSP +
                "    INNER JOIN orders USING (order_id)" + LSP +
                "    WHERE status = 'Shipped'" + LSP +
                "    GROUP BY EXTRACT(YEAR FROM order_date)" + LSP +
                "    HAVING EXTRACT(YEAR FROM order_date) = in_year;" + LSP +
                "    " + LSP +
                "    -- return the total sales ;;;" + LSP +
                "    RETURN l_total_sales;" + LSP +
                "END" +  LSP + LSP;

        assertEquals(expectedCleanSql, generator.getViewDefinition(viewDefinition, database));
    }

//    @Test
//    public void execute_defaultSchema() throws Exception {
//        final String definition = "SELECT * FROM " + TABLE_NAME;
//
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new CreateViewStatement(null, VIEW_NAME, definition, false)) {
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getView(VIEW_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        View view = snapshot.getView(VIEW_NAME);
//                        assertNotNull(view);
//                        assertEquals(2, view.getColumns().size());
//                    }
//
//                });
//    }
//
//    @Test
//    public void execute_altSchema() throws Exception {
//        final String definition = "SELECT * FROM " + TestContext.ALT_SCHEMA+"."+TABLE_NAME;
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(TestContext.ALT_SCHEMA, new CreateViewStatement(TestContext.ALT_SCHEMA, VIEW_NAME, definition, false)) {
//                    protected boolean supportsTest(Database database) {
//                        return !(database instanceof HsqlDatabase  || database  instanceof H2Database || database instanceof OracleDatabase); //don't know why oracle isn't working
//                    }
//
//                    protected boolean expectedException(Database database, DatabaseException exception) {
//                        return !database.supportsSchemas();
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        assertNull(snapshot.getView(VIEW_NAME));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) {
//                        View view = snapshot.getView(VIEW_NAME);
//                        assertNotNull(view);
//                        assertEquals(2, view.getColumns().size());
//                    }
//
//                });
//    }

}
