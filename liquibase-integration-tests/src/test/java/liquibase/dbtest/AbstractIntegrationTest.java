package liquibase.dbtest;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.structure.Catalog;
import liquibase.database.structure.Schema;
import liquibase.database.structure.Table;
import liquibase.database.structure.View;
import liquibase.datatype.DataTypeFactory;
import liquibase.diff.DiffControl;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.output.DiffOutputConfig;
import liquibase.diff.output.DiffToChangeLog;
import liquibase.diff.output.DiffToPrintStream;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.LiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.snapshot.DatabaseSnapshotGenerator;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.executor.ExecutorService;
import liquibase.executor.Executor;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.lockservice.LockService;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.statement.core.DropTableStatement;
import liquibase.test.JUnitResourceAccessor;
import liquibase.test.TestContext;
import liquibase.test.DatabaseTestContext;
import liquibase.logging.LogFactory;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.sql.Statement;
import java.sql.SQLException;
import liquibase.assertions.DiffResultAssert;
import liquibase.util.RegexMatcher;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Base class for all database integration tests.  There is an AbstractIntegrationTest subclass for each supported database.
 * The database is assumed to exist at the host returned by getDatabaseServerHostname.  Currently this is hardcoded to an integration test server
 * at liquibase world headquarters.  Feel free to change the return value, but don't check it in.  We are going to improve the config of this at some point.
 */
public abstract class AbstractIntegrationTest {


    protected String completeChangeLog;
    private String rollbackChangeLog;
    private String includedChangeLog;
    private String encodingChangeLog;
    private String externalfkInitChangeLog;
    private String externalEntityChangeLog;
    private String externalEntityChangeLog2;
    private String invalidReferenceChangeLog;

    protected String contexts = "test, context-b";
    private Database database;
    private String url;

    protected AbstractIntegrationTest(String changelogDir, String url) throws Exception {
        LogFactory.setLoggingLevel("info");

        this.completeChangeLog = "changelogs/" + changelogDir + "/complete/root.changelog.xml";
        this.rollbackChangeLog = "changelogs/" + changelogDir + "/rollback/rollbackable.changelog.xml";
        this.includedChangeLog = "changelogs/" + changelogDir + "/complete/included.changelog.xml";
        this.encodingChangeLog = "changelogs/common/encoding.changelog.xml";
        this.externalfkInitChangeLog= "changelogs/common/externalfk.init.changelog.xml";
        this.externalEntityChangeLog= "changelogs/common/externalEntity.changelog.xml";
        this.externalEntityChangeLog2= "com/example/nonIncluded/externalEntity.changelog.xml";
        this.invalidReferenceChangeLog= "changelogs/common/invalid.reference.changelog.xml";

        this.url = url;

        ServiceLocator.getInstance().setResourceAccessor(TestContext.getInstance().getTestResourceAccessor());
    }

    private void openConnection(String url) throws Exception {
        DatabaseConnection connection = DatabaseTestContext.getInstance().getConnection(url);

        if (connection != null) {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        }
    }

