package liquibase.dbtest;

import junit.framework.TestCase;
import liquibase.ChangeSet;
import liquibase.FileOpener;
import liquibase.FileSystemFileOpener;
import liquibase.Liquibase;
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

    protected Liquibase createLiquibase(String changeLogFile) throws Exception {
        JUnitFileOpener fileOpener = new JUnitFileOpener();
        return createLiquibase(changeLogFile, fileOpener);
    }

    private Liquibase createLiquibase(String changeLogFile, FileOpener fileOpener) throws JDBCException {
        return new Liquibase(changeLogFile, fileOpener, database);
    }

    public void testRunChangeLog() throws Exception {
        if (database == null) {
            return;
        }
        
        runCompleteChangeLog();
    }

    private void runCompleteChangeLog() throws Exception {
        Liquibase liquibase = createLiquibase(completeChangeLog);
        liquibase.dropAll(getSchemasToDrop());

        //run again to test changelog testing logic
        liquibase = createLiquibase(completeChangeLog);
        try {
            liquibase.update(this.contexts);
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
        Liquibase liquibase = createLiquibase(completeChangeLog);
        liquibase.dropAll(getSchemasToDrop());

        liquibase = createLiquibase(completeChangeLog);
        liquibase.update(this.contexts, output);

//        System.out.println(output.getBuffer().toString());
    }

    public void testRollbackableChangeLog() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(rollbackChangeLog);
        liquibase.dropAll(getSchemasToDrop());

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
        liquibase.dropAll(getSchemasToDrop());

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
        liquibase.dropAll(getSchemasToDrop());

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.futureRollbackSQL(this.contexts, writer);

//        System.out.println("Rollback SQL for future "+driverName+"\n\n"+writer.toString());
    }

    public void testTag() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        liquibase.dropAll(getSchemasToDrop());

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

        DatabaseSnapshot originalSnapshot = database.createDatabaseSnapshot(null, null);

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
        liquibase.dropAll(getSchemasToDrop());

        DatabaseSnapshot emptySnapshot = database.createDatabaseSnapshot(null, null);

        //run again to test changelog testing logic
        liquibase = createLiquibase(tempFile.getName());
        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        tempFile.deleteOnExit();

        DatabaseSnapshot migratedSnapshot = database.createDatabaseSnapshot(null, null);

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

        DatabaseSnapshot emptyAgainSnapshot = database.createDatabaseSnapshot(null, null);
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
        liquibase.dropAll(getSchemasToDrop());

        database.setDefaultSchemaName("liquibaseb");
        
        liquibase.update(includedChangeLog);

        DatabaseSnapshot originalSnapshot = database.createDatabaseSnapshot(null, null);

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
        liquibase.dropAll(getSchemasToDrop());

        //run again to test changelog testing logic
        database.getJdbcTemplate().execute(new DropTableStatement("liquibaseb", database.getDatabaseChangeLogTableName(), false));
        database.getJdbcTemplate().execute(new DropTableStatement("liquibaseb", database.getDatabaseChangeLogLockTableName(), false));
        database.commit();

        DatabaseConnection connection = TestContext.getInstance().getConnection(url);
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

        DatabaseSnapshot finalSnapshot = database.createDatabaseSnapshot(null, null);

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
        liquibase.dropAll(getSchemasToDrop());

        liquibase = createLiquibase(completeChangeLog);
        liquibase.dropAll(getSchemasToDrop());

        liquibase = createLiquibase(completeChangeLog);
        liquibase.update(this.contexts);

        liquibase.clearCheckSums();
    }

    public void testTagEmptyDatabase() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        liquibase.dropAll(getSchemasToDrop());

        liquibase = createLiquibase(completeChangeLog);
        liquibase.checkDatabaseChangeLogTable();
        try {
            liquibase.tag("empty");
        } catch (JDBCException e) {
            assertEquals("liquibase.exception.JDBCException: Cannot tag an empty database", e.getMessage());
        }

    }

    public void testUnrunChangeSetsEmptyDatabase() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        liquibase.dropAll(getSchemasToDrop());

        liquibase = createLiquibase(completeChangeLog);
        List<ChangeSet> list = liquibase.listUnrunChangeSets(this.contexts);

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
        Liquibase liquibase = createLiquibase(absolutePathOfChangeLog, new FileSystemFileOpener());
        liquibase.dropAll(getSchemasToDrop());

        liquibase.update(this.contexts);

        liquibase.update(this.contexts); //try again, make sure there are no errors

        liquibase.dropAll(getSchemasToDrop());
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
//        DatabaseConnection connection2 = TestContext.getInstance().getConnection(url);
//
//        Database database2 = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection2);
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
//        JUnitFileOpener fileOpener = new JUnitFileOpener();
//        Liquibase liquibase = new Liquibase(completeChangeLog, fileOpener, database2);
//        liquibase.update(this.contexts);
//    }

    private void dropDatabaseChangeLogTable(String schema, Database database) {
        try {
            database.getJdbcTemplate().execute(new DropTableStatement(schema, database.getDatabaseChangeLogTableName(), false));
        } catch (JDBCException e) {
            ; //ok
        }
    }
}
