package liquibase.migrator;

import junit.framework.TestCase;
import junit.framework.AssertionFailedError;
import liquibase.database.DatabaseFactory;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.migrator.diff.Diff;
import liquibase.migrator.diff.DiffResult;
import liquibase.migrator.exception.ValidationFailedException;
import liquibase.migrator.exception.MigrationFailedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Date;
import java.util.Properties;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractSimpleChangeLogRunnerTest extends TestCase {

    private String completeChangeLog;
    private String rollbackChangeLog;
    protected String username;
    protected String password;
    protected String driverName;
    protected String url;
    protected Connection connection;
    protected Driver driver;

    protected AbstractSimpleChangeLogRunnerTest(String changelogDir, String driverName, String url) {
        this.completeChangeLog = "changelogs/" + changelogDir + "/complete/root.changelog.xml";
        this.rollbackChangeLog = "changelogs/" + changelogDir + "/rollback/rollbackable.changelog.xml";
        this.driverName = driverName;
        this.url = url;
        username = "liquibase";
        password = "liquibase";

        Logger.getLogger(Migrator.DEFAULT_LOG_NAME).setLevel(Level.OFF);
    }

    protected void setUp() throws Exception {
        super.setUp();

        JUnitJDBCDriverClassLoader jdbcDriverLoader = JUnitJDBCDriverClassLoader.getInstance();
        driver = (Driver) Class.forName(driverName, true, jdbcDriverLoader).newInstance();
        Properties info = createProperties();
        info.put("user", username);
        info.put("password", password);
        connection = driver.connect(url, info);
        connection.setAutoCommit(false);
    }

    protected Properties createProperties() {
        return new Properties();
    }

    protected void tearDown() throws Exception {
        if (shouldRollBack()) {
            connection.rollback();
        }
        super.tearDown();
        connection.close();
        connection = null;
        driver = null;

    }

    protected boolean shouldRollBack() {
        return true;
    }

    protected Migrator createMigrator(String changeLogFile) throws Exception {
        JUnitFileOpener fileOpener = new JUnitFileOpener();
        Migrator migrator = new Migrator(changeLogFile, fileOpener);
        migrator.setContexts("test");

        migrator.init(connection);
        return migrator;
    }

    public void testRunChangeLog() throws Exception {
        runCompleteChangeLog();
    }

    private void runCompleteChangeLog() throws Exception {
        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll();

        //run again to test changelog testing logic
        migrator = createMigrator(completeChangeLog);
        try {
            migrator.migrate();
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

    public void testOutputChangeLog() throws Exception {
        StringWriter output = new StringWriter();
        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll();

        migrator = createMigrator(completeChangeLog);
        migrator.setOutputSQLWriter(output);
        migrator.setMode(Migrator.Mode.OUTPUT_SQL_MODE);
        migrator.migrate();

//        System.out.println(output.getBuffer().toString());
    }

    public void testRollbackableChangeLog() throws Exception {
        Migrator migrator = createMigrator(rollbackChangeLog);
        migrator.dropAll();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.Mode.EXECUTE_MODE);
        migrator.migrate();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.Mode.EXECUTE_ROLLBACK_MODE);
        migrator.setRollbackToDate(new Date(0));
        migrator.migrate();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.Mode.EXECUTE_MODE);
        migrator.migrate();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.Mode.EXECUTE_ROLLBACK_MODE);
        migrator.setRollbackToDate(new Date(0));
        migrator.migrate();
    }

    public void testRollbackableChangeLogScriptOnExistingDatabase() throws Exception {
        Migrator migrator = createMigrator(rollbackChangeLog);
        migrator.dropAll();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.Mode.EXECUTE_MODE);
        migrator.migrate();

        StringWriter writer = new StringWriter();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.Mode.OUTPUT_ROLLBACK_SQL_MODE);
        migrator.setOutputSQLWriter(writer);
        migrator.setRollbackToDate(new Date(0));
        migrator.migrate();

//        System.out.println("Rollback SQL for "+driverName+StreamUtil.getLineSeparator()+StreamUtil.getLineSeparator()+writer.toString());
    }

    public void testRollbackableChangeLogScriptOnFutureDatabase() throws Exception {
        StringWriter writer = new StringWriter();

        Migrator migrator = createMigrator(rollbackChangeLog);
        migrator.dropAll();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.Mode.OUTPUT_FUTURE_ROLLBACK_SQL_MODE);
        migrator.setOutputSQLWriter(writer);
        migrator.setRollbackToDate(new Date(0));
        migrator.migrate();