    @Before
    public void setUp() throws Exception {

        openConnection(url);

        if (database != null) {
            if (!database.getConnection().getAutoCommit()) {
                database.rollback();
            }

            DatabaseSnapshotGeneratorFactory.resetAll();
            ExecutorService.getInstance().reset();
            LockService.resetAll();

            database.checkDatabaseChangeLogLockTable();

            if (database.getConnection() != null) {
                String dropTableCommand = "drop table " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(),database.getDatabaseChangeLogLockTableName());
                ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement().executeUpdate(dropTableCommand);
                database.commit();
            }

            DatabaseSnapshotGeneratorFactory.resetAll();
            LockService.getInstance(database).forceReleaseLock();
            database.dropDatabaseObjects(Schema.DEFAULT);

            if (database.supportsSchemas()) {
                database.dropDatabaseObjects(new Schema((String) null, DatabaseTestContext.ALT_SCHEMA));
            }

            if (supportsAltCatalogTests()) {
                if (database.supportsSchemas() && database.supportsCatalogs()) {
                    database.dropDatabaseObjects(new Schema(DatabaseTestContext.ALT_CATALOG, DatabaseTestContext.ALT_SCHEMA));
                } else if (database.supportsCatalogs()) {
                    database.dropDatabaseObjects(new Schema((String) null, DatabaseTestContext.ALT_SCHEMA));
                }
            }
            database.commit();
            DatabaseSnapshotGeneratorFactory.resetAll();

        }
    }

    protected boolean supportsAltCatalogTests() {
        return database.supportsCatalogs();
    }

    protected Properties createProperties() {
        return new Properties();
    }

    @After
    public void tearDown() throws Exception {
        if (database != null) {
            if (shouldRollBack()) {
                database.rollback();
            }
            ExecutorService.getInstance().clearExecutor(database);
            database.setDefaultSchemaName(null);
//            database.close();
        }
//        ServiceLocator.resetInternalState();
        DatabaseSnapshotGeneratorFactory.resetAll();
//        DatabaseFactory.resetInternalState();
    }

    protected boolean shouldRollBack() {
        return true;
    }

    protected Liquibase createLiquibase(String changeLogFile) throws Exception {
        CompositeResourceAccessor fileOpener = new CompositeResourceAccessor(new JUnitResourceAccessor(), new FileSystemResourceAccessor());
        return createLiquibase(changeLogFile, fileOpener);
    }

    private Liquibase createLiquibase(String changeLogFile, ResourceAccessor resourceAccessor) throws LiquibaseException {
        ExecutorService.getInstance().clearExecutor(database);
        database.resetInternalState();
        return new Liquibase(changeLogFile, resourceAccessor, database);
    }

    @Test
    public void testRunChangeLog() throws Exception {
        if (database == null) {
            return;
        }

        runCompleteChangeLog();
    }

    protected void runCompleteChangeLog() throws Exception {
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

    protected Schema[] getSchemasToDrop() throws DatabaseException {
        return new Schema[]{
                new Schema(new Catalog(null), database.escapeDatabaseObject("liquibaseb", Schema.class)),
                new Schema(new Catalog(null), database.getDefaultSchemaName())
        };
    }

    @Test
    public void runUpdateOnOldChangelogTableFormat() throws Exception {
        if (database == null) {
            return;
        }
        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase(liquibase);


        ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement().execute("CREATE TABLE DATABASECHANGELOG (id varchar(150) NOT NULL, " +
                "author varchar(150) NOT NULL, " +
                "filename varchar(255) NOT NULL, " +
                "dateExecuted "+ DataTypeFactory.getInstance().fromDescription("datetime").toDatabaseDataType(database) +" NOT NULL, " +
                "md5sum varchar(32), " +
                "description varchar(255), " +
                "comments varchar(255), " +
                "tag varchar(255), " +
                "liquibase varchar(10), " +
                "PRIMARY KEY(id, author, filename))");

        liquibase = createLiquibase(completeChangeLog);
        liquibase.update(this.contexts);

    }

    @Test
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
        System.out.println(outputResult);
        assertTrue("create databasechangelog command not found in: \n" + outputResult, outputResult.contains("CREATE TABLE "+database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())));
        assertTrue("create databasechangeloglock command not found in: \n" + outputResult, outputResult.contains("CREATE TABLE "+database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())));

        DatabaseSnapshot snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, new DiffControl());
        assertEquals(0, snapshot.getDatabaseObjects(Schema.DEFAULT, Table.class).size());
    }

    protected void clearDatabase(Liquibase liquibase) throws DatabaseException {
        liquibase.dropAll(getSchemasToDrop());
        Statement statement = null;
        try {
            statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
            try {
                statement.execute("drop table " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()));
            } catch (Exception e) {
                //ok
            }
            try {
                statement.execute("drop table " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName()));
            } catch (Exception e) {
                //ok
            }
            statement.close();
            database.commit();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }

        DatabaseSnapshotGeneratorFactory.resetAll();
        DatabaseFactory.reset();
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testDiff() throws Exception {
        if (database == null) {
            return;
        }

        runCompleteChangeLog();

        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(database, database, new DiffControl());

        try {
            assertTrue(diffResult.areEqual());
        } catch (AssertionError e) {
            new DiffToPrintStream(diffResult, System.out).print();
            throw e;
        }
    }

    @Test
    public void testRerunDiffChangeLog() throws Exception {
        if (database == null) {
            return;
        }

        for (int run=0; run < 2; run++) { //run once outputting data as insert, once as csv
            boolean outputCsv = run == 1;
            runCompleteChangeLog();

            DatabaseSnapshot originalSnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, new DiffControl());

            DiffControl diffControl = new DiffControl();
            diffControl.setDiffData(true);
            File tempFile = File.createTempFile("liquibase-test", ".xml");
            if (outputCsv) {
                diffControl.setDataDir(new File(tempFile.getParentFile(), "liquibase-data").getCanonicalPath().replaceFirst("\\w:",""));
            }

            DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(database, null, diffControl);


            FileOutputStream output = new FileOutputStream(tempFile);
            try {
                new DiffToChangeLog(diffResult, new DiffOutputConfig()).print(new PrintStream(output));
                output.flush();
            } finally {
                output.close();
            }


            Liquibase liquibase = createLiquibase(tempFile.getName());
            clearDatabase(liquibase);

            DatabaseSnapshot emptySnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, new DiffControl());

            //run again to test changelog testing logic
            liquibase = createLiquibase(tempFile.getName());
            try {
                liquibase.update(this.contexts);
            } catch (ValidationFailedException e) {
                e.printDescriptiveError(System.out);
                throw e;
            }

