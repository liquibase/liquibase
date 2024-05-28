package liquibase.dbtest.h2;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.change.Change;
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
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.core.DatabaseObjectFactory;
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
        //
        // Reset the factory so that the standard types will be repopulated
        // They might have been changed by a previous test.  Without the correct
        // list of types, not all objects will be correctly dropped.
        //
        DatabaseObjectFactory.getInstance().reset();
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
                    .execute(new RawParameterizedSqlStatement(String.format("CREATE TABLE %s (%s varchar(50))", tableName, colName)));

            Liquibase liquibase = createLiquibase("changelogs/h2/complete/rollback.different.contexts.changelog.xml");
            liquibase.update(context);

            List<Map<String, ?>> queryResult = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                    .queryForList(new RawParameterizedSqlStatement(String.format("select * from %s", tableName)));

            Assert.assertEquals(1, queryResult.size());
            Assert.assertEquals(insertedValue.toString(), queryResult.get(0).get(colName));
            insertedValue++;

            liquibase.rollback(1, context);
            queryResult = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                    .queryForList(new RawParameterizedSqlStatement(String.format("select * from %s", tableName)));
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
                .execute(new RawParameterizedSqlStatement(String.format("CREATE TABLE %s (%s varchar(50))", tableName, colName)));

        Liquibase liquibase = createLiquibase("changelogs/h2/complete/rollback.sql.changelog.xml");
        liquibase.update();
        List<Map<String, ?>> queryResult = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                .queryForList(new RawParameterizedSqlStatement(String.format("select * from %s", tableName)));

        Assert.assertEquals(1, queryResult.size());
        Assert.assertEquals(insertedValue.toString(), queryResult.get(0).get(colName));
        insertedValue++;

        liquibase.rollback(1, null);
        queryResult = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                .queryForList(new RawParameterizedSqlStatement(String.format("select * from %s", tableName)));
        Assert.assertEquals("Rollbacking for " + insertedValue, 2, queryResult.size());
        Assert.assertEquals(insertedValue.toString(), queryResult.get(1).get(colName));
    }

    @Test
    public void makeSureDbmsFilteredChangeIsNotDeployed() throws Exception {
        clearDatabase();
        runUpdate("changelogs/h2/complete/sql.change.dbms.filtered.should.not.be.deployed.changelog.xml");

        try {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                    .queryForList(new RawParameterizedSqlStatement("select * from oraculo"));
        }
        catch (DatabaseException e) {
            Assert.assertTrue(e.getMessage().contains("Table \"ORACULO\" not found"));
        }
        try {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                    .queryForList(new RawParameterizedSqlStatement("select * from anydb"));
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
        final String changeLogFile = "changelogs/common/runWith.executor.changelog.xml";
        try {
            Liquibase liquibase = createLiquibase(changeLogFile);
            liquibase.update();
        } catch (Exception e) {
            // ok - expect a failure to prove that the changes earlier in the change set committed
        }

        // Confirm the number of expected rows were inserted into the table in the alt schema by running the changelog again to get to the precondition checks
        try {
            Liquibase liquibase = createLiquibase(changeLogFile);
            liquibase.update();
        } catch (PreconditionFailedException e) {
            fail(e.getFailedPreconditions().get(0).getMessage());
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
