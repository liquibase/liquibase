package liquibase.dbtest.h2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.diff.output.report.DiffToReport;
import liquibase.exception.DatabaseException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationFailedException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.jvm.JdbcExecutor;
import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.TestSystemFactory;
import liquibase.precondition.core.RowCountPrecondition;
import liquibase.precondition.core.TableExistsPrecondition;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.core.RawSqlStatement;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class H2IntegrationTest extends AbstractIntegrationTest {

    private static final String H2_SQLSTATE_OBJECT_ALREADY_EXISTS = "90078";
    private final String changeSpecifyDbmsChangeLog;
    private final String dbmsExcludeChangelog;

    public H2IntegrationTest() throws Exception {
        super("h2", DatabaseFactory.getInstance().getDatabase("h2"));
        this.changeSpecifyDbmsChangeLog = "changelogs/h2/complete/change.specify.dbms.changelog.xml";
        this.dbmsExcludeChangelog = "changelogs/h2/complete/dbms.exclude.changelog.xml";
    }

    @Test
    public void diffToPrintStream() throws Exception{
        if (getDatabase() == null) {
            return;
        }

        runCompleteChangeLog();

        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(getDatabase(), null, new CompareControl());
        new DiffToReport(diffResult, System.out).print();
    }

    // TODO: This test currently makes the whole VM exit with exit code -1, but does not generate a dump file
    // (not a "genuine" VM crash). I need to disable this test until I can find out how to catch/debug this.
    @Test
    public void diffToChangeLog() throws Exception{
        if (getDatabase() == null) {
            return;
        }

        runCompleteChangeLog();

        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(getDatabase(), null, new CompareControl());
        File outputFile = new File("diffToChangeLog_" + getDatabase().getShortName() + ".log");
        if (outputFile.exists())
            outputFile.delete();
        PrintStream writer = new PrintStream(outputFile);

        new DiffToChangeLog(diffResult, new DiffOutputControl(true, true, true, null)).print(writer);
        writer.close();


    }

    @Test
    public void canSpecifyDbmsForIndividualChanges() throws Exception {
        runChangeLogFile(changeSpecifyDbmsChangeLog);
    }

    @Test
    public void h2IsExcludedFromRunningChangeset() throws Exception {
        runChangeLogFile(dbmsExcludeChangelog);
    }

    @Test
    public void runYamlChangelog() throws Exception {
        if (getDatabase() == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase();

        //run again to test changelog testing logic
        liquibase = createLiquibase("changelogs/yaml/common.tests.changelog.yaml");
        liquibase.setChangeLogParameter("loginuser", testSystem.getUsername());

        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }


    }

    @Test
    public void runJsonChangelog() throws Exception {
        if (getDatabase() == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase();

        //run again to test changelog testing logic
        liquibase = createLiquibase("changelogs/json/common.tests.changelog.json");
        liquibase.setChangeLogParameter("loginuser", testSystem.getUsername());

        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

    @Test
    @Override
    public void testGenerateChangeLogWithNoChanges() throws Exception {
        super.testGenerateChangeLogWithNoChanges();    //To change body of overridden methods use File | Settings |
        // File Templates.
    }

    @Test
    public void testRollbackByContext() throws Exception {
        Integer insertedValue = 1;
        String colName = "COL1";
        String tableName = "tmp_tbl";
        for (String context : Arrays.asList("ctx1", "ctx2")) {
            clearDatabase();

            Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                    .execute(new RawSqlStatement(String.format("CREATE TABLE %s (%s varchar(50))", tableName, colName)));

            Liquibase liquibase = createLiquibase("changelogs/h2/complete/rollback.different.contexts.changelog.xml");
            liquibase.update(context);

            List<Map<String, ?>> queryResult = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                    .queryForList(new RawSqlStatement(String.format("select * from %s", tableName)));

            Assert.assertEquals(1, queryResult.size());
            Assert.assertEquals(insertedValue.toString(), queryResult.get(0).get(colName));
            insertedValue++;

            liquibase.rollback(1, context);
            queryResult = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                    .queryForList(new RawSqlStatement(String.format("select * from %s", tableName)));
            Assert.assertEquals("Rollbacking for " + insertedValue, 2, queryResult.size());
            Assert.assertEquals(insertedValue.toString(), queryResult.get(1).get(colName));
            insertedValue++;
        }
    }

    @Test
    public void testRollbackWithoutContext() throws Exception {
        Integer insertedValue = 5;
        String colName = "COL1";
        String tableName = "tmp_tbl";

        clearDatabase();

        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                .execute(new RawSqlStatement(String.format("CREATE TABLE %s (%s varchar(50))", tableName, colName)));

        Liquibase liquibase = createLiquibase("changelogs/h2/complete/rollback.sql.changelog.xml");
        liquibase.update();

        List<Map<String, ?>> queryResult = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                .queryForList(new RawSqlStatement(String.format("select * from %s", tableName)));

        Assert.assertEquals(1, queryResult.size());
        Assert.assertEquals(insertedValue.toString(), queryResult.get(0).get(colName));
        insertedValue++;

        liquibase.rollback(1, null);
        queryResult = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                .queryForList(new RawSqlStatement(String.format("select * from %s", tableName)));
        Assert.assertEquals("Rollbacking for " + insertedValue, 2, queryResult.size());
        Assert.assertEquals(insertedValue.toString(), queryResult.get(1).get(colName));
    }

    @Test
    public void makeSureDbmsFilteredChangeIsNotDeployed() throws Exception {
        clearDatabase();
        runUpdate("changelogs/h2/complete/sql.change.dbms.filtered.should.not.be.deployed.changelog.xml");

        try {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                    .queryForList(new RawSqlStatement("select * from oraculo"));
        }
        catch (DatabaseException e) {
            Assert.assertTrue(e.getMessage().contains("Table \"ORACULO\" not found"));
        }
        try {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                    .queryForList(new RawSqlStatement("select * from anydb"));
        }
        catch (DatabaseException e) {
            Assert.assertTrue(e.getMessage().contains("Table \"ANYDB\" not found"));
        }
    }

    /**
     * Verifies that the {@link Executor#execute(Change)} method is called by testing an Executor implementation
     * that uses a separate database connection and commits after each change completes.
     *
     * @throws Exception
     */
    @Test
    public void testCustomExecutorInvokedPerChange() throws Exception {
        assumeNotNull(this.getDatabase());
        String tableName = "test_numbers";
        try {
            runChangeLogFile("changelogs/common/runWith.executor.changelog.xml");
        } catch (Exception e) {
            // ok - expect a failure to prove that the changes earlier in the change set committed
        }

        AlternateConnectionExecutor alternateConnectionExecutor = (AlternateConnectionExecutor) Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("h2alt", getDatabase());
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", getDatabase(), alternateConnectionExecutor);

        // Rollback anything in progress on the connection to ensure the executor actually committed
        alternateConnectionExecutor.getDatabase().rollback();

        // Confirm the number of expected rows were inserted into the table in the alt schema
        RowCountPrecondition precondition = new RowCountPrecondition();
        precondition.setSchemaName(testSystem.getAltSchema());
        precondition.setTableName(tableName);
        precondition.setExpectedRows(2L);
        try {
            precondition.check(alternateConnectionExecutor.getDatabase(), null, null, null);
        } catch (PreconditionFailedException e) {
            fail(e.getFailedPreconditions().get(0).getMessage());
        }

        // Confirm the table was not created in default database and schema
        TableExistsPrecondition tableExistsPrecondition = new TableExistsPrecondition();
        tableExistsPrecondition.setTableName(tableName);
        PreconditionFailedException ex = assertThrows(PreconditionFailedException.class, () -> tableExistsPrecondition.check(this.getDatabase(), null, null, null));
        assertEquals(1, ex.getFailedPreconditions().size());
        String expectedMessage = String.format("%s does not exist", tableName);
        assertTrue(ex.getFailedPreconditions().get(0).getMessage().endsWith(expectedMessage));
    }

    /**
     * An {@link JdbcExecutor} that provides its own {@link DatabaseConnection} and commits after executing
     * each change
     */
    public class AlternateConnectionExecutor extends JdbcExecutor {

        public AlternateConnectionExecutor() throws Exception {
            String urlParameters = ";INIT=CREATE SCHEMA IF NOT EXISTS lbschem2\\;SET SCHEMA lbschem2";
            DatabaseTestSystem testSystem = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("h2");
            database = DatabaseFactory.getInstance().openDatabase(testSystem.getConnectionUrl().replace("lbcat", "lbcat2") + urlParameters,
                    testSystem.getUsername(), testSystem.getPassword(), null, null);
            database.setAutoCommit(false);
        }

        @Override
        public String getName() {
            return "h2alt";
        }

        @Override
        public void setDatabase(Database database) {
            // ignore the database connection passed in since we're providing our own
        }

        public Database getDatabase() {
            return database;
        }

        @Override
        public void execute(Change change, List<SqlVisitor> sqlVisitors) throws DatabaseException {
            super.execute(change, sqlVisitors);
            database.commit();
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        try {
            // Create schemas for tests testRerunDiffChangeLogAltSchema
            ((JdbcConnection) getDatabase().getConnection()).getUnderlyingConnection().createStatement().executeUpdate(
                    "CREATE SCHEMA LBCAT2"
            );
        } catch (SQLException e) {
            if (e.getSQLState().equals(H2_SQLSTATE_OBJECT_ALREADY_EXISTS)) {
                // do nothing
            } else {
                throw e;
            }
        }
/*        ((JdbcConnection) getDatabase().getConnection()).getUnderlyingConnection().createStatement().executeUpdate(
                "SET SCHEMA LIQUIBASE"
        ); */
        getDatabase().commit();

    }
}