//            tempFile.deleteOnExit();

            DatabaseSnapshot migratedSnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, new DiffControl());

            DiffResult finalDiffResult = DiffGeneratorFactory.getInstance().compare(originalSnapshot, migratedSnapshot, new DiffControl());
            try {
                assertTrue(finalDiffResult.areEqual());
            } catch (AssertionError e) {
                new DiffToPrintStream(finalDiffResult, System.out).print();
                throw e;
            }

            //diff to empty and drop all
            DiffResult emptyDiffResult = DiffGeneratorFactory.getInstance().compare(emptySnapshot, migratedSnapshot, new DiffControl());
            output = new FileOutputStream(tempFile);
            try {
                new DiffToChangeLog(emptyDiffResult, new DiffOutputConfig(true, true, true)).print(new PrintStream(output));
                output.flush();
            } finally {
                output.close();
            }

            liquibase = createLiquibase(tempFile.getName());
            System.out.println("updating from "+tempFile.getCanonicalPath());
            try {
                liquibase.update(this.contexts);
            } catch (LiquibaseException e) {
                throw e;
            }

            DatabaseSnapshot emptyAgainSnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, new DiffControl());
            assertEquals(0, emptyAgainSnapshot.getDatabaseObjects(migratedSnapshot.getSchemas().iterator().next(), Table.class).size());
            assertEquals(0, emptyAgainSnapshot.getDatabaseObjects(migratedSnapshot.getSchemas().iterator().next(), View.class).size());
        }
    }

    @Test
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

        DatabaseSnapshot originalSnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, new DiffControl());

        DiffControl diffControl = new DiffControl(new DiffControl.SchemaComparison[]{new DiffControl.SchemaComparison(Schema.DEFAULT, new Schema(Catalog.DEFAULT, "liquibaseb"))});
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(database, database, diffControl);

        File tempFile = File.createTempFile("liquibase-test", ".xml");

        FileOutputStream output = new FileOutputStream(tempFile);
        try {
            new DiffToChangeLog(diffResult, new DiffOutputConfig()).print(new PrintStream(output));
            output.flush();
        } finally {
            output.close();
        }

        liquibase = createLiquibase(tempFile.getName());
        clearDatabase(liquibase);

        //run again to test changelog testing logic
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        try {
            executor.execute(new DropTableStatement("liquibaseb", "liquibaseb", database.getDatabaseChangeLogTableName(), false));
        } catch (DatabaseException e) {
            //ok
        }
        try {
            executor.execute(new DropTableStatement("liquibaseb", "liquibaseb", database.getDatabaseChangeLogLockTableName(), false));
        } catch (DatabaseException e) {
            //ok
        }
        database.commit();

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

        DatabaseSnapshot finalSnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, new DiffControl());

        DiffResult finalDiffResult = DiffGeneratorFactory.getInstance().compare(originalSnapshot, finalSnapshot, new DiffControl());
        new DiffToPrintStream(finalDiffResult, System.out).print();
        assertTrue(finalDiffResult.areEqual());
    }

    @Test
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

    @Test
    public void testTagEmptyDatabase() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(completeChangeLog);
        liquibase.checkDatabaseChangeLogTable(false, null, null);
        liquibase.tag("empty");

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.update(null);

        liquibase.rollback("empty", null);

    }

    @Test
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

    @Test
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


