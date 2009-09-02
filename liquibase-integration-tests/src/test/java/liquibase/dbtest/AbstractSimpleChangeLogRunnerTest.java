package liquibase.dbtest;

import junit.framework.TestCase;
import liquibase.Liquibase;
import liquibase.servicelocator.ServiceLocator;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.executor.ExecutorService;
import liquibase.executor.Executor;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.JdbcConnection;
import liquibase.diff.Diff;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.lockservice.LockService;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.statement.core.DropTableStatement;
import liquibase.test.JUnitResourceAccessor;
import liquibase.test.TestContext;
import liquibase.test.DatabaseTestContext;
import liquibase.logging.LogFactory;
import liquibase.logging.LogLevel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public abstract class AbstractSimpleChangeLogRunnerTest extends TestCase {

    protected String completeChangeLog;
    private String rollbackChangeLog;
    private String includedChangeLog;
    protected String contexts = "test, context-b";
    private Database database;
    private String url;

    protected AbstractSimpleChangeLogRunnerTest(String changelogDir, String url) throws Exception {
        LogFactory.setLoggingLevel("severe");

        this.completeChangeLog = "changelogs/" + changelogDir + "/complete/root.changelog.xml";
        this.rollbackChangeLog = "changelogs/" + changelogDir + "/rollback/rollbackable.changelog.xml";
        this.includedChangeLog = "changelogs/" + changelogDir + "/complete/included.changelog.xml";

        this.url = url;

        ServiceLocator.getInstance().setResourceAccessor(TestContext.getInstance().getTestResourceAccessor());
        
        DatabaseConnection connection = DatabaseTestContext.getInstance().getConnection(url);

        LogFactory.getLogger().setLogLevel(LogLevel.DEBUG);
        if (connection != null) {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        if (database != null) {
            if (!database.getConnection().getAutoCommit()) {
                database.rollback();
            }

            LockService.getInstance(database).forceReleaseLock();
            if (database.supportsSchemas()) {
                database.dropDatabaseObjects(DatabaseTestContext.ALT_SCHEMA);
            }
            database.dropDatabaseObjects(null);
            database.commit();
        }
    }

    protected Properties createProperties() {
        return new Properties();
    }

    @Override
    protected void tearDown() throws Exception {
        if (database != null) {
            if (shouldRollBack()) {
                database.rollback();
            }
            ExecutorService.getInstance().clearExecutor(database);
            database.setDefaultSchemaName(null);
        }
        super.tearDown();
    }

    protected boolean shouldRollBack() {
        return true;
    }

    protected Liquibase createLiquibase(String changeLogFile) throws Exception {
        JUnitResourceAccessor fileOpener = new JUnitResourceAccessor();
        return createLiquibase(changeLogFile, fileOpener);
    }

    private Liquibase createLiquibase(String changeLogFile, ResourceAccessor resourceAccessor) throws DatabaseException {
        ExecutorService.getInstance().clearExecutor(database);
        return new Liquibase(changeLogFile, resourceAccessor, database);
    }

    public void testRunChangeLog() throws Exception {
        if (database == null) {
            return;
        }

        runCompleteChangeLog();
    }

    private void runCompleteChangeLog() throws Exception {
        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase(liquibase);

        //run again to test changelog testing logic
        liquibase = createLiquibase(completeChangeLog);
        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

    protected String[] getSchemasToDrop() throws DatabaseException {
        return new String[]{
                "liquibaseb".toUpperCase(),
                database.getDefaultSchemaName(),
        };
    }

    public void testOutputChangeLog() throws Exception {
        if (database == null) {
            return;
        }

        StringWriter output = new StringWriter();
        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(completeChangeLog);
        liquibase.update(this.contexts, output);

        String outputResult = output.getBuffer().toString();
        assertNotNull(outputResult);
        assertTrue(outputResult.length() > 100); //should be pretty big
//        System.out.println(outputResult);
        assertTrue("create databasechangelog command not found", outputResult.contains("CREATE TABLE DATABASECHANGELOG"));

        DatabaseSnapshot snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);
        assertEquals(0, snapshot.getTables().size());
    }

    private void clearDatabase(Liquibase liquibase) throws DatabaseException {
        liquibase.dropAll(getSchemasToDrop());
        try {
            ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement().execute("drop table "+database.getDatabaseChangeLogTableName());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement().execute("drop table "+database.getDatabaseChangeLogLockTableName());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void testUpdateTwice() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(completeChangeLog);
        liquibase.update(this.contexts);
        liquibase.update(this.contexts);
    }

    public void testRollbackableChangeLog() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(rollbackChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.update(this.contexts);

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.rollback(new Date(0), this.contexts);

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.update(this.contexts);

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.rollback(new Date(0), this.contexts);
    }

    public void testRollbackableChangeLogScriptOnExistingDatabase() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(rollbackChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.update(this.contexts);

        StringWriter writer = new StringWriter();

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.rollback(new Date(0), this.contexts, writer);

//        System.out.println("Rollback SQL for "+driverName+StreamUtil.getLineSeparator()+StreamUtil.getLineSeparator()+writer.toString());
    }

    public void testRollbackableChangeLogScriptOnFutureDatabase() throws Exception {
        if (database == null) {
            return;
        }

        StringWriter writer = new StringWriter();

        Liquibase liquibase = createLiquibase(rollbackChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.futureRollbackSQL(this.contexts, writer);

//        System.out.println("Rollback SQL for future "+driverName+"\n\n"+writer.toString());
    }

    public void testTag() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(completeChangeLog);
        liquibase.update(this.contexts);

        liquibase.tag("Test Tag");
    }

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

        DatabaseSnapshot originalSnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);

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

        Liquibase liquibase = createLiquibase(tempFile.getName());
        clearDatabase(liquibase);

        DatabaseSnapshot emptySnapshot= DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);

        //run again to test changelog testing logic
        liquibase = createLiquibase(tempFile.getName());
        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        tempFile.deleteOnExit();

        DatabaseSnapshot migratedSnapshot= DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);

        DiffResult finalDiffResult = new Diff(originalSnapshot, migratedSnapshot).compare();
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

        //diff to empty and drop all
        Diff emptyDiff = new Diff(emptySnapshot, migratedSnapshot);
        DiffResult emptyDiffResult = emptyDiff.compare();
        output = new FileOutputStream(tempFile);
        try {
            emptyDiffResult.printChangeLog(new PrintStream(output), database);
            output.flush();
        } finally {
            output.close();
        }

        liquibase = createLiquibase(tempFile.getName());
        liquibase.update(this.contexts);

        DatabaseSnapshot emptyAgainSnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);
        assertEquals(0, emptyAgainSnapshot.getTables().size());
        assertEquals(0, emptyAgainSnapshot.getViews().size());
    }

    public void testRerunDiffChangeLogAltSchema() throws Exception {
        if (database == null) {
            return;
        }
        if (!database.supportsSchemas()) {
            return;
        }

        Liquibase liquibase = createLiquibase(includedChangeLog);
        clearDatabase(liquibase);

        database.setDefaultSchemaName("liquibaseb");

        LockService.getInstance(database).forceReleaseLock();

        liquibase.update(includedChangeLog);

        DatabaseSnapshot originalSnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);

        Diff diff = new Diff(database, "liquibaseb");
        DiffResult diffResult = diff.compare();

        File tempFile = File.createTempFile("liquibase-test", ".xml");

        FileOutputStream output = new FileOutputStream(tempFile);
        try {
            diffResult.printChangeLog(new PrintStream(output), database);
            output.flush();
        } finally {
            output.close();
        }

        liquibase = createLiquibase(tempFile.getName());
        clearDatabase(liquibase);

        //run again to test changelog testing logic
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        if (!database.isPeculiarLiquibaseSchema()) {
            executor.execute(new DropTableStatement("liquibaseb", database.getDatabaseChangeLogTableName(), false));
            executor.execute(new DropTableStatement("liquibaseb", database.getDatabaseChangeLogLockTableName(), false));
            database.commit();
        }

        DatabaseConnection connection = DatabaseTestContext.getInstance().getConnection(url);
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        database.setDefaultSchemaName("liquibaseb");
        liquibase = createLiquibase(tempFile.getName());
        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        tempFile.deleteOnExit();

        DatabaseSnapshot finalSnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);

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

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(completeChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(completeChangeLog);
        liquibase.update(this.contexts);

        liquibase.clearCheckSums();
    }

    public void testTagEmptyDatabase() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(completeChangeLog);
        liquibase.checkDatabaseChangeLogTable();
        liquibase.tag("empty");
        
        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.update(null);

        liquibase.rollback("empty", null);

    }

    public void testUnrunChangeSetsEmptyDatabase() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(completeChangeLog);
        List<ChangeSet> list = liquibase.listUnrunChangeSets(this.contexts);

        assertTrue(list.size() > 0);

    }

    public void testAbsolutePathChangeLog() throws Exception {
        if (database == null) {
            return;
        }


        Enumeration<URL> urls = new JUnitResourceAccessor().getResources(includedChangeLog);
        URL completeChangeLogURL = urls.nextElement();

        String absolutePathOfChangeLog = completeChangeLogURL.toExternalForm();
        absolutePathOfChangeLog = absolutePathOfChangeLog.replaceFirst("file:\\/", "");
        if (System.getProperty("os.name").startsWith("Windows ")) {
            absolutePathOfChangeLog = absolutePathOfChangeLog.replace('/', '\\');
        } else {
            absolutePathOfChangeLog = "/" + absolutePathOfChangeLog;
        }
        Liquibase liquibase = createLiquibase(absolutePathOfChangeLog, new FileSystemResourceAccessor());
        clearDatabase(liquibase);

        liquibase.update(this.contexts);

        liquibase.update(this.contexts); //try again, make sure there are no errors

        clearDatabase(liquibase);
    }