//        System.out.println("Rollback SQL for future "+driverName+"\n\n"+writer.toString());
    }

    public void testTag() throws Exception {
        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll();

        migrator = createMigrator(completeChangeLog);
        migrator.migrate();

        migrator.tag("Test Tag");
    }

    public void testGetDefaultDriver() throws Exception {
        Migrator migrator = createMigrator(completeChangeLog);

        assertEquals(driverName, migrator.getDatabase().getDefaultDriver(url));
    }

    public void testDiff() throws Exception {
        runCompleteChangeLog();

        Diff diff = new Diff(connection, connection);
        DiffResult diffResult = diff.compare();

        assertEquals(0, diffResult.getMissingColumns().size());
        assertEquals(0, diffResult.getMissingForeignKeys().size());
        assertEquals(0, diffResult.getMissingIndexes().size());
        assertEquals(0, diffResult.getMissingPrimaryKeys().size());
        assertEquals(0, diffResult.getMissingSequences().size());
        assertEquals(0, diffResult.getMissingColumns().size());
        assertEquals(0, diffResult.getMissingTables().size());
        assertEquals(0, diffResult.getMissingViews().size());

        assertEquals(0, diffResult.getUnexpectedColumns().size());
        assertEquals(0, diffResult.getUnexpectedForeignKeys().size());
        assertEquals(0, diffResult.getUnexpectedIndexes().size());
        assertEquals(0, diffResult.getUnexpectedPrimaryKeys().size());
        assertEquals(0, diffResult.getUnexpectedSequences().size());
        assertEquals(0, diffResult.getUnexpectedColumns().size());
        assertEquals(0, diffResult.getUnexpectedTables().size());
        assertEquals(0, diffResult.getUnexpectedViews().size());
    }

    public void testRerunDiffChangeLog() throws Exception {
        runCompleteChangeLog();

        DatabaseSnapshot originalSnapshot = new DatabaseSnapshot(DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection));

        Diff diff = new Diff(connection);
        DiffResult diffResult = diff.compare();

        File tempFile = File.createTempFile("liquibase-test", ".xml");

        FileOutputStream output = new FileOutputStream(tempFile);
        diffResult.printChangeLog(new PrintStream(output), DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection));
        output.flush();
        output.close();

        Migrator migrator = createMigrator(tempFile.getName());
        migrator.dropAll();

        //run again to test changelog testing logic
        migrator = createMigrator(tempFile.getName());
        try {
            migrator.migrate();
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        tempFile.deleteOnExit();

        DatabaseSnapshot finalSnapshot = new DatabaseSnapshot(DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection));

        DiffResult finalDiffResult = new Diff(originalSnapshot, finalSnapshot).compare();
        assertEquals(0, finalDiffResult.getMissingColumns().size());
        assertEquals(0, finalDiffResult.getMissingForeignKeys().size());
        assertEquals(0, finalDiffResult.getMissingIndexes().size());
        assertEquals(0, finalDiffResult.getMissingPrimaryKeys().size());
//        assertEquals(0, finalDiffResult.getMissingSequences().size());  //disabled for now because we can't diff sequences
        assertEquals(0, finalDiffResult.getMissingTables().size());
//        assertEquals(0, finalDiffResult.getMissingViews().size());  //disabled for now because we can't diff views
        assertEquals(0, finalDiffResult.getUnexpectedColumns().size());
        assertEquals(0, finalDiffResult.getUnexpectedForeignKeys().size());
        assertEquals(0, finalDiffResult.getUnexpectedIndexes().size());
        assertEquals(0, finalDiffResult.getUnexpectedPrimaryKeys().size());
        assertEquals(0, finalDiffResult.getUnexpectedSequences().size());
        assertEquals(0, finalDiffResult.getUnexpectedTables().size());
        assertEquals(0, finalDiffResult.getUnexpectedViews().size());

    }

    public void testClearChecksums() throws Exception {
        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll();

        migrator = createMigrator(completeChangeLog);
        migrator.dropAll();

        migrator = createMigrator(completeChangeLog);
        migrator.migrate();

        migrator.clearCheckSums();
    }

    public void testTagEmptyDatabase() throws Exception {
        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll();

        migrator = createMigrator(completeChangeLog);
        migrator.checkDatabaseChangeLogTable();
        try {
            migrator.tag("empty");
        } catch (MigrationFailedException e) {
            assertEquals("liquibase.migrator.exception.MigrationFailedException: Cannot tag an empty database", e.getMessage());
        }

    }

    public void testUnrunChangeSetsEmptyDatabase() throws Exception {
        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll();

        migrator = createMigrator(completeChangeLog);
        List<ChangeSet> list = migrator.listUnrunChangeSets();

        assertTrue(list.size() > 0);

    }
}