//    @Test
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

    private void dropDatabaseChangeLogTable(String catalog, String schema, Database database) {
        try {
            ExecutorService.getInstance().getExecutor(database).execute(new DropTableStatement(catalog, schema, database.getDatabaseChangeLogTableName(), false));
        } catch (DatabaseException e) {
            ; //ok
        }
    }

    @Test
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

    @Test
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

    @Test
    public void testDbDoc() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        liquibase.dropAll(getSchemasToDrop());

        liquibase = createLiquibase(completeChangeLog);
        liquibase.update(this.contexts);

        File outputDir = File.createTempFile("liquibase-dbdoctest", "dir");
        outputDir.delete();
        outputDir.mkdir();
        outputDir.deleteOnExit();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.generateDocumentation(outputDir.getAbsolutePath(), this.contexts);
    }


    @Test
    public void testEncodingUpdating2SQL() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(encodingChangeLog);

        StringWriter writer=new StringWriter();
        liquibase.update(this.contexts,writer);
        assertTrue("Update to SQL preserves encoding",
            new RegexMatcher(writer.toString(), new String[] {
                //For the UTF-8 encoded cvs
                "^.*INSERT.*VALUES.*àèìòùáéíóúÀÈÌÒÙÁÉÍÓÚâêîôûäëïöü.*$",
                "çñ®",
                //For the latin1 one
                "^.*INSERT.*VALUES.*àèìòùáéíóúÀÈÌÒÙÁÉÍÓÚâêîôûäëïöü.*$",
                "çñ®"
            }).allMatchedInSequentialOrder());
    }

