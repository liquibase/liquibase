package liquibase.sqlgenerator.core;

import liquibase.database.core.MSSQLDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.CreateViewStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

public class CreateViewGeneratorTest {

    private CreateViewGenerator generator;

    @Before
    public void setUp() {
        generator = new CreateViewGenerator();
    }

    @Test
    public void generateSql_mssqlWithSetStatements_addsSetInProperOrder() {
        String viewBody = "" +
                "SET ANSI_NULLS OFF\n" +
                "SET QUOTED_IDENTIFIER OFF\n" +
                "CREATE VIEW dbo.some_view \n" +
                "AS \n" +
                "   SELECT LastName FROM dbo.Persons\n" +
                ";\n" +
                "SET ANSI_NULLS ON\n" +
                "SET QUOTED_IDENTIFIER ON\n";

        CreateViewStatement statement =
                new CreateViewStatement("", "dbo", "some_view", viewBody, true);

        Sql[] sqls = generator.generateSql(statement, new MSSQLDatabase(), new SqlGeneratorChain(new TreeSet<>()));


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
