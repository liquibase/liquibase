package liquibase.migrator;

import junit.framework.TestCase;
import liquibase.ChangeSet;
import liquibase.FileOpener;
import liquibase.FileSystemFileOpener;
import liquibase.database.AbstractDatabase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.sql.DropTableStatement;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.diff.Diff;
import liquibase.diff.DiffResult;
import liquibase.exception.JDBCException;
import liquibase.exception.ValidationFailedException;
import liquibase.lock.LockHandler;
import liquibase.log.LogFactory;
import liquibase.test.JUnitFileOpener;
import liquibase.test.TestContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

public abstract class AbstractSimpleChangeLogRunnerTest extends TestCase {

    private String completeChangeLog;
    private String rollbackChangeLog;
    private String includedChangeLog;
    private String contexts = "test, context-b";
    private Database database;
    private String url;

    protected AbstractSimpleChangeLogRunnerTest(String changelogDir, String url) throws Exception {
        this.completeChangeLog = "changelogs/" + changelogDir + "/complete/root.changelog.xml";
        this.rollbackChangeLog = "changelogs/" + changelogDir + "/rollback/rollbackable.changelog.xml";
        this.includedChangeLog = "changelogs/" + changelogDir + "/complete/included.changelog.xml";

        this.url = url;

        DatabaseConnection connection = TestContext.getInstance().getConnection(url);

        LogFactory.getLogger().setLevel(Level.SEVERE);
        if (connection != null) {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        if (database != null) {
            if (!database.getConnection().getAutoCommit()) {
                database.rollback();
            }

            LockHandler.getInstance(database).forceReleaseLock();
        }
    }

    protected Properties createProperties() {
        return new Properties();
    }

    protected void tearDown() throws Exception {
        if (database != null) {
            if (shouldRollBack()) {
                database.rollback();
            }
            database.setDefaultSchemaName(null);
        }
        super.tearDown();
    }

    protected boolean shouldRollBack() {
        return true;
    }

    protected Migrator createMigrator(String changeLogFile) throws Exception {
        JUnitFileOpener fileOpener = new JUnitFileOpener();
        return createMigrator(changeLogFile, fileOpener);
    }

    private Migrator createMigrator(String changeLogFile, FileOpener fileOpener) throws JDBCException {
        return new Migrator(changeLogFile, fileOpener, database);
    }

    public void testRunChangeLog() throws Exception {
        if (database == null) {
            return;
        }
        
        runCompleteChangeLog();
    }

    private void runCompleteChangeLog() throws Exception {
        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll(getSchemasToDrop());

        //run again to test changelog testing logic
        migrator = createMigrator(completeChangeLog);
        try {
            migrator.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

    protected String[] getSchemasToDrop() throws JDBCException {
        return new String[] {
                "liquibaseb".toUpperCase(),
                database.getDefaultSchemaName(),
        };
    }

    public void testOutputChangeLog() throws Exception {
        if (database == null) {
            return;
        }

        StringWriter output = new StringWriter();
        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll(getSchemasToDrop());

        migrator = createMigrator(completeChangeLog);
        migrator.update(this.contexts, output);

//        System.out.println(output.getBuffer().toString());
    }

    public void testRollbackableChangeLog() throws Exception {
        if (database == null) {
            return;
        }

        Migrator migrator = createMigrator(rollbackChangeLog);
        migrator.dropAll(getSchemasToDrop());

        migrator = createMigrator(rollbackChangeLog);
        migrator.update(this.contexts);

        migrator = createMigrator(rollbackChangeLog);
        migrator.rollback(new Date(0), this.contexts);

        migrator = createMigrator(rollbackChangeLog);
        migrator.update(this.contexts);

        migrator = createMigrator(rollbackChangeLog);
        migrator.rollback(new Date(0), this.contexts);
    }

    public void testRollbackableChangeLogScriptOnExistingDatabase() throws Exception {
        if (database == null) {
            return;
        }

        Migrator migrator = createMigrator(rollbackChangeLog);
        migrator.dropAll(getSchemasToDrop());

        migrator = createMigrator(rollbackChangeLog);
        migrator.update(this.contexts);

        StringWriter writer = new StringWriter();

        migrator = createMigrator(rollbackChangeLog);
        migrator.rollback(new Date(0), this.contexts, writer);

//        System.out.println("Rollback SQL for "+driverName+StreamUtil.getLineSeparator()+StreamUtil.getLineSeparator()+writer.toString());
    }

    public void testRollbackableChangeLogScriptOnFutureDatabase() throws Exception {
        if (database == null) {
            return;
        }

        StringWriter writer = new StringWriter();

        Migrator migrator = createMigrator(rollbackChangeLog);
        migrator.dropAll(getSchemasToDrop());

        migrator = createMigrator(rollbackChangeLog);
        migrator.futureRollbackSQL(this.contexts, writer);

//        System.out.println("Rollback SQL for future "+driverName+"\n\n"+writer.toString());
    }

    public void testTag() throws Exception {
        if (database == null) {
            return;
        }

        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll(getSchemasToDrop());

        migrator = createMigrator(completeChangeLog);
        migrator.update(this.contexts);

        migrator.tag("Test Tag");
    }

//    public void testGetDefaultDriver() throws Exception {
//        Migrator migrator = createMigrator(completeChangeLog);
//
//        assertEquals(driverName, migrator.getDatabase().getDefaultDriver(url));
//    }

    public void testDiff() throws Exception {
        if (database == null) {
            return;
        }

        runCompleteChangeLog();

        Diff diff = new Diff(database, database);
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
        if (database == null) {
            return;
        }

        runCompleteChangeLog();

        DatabaseSnapshot originalSnapshot = new DatabaseSnapshot(database);

        Diff diff = new Diff(database, (String) null);
        DiffResult diffResult = diff.compare();

        File tempFile = File.createTempFile("liquibase-test", ".xml");

        FileOutputStream output = new FileOutputStream(tempFile);
        try {
            diffResult.printChangeLog(new PrintStream(output), database);
            output.flush();
        } finally {
            output.close();
        }

        Migrator migrator = createMigrator(tempFile.getName());
        migrator.dropAll(getSchemasToDrop());

        //run again to test changelog testing logic
        migrator = createMigrator(tempFile.getName());
        try {
            migrator.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        tempFile.deleteOnExit();

        DatabaseSnapshot finalSnapshot = new DatabaseSnapshot(database);

        DiffResult finalDiffResult = new Diff(originalSnapshot, finalSnapshot).compare();
        assertEquals(0, finalDiffResult.getMissingColumns().size());
        assertEquals(0, finalDiffResult.getMissingForeignKeys().size());
        assertEquals(0, finalDiffResult.getMissingIndexes().size());
        assertEquals(0, finalDiffResult.getMissingPrimaryKeys().size());
        assertEquals(0, finalDiffResult.getMissingSequences().size());
        assertEquals(0, finalDiffResult.getMissingTables().size());
        assertEquals(0, finalDiffResult.getMissingViews().size());
        assertEquals(0, finalDiffResult.getUnexpectedColumns().size());
        assertEquals(0, finalDiffResult.getUnexpectedForeignKeys().size());
        assertEquals(0, finalDiffResult.getUnexpectedIndexes().size());
        assertEquals(0, finalDiffResult.getUnexpectedPrimaryKeys().size());
        assertEquals(0, finalDiffResult.getUnexpectedSequences().size());
        assertEquals(0, finalDiffResult.getUnexpectedTables().size());
        assertEquals(0, finalDiffResult.getUnexpectedViews().size());

    }

    public void testClearChecksums() throws Exception {
        if (database == null) {
            return;
        }

        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll(getSchemasToDrop());

        migrator = createMigrator(completeChangeLog);
        migrator.dropAll(getSchemasToDrop());

        migrator = createMigrator(completeChangeLog);
        migrator.update(this.contexts);

        migrator.clearCheckSums();
    }

    public void testTagEmptyDatabase() throws Exception {
        if (database == null) {
            return;
        }

        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll(getSchemasToDrop());

        migrator = createMigrator(completeChangeLog);
        migrator.checkDatabaseChangeLogTable();
        try {
            migrator.tag("empty");
        } catch (JDBCException e) {
            assertEquals("liquibase.exception.JDBCException: Cannot tag an empty database", e.getMessage());
        }

    }

    public void testUnrunChangeSetsEmptyDatabase() throws Exception {
        if (database == null) {
            return;
        }

        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll(getSchemasToDrop());

        migrator = createMigrator(completeChangeLog);
        List<ChangeSet> list = migrator.listUnrunChangeSets(this.contexts);

        assertTrue(list.size() > 0);

    }

    public void testAbsolutePathChangeLog() throws Exception {
        if (database == null) {
            return;
        }
        

        Enumeration<URL> urls = new JUnitFileOpener().getResources(includedChangeLog);
        URL completeChangeLogURL = urls.nextElement();

        String absolutePathOfChangeLog = completeChangeLogURL.toExternalForm();
        absolutePathOfChangeLog = absolutePathOfChangeLog.replaceFirst("file:\\/","");
        if (System.getProperty("os.name").startsWith("Windows ")) {
            absolutePathOfChangeLog = absolutePathOfChangeLog.replace('/', '\\');
        } else {
            absolutePathOfChangeLog = "/" + absolutePathOfChangeLog;
        }
        Migrator migrator = createMigrator(absolutePathOfChangeLog, new FileSystemFileOpener());
        migrator.dropAll(getSchemasToDrop());

        migrator.update(this.contexts);

        migrator.update(this.contexts); //try again, make sure there are no errors

        migrator.dropAll(getSchemasToDrop());
    }


    public void testRerunChangeLogOnDifferentSchema() throws Exception {
        if (database == null) {
            return;
        }

        if (!database.supportsSchemas()) {
            return;
        }

        runCompleteChangeLog();

        DatabaseConnection connection2 = TestContext.getInstance().getConnection(url);

        Database database2 = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection2);

        database2.setDefaultSchemaName("liquibaseb");

        { //this is ugly, but is a special case specific to this test
            Field changeLogTableExistsField = AbstractDatabase.class.getDeclaredField("changeLogTableExists");
            changeLogTableExistsField.setAccessible(true);
            changeLogTableExistsField.set(database2, false);

            Field changeLogCreateAttemptedField = AbstractDatabase.class.getDeclaredField("changeLogCreateAttempted");
            changeLogCreateAttemptedField.setAccessible(true);
            changeLogCreateAttemptedField.set(database2, false);

            Field changeLogLockTableExistsField = AbstractDatabase.class.getDeclaredField("changeLogLockTableExists");
            changeLogLockTableExistsField.setAccessible(true);
            changeLogLockTableExistsField.set(database2, false);

            Field changeLogLockCreateAttemptedField = AbstractDatabase.class.getDeclaredField("changeLogLockCreateAttempted");
            changeLogLockCreateAttemptedField.setAccessible(true);
            changeLogLockCreateAttemptedField.set(database2, false);

        }
        database2.checkDatabaseChangeLogTable();
        database2.dropDatabaseObjects(database2.getDefaultSchemaName());
        dropDatabaseChangeLogTable(database2.getDefaultSchemaName(), database2);

        JUnitFileOpener fileOpener = new JUnitFileOpener();
        Migrator migrator = new Migrator(completeChangeLog, fileOpener, database2);
        migrator.update(this.contexts);
    }

    private void dropDatabaseChangeLogTable(String schema, Database database) {
        try {
            database.getJdbcTemplate().execute(new DropTableStatement(schema, database.getDatabaseChangeLogTableName(), false));
        } catch (JDBCException e) {
            ; //ok
        }
    }
}