//    @Test
//    public void testEncondingUpdatingDatabase() throws Exception {
//        if (database == null) {
//            return;
//        }
//        
//        // First import some data from utf8 encoded csv
//        // and create a snapshot
//        Liquibase liquibase = createLiquibase("changelogs/common/encoding.utf8.changelog.xml");
//        liquibase.update(this.contexts);
//        DatabaseSnapshot utf8Snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);
//
//        clearDatabase(liquibase);
//
//        // Second import some data from latin1 encoded csv
//        // and create a snapshot
//        liquibase = createLiquibase("changelogs/common/encoding.latin1.changelog.xml");
//        liquibase.update(this.contexts);
//        DatabaseSnapshot iso88951Snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, null, null);
//
//        //TODO: We need better data diff support to be able to do that
//        //Diff diff = new Diff(utf8Snapshot,iso88951Snapshot);
//        //diff.setDiffData(true);
//        //assertFalse("There are no differences setting the same data in utf-8 and iso-8895-1 "
//        //        ,diff.compare().areEqual());
//
//        //For now we do an approach reading diff data
//        DiffResult[] diffGenerators =new DiffResult[2];
//        diffGenerators[0]= DiffGeneratorFactory(utf8Snapshot,iso88951Snapshot);
//        diffGenerators[0].setDiffData(true);
//        diffGenerators[1]= new DiffGeneratorFactory(iso88951Snapshot,utf8Snapshot);
//        diffGenerators[1].setDiffData(true);
//        for(DiffGeneratorFactory diffGenerator : diffGenerators) {
//            File tempFile = File.createTempFile("liquibase-test", ".xml");
//            tempFile.deleteOnExit();
//            FileOutputStream output=new FileOutputStream(tempFile);
//            diffGenerator.compare().print(new PrintStream(output,false,"UTF-8"),database);
//            output.close();
//            String diffOutput=StreamUtil.getStreamContents(new FileInputStream(tempFile),"UTF-8");
//            assertTrue("Update to SQL preserves encoding",
//                new RegexMatcher(diffOutput, new String[] {
//                    //For the UTF-8 encoded cvs
//                    "value=\"àèìòùáéíóúÀÈÌÒÙÁÉÍÓÚâêîôûäëïöü\"",
//                    "value=\"çñ®\""
//                }).allMatchedInSequentialOrder());
//        }
//
//    }

    /**
     * Test that diff is capable to detect foreign keys to external schemas that doesn't appear in the changelog
     */
   @Test
   public void testDiffExternalForeignKeys() throws Exception {
       if (database == null) {
           return;
       }
       Liquibase liquibase = createLiquibase(externalfkInitChangeLog);
       liquibase.update(contexts);

       DiffResult diffResult = liquibase.diff(database, null, new DiffControl());
       DiffResultAssert.assertThat(diffResult).containsMissingForeignKeyWithName("fk_person_country");
   }

    @Test
    public void invalidIncludeDoesntBreakLiquibase() throws Exception{
        if (database == null) {
            return;
        }
        Liquibase liquibase = createLiquibase(invalidReferenceChangeLog);
        try {
            liquibase.update(null);
            fail("Did not fail with invalid include");
        } catch (ChangeLogParseException ignored) {
            //expected
        }

        assertFalse(LockService.getInstance(database).hasChangeLogLock());
    }

    @Test
    public void contextsWithHyphensWorkInFormattedSql() throws Exception {
        if (database == null) {
            return;
        }
        Liquibase liquibase = createLiquibase("changelogs/common/sqlstyle/formatted.changelog.sql");
        liquibase.update("hyphen-context-using-sql,camelCaseContextUsingSql");

        DatabaseSnapshotGenerator snapshot = DatabaseSnapshotGeneratorFactory.getInstance().getGenerator(database);
        assertNotNull(snapshot.hasTable(null, "hyphen_context", database));
        assertNotNull(snapshot.hasTable(null, "camel_context", database));
        assertNotNull(snapshot.hasTable(null, "bar_id", database));
        assertNotNull(snapshot.hasTable(null, "foo_id", database));
    }

//   @Test
//   public void testXMLInclude() throws Exception{
//       if (database == null) {
//            return;
//       }
//       //Test external entity with a standard class loaded resource
//       Liquibase liquibase = createLiquibase(externalEntityChangeLog);
//       liquibase.update(contexts);
//
//       //Test with a packaged one
//       liquibase = createLiquibase(externalEntityChangeLog2);
//       liquibase.update(contexts);
//   }

    public static String getDatabaseServerHostname(String databaseManager)  {
        try {
            Properties integrationTestProperties;
            integrationTestProperties = new Properties();
            integrationTestProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.properties"));
            InputStream localProperties=Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.local.properties");
            if(localProperties!=null)
                integrationTestProperties.load(localProperties);

            String host=integrationTestProperties.getProperty("integration.test."+databaseManager+".hostname");
            if(host==null)
                host=integrationTestProperties.getProperty("integration.test.hostname");
            return host;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Database getDatabase(){
        return database;
    }
}