//    public void testRerunChangeLogOnDifferentSchema() throws Exception {
//        if (database == null) {
//            return;
//        }
//
//        if (!database.supportsSchemas()) {
//            return;
//        }
//
//        runCompleteChangeLog();
//
//        DatabaseConnection connection2 = TestContext.getWriteExecutor().getConnection(url);
//
//        Database database2 = DatabaseFactory.getWriteExecutor().findCorrectDatabaseImplementation(connection2);
//
//        database2.setDefaultSchemaName("liquibaseb");
//
//        { //this is ugly, but is a special case specific to this test
//            Field changeLogTableExistsField = AbstractDatabase.class.getDeclaredField("changeLogTableExists");
//            changeLogTableExistsField.setAccessible(true);
//            changeLogTableExistsField.set(database2, false);
//
//            Field changeLogCreateAttemptedField = AbstractDatabase.class.getDeclaredField("changeLogCreateAttempted");
//            changeLogCreateAttemptedField.setAccessible(true);
//            changeLogCreateAttemptedField.set(database2, false);
//
//            Field changeLogLockTableExistsField = AbstractDatabase.class.getDeclaredField("changeLogLockTableExists");
//            changeLogLockTableExistsField.setAccessible(true);
//            changeLogLockTableExistsField.set(database2, false);
//
//            Field changeLogLockCreateAttemptedField = AbstractDatabase.class.getDeclaredField("changeLogLockCreateAttempted");
//            changeLogLockCreateAttemptedField.setAccessible(true);
//            changeLogLockCreateAttemptedField.set(database2, false);
//
//        }
//        database2.checkDatabaseChangeLogTable();
//        database2.dropDatabaseObjects(database2.getDefaultSchemaName());
//        dropDatabaseChangeLogTable(database2.getDefaultSchemaName(), database2);
//
//        JUnitResourceAccessor resourceAccessor = new JUnitResourceAccessor();
//        Liquibase liquibase = new Liquibase(completeChangeLog, resourceAccessor, database2);
//        liquibase.update(this.contexts);
//    }

    private void dropDatabaseChangeLogTable(String schema, Database database) {
        try {
            ExecutorService.getInstance().getExecutor(database).execute(new DropTableStatement(schema, database.getDatabaseChangeLogTableName(), false));
        } catch (DatabaseException e) {
            ; //ok
        }
    }

    public void testExecuteExtChangelog() throws Exception {
        if (database == null) {
            return;
        }

        try {
            String extChangelog = "changelogs/common/ext.changelog.xml";
            Liquibase liquibase = createLiquibase(extChangelog);
            clearDatabase(liquibase);

            //run again to test changelog testing logic
            liquibase = createLiquibase(extChangelog);
            try {
                liquibase.update(this.contexts);
            } catch (ValidationFailedException e) {
                e.printDescriptiveError(System.out);
                throw e;
            }
        } finally {
            ServiceLocator.reset();
        }
    }

    public void testRollbackToChange() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(rollbackChangeLog);
        liquibase.dropAll(getSchemasToDrop());

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.update(this.contexts);

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.rollback(8, this.contexts);

        liquibase.tag("testRollbackToChange");

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.update(this.contexts);

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.rollback("testRollbackToChange", this.contexts);
    }
}
