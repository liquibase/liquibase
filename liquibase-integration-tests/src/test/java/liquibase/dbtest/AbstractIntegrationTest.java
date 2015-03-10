package liquibase.dbtest;

import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.core.OracleDatabase;
import liquibase.structure.core.*;
import liquibase.test.DiffResultAssert;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.report.DiffToReport;
import liquibase.snapshot.*;
import liquibase.datatype.DataTypeFactory;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.LiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.executor.ExecutorService;
import liquibase.executor.Executor;
import liquibase.diff.DiffResult;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogFactory;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.statement.core.DropTableStatement;
import liquibase.test.DatabaseTestContext;
import liquibase.test.JUnitResourceAccessor;
import liquibase.test.TestContext;
import liquibase.util.FileUtil;
import liquibase.util.RegexMatcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertFalse;

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
    private String objectQuotingStrategyChangeLog;

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
        this.objectQuotingStrategyChangeLog = "changelogs/common/object.quoting.strategy.changelog.xml";

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

            SnapshotGeneratorFactory.resetAll();
            ExecutorService.getInstance().reset();

            LockServiceFactory.getInstance().resetAll();
            LockServiceFactory.getInstance().getLockService(database).init();


            ChangeLogHistoryServiceFactory.getInstance().resetAll();

            if (database.getConnection() != null) {
                ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement().executeUpdate("drop table "+database.getDatabaseChangeLogLockTableName());
                database.commit();
            }

            SnapshotGeneratorFactory.resetAll();
            LockService lockService = LockServiceFactory.getInstance().getLockService(database);
            database.dropDatabaseObjects(CatalogAndSchema.DEFAULT);

            if (database.supportsSchemas()) {
                database.dropDatabaseObjects(new CatalogAndSchema((String) null, DatabaseTestContext.ALT_SCHEMA));
            }

            if (supportsAltCatalogTests()) {
                if (database.supportsSchemas() && database.supportsCatalogs()) {
                    database.dropDatabaseObjects(new CatalogAndSchema(DatabaseTestContext.ALT_CATALOG, DatabaseTestContext.ALT_SCHEMA));
                } else if (database.supportsCatalogs()) {
                    database.dropDatabaseObjects(new CatalogAndSchema(DatabaseTestContext.ALT_SCHEMA, null));
                }
            }
            database.commit();
            SnapshotGeneratorFactory.resetAll();

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
            database.setOutputDefaultCatalog(true);
            database.setOutputDefaultSchema(true);
//            database.close();
        }
//        ServiceLocator.resetInternalState();
        SnapshotGeneratorFactory.resetAll();
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
        runChangeLogFile(completeChangeLog);
    }

    protected void runChangeLogFile(String changeLogFile) throws Exception {
        Liquibase liquibase = createLiquibase(changeLogFile);
        clearDatabase(liquibase);

        //run again to test changelog testing logic
        liquibase = createLiquibase(changeLogFile);
        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }
    }

    protected CatalogAndSchema[] getSchemasToDrop() throws DatabaseException {
        return new CatalogAndSchema[]{
                new CatalogAndSchema(database.correctObjectName("lbcat2", Catalog.class), database.correctObjectName("lbschem2", Schema.class)),
                new CatalogAndSchema(null, database.getDefaultSchemaName())
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
                "dateExecuted "+ DataTypeFactory.getInstance().fromDescription("datetime", database).toDatabaseDataType(database) +" NOT NULL, " +
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

        assertTrue(outputResult.contains("€"));
        assertTrue(outputResult.contains("€"));

        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));
        assertEquals(0, snapshot.get(Schema.class).iterator().next().getDatabaseObjects(Table.class).size());
    }

    protected void clearDatabase(Liquibase liquibase) throws DatabaseException {
        liquibase.dropAll(getSchemasToDrop());
        try {
            Statement statement = null;
            try {
                statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
                statement.execute("drop table " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()));
                database.commit();
            } catch (Exception e) {
                System.out.println("Probably expected error dropping databasechangelog table");
                e.printStackTrace();
                database.rollback();
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }
            try {
                statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
                statement.execute("drop table " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName()));
                database.commit();
            } catch (Exception e) {
                System.out.println("Probably expected error dropping databasechangeloglock table");
                e.printStackTrace();
                database.rollback();
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }

        SnapshotGeneratorFactory.resetAll();
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
    public void testUpdateClearUpdate() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(completeChangeLog);
        liquibase.update(this.contexts);
        clearDatabase(liquibase);

        liquibase = createLiquibase(completeChangeLog);
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

        CompareControl compareControl = new CompareControl();
        compareControl.addSuppressedField(Column.class, "defaultValue");  //database returns different data even if the same
        compareControl.addSuppressedField(Column.class, "autoIncrementInformation"); //database returns different data even if the same
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(database, database, compareControl);

        try {
            assertTrue(diffResult.areEqual());
        } catch (AssertionError e) {
            new DiffToReport(diffResult, System.err).print();
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

            SnapshotControl snapshotControl = new SnapshotControl(database);
//todo            compareControl.setDiffData(true);

            DatabaseSnapshot originalSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, snapshotControl);

            CompareControl compareControl = new CompareControl();
            compareControl.addSuppressedField(Column.class, "defaultValue");  //database returns different data even if the same
            compareControl.addSuppressedField(Column.class, "autoIncrementInformation"); //database returns different data even if the same
            if (database instanceof OracleDatabase) {
                compareControl.addSuppressedField(Column.class, "type"); //database returns different nvarchar2 info even though they are the same
            }

            DiffOutputControl diffOutputControl = new DiffOutputControl();
            File tempFile = File.createTempFile("liquibase-test", ".xml");
            FileUtil.deleteOnExit(tempFile);
            if (outputCsv) {
                diffOutputControl.setDataDir(new File(tempFile.getParentFile(), "liquibase-data").getCanonicalPath().replaceFirst("\\w:",""));
            }

            DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(database, null, compareControl);


            FileOutputStream output = new FileOutputStream(tempFile);
            try {
                new DiffToChangeLog(diffResult, new DiffOutputControl()).print(new PrintStream(output));
                output.flush();
            } finally {
                output.close();
            }

            Liquibase liquibase = createLiquibase(tempFile.getName());
            clearDatabase(liquibase);

            DatabaseSnapshot emptySnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));

            //run again to test changelog testing logic
            liquibase = createLiquibase(tempFile.getName());
            try {
                liquibase.update(this.contexts);
            } catch (ValidationFailedException e) {
                e.printDescriptiveError(System.out);
                throw e;
            }

//            tempFile.deleteOnExit();

            DatabaseSnapshot migratedSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));

            DiffResult finalDiffResult = DiffGeneratorFactory.getInstance().compare(originalSnapshot, migratedSnapshot, compareControl);
            try {
                assertTrue(finalDiffResult.areEqual());
            } catch (AssertionError e) {
                new DiffToReport(finalDiffResult, System.err).print();
                throw e;
            }

            //diff to empty and drop all
            DiffResult emptyDiffResult = DiffGeneratorFactory.getInstance().compare(emptySnapshot, migratedSnapshot, compareControl);
            output = new FileOutputStream(tempFile);
            try {
                new DiffToChangeLog(emptyDiffResult, new DiffOutputControl(true, true, true)).print(new PrintStream(output));
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

            DatabaseSnapshot emptyAgainSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));
            assertEquals(2, emptyAgainSnapshot.get(Table.class).size());
            assertEquals(0, emptyAgainSnapshot.get(View.class).size());
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

        database.setDefaultSchemaName("lbcat2");

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.forceReleaseLock();

        liquibase.update(includedChangeLog);

        DatabaseSnapshot originalSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));

        CompareControl compareControl = new CompareControl(new CompareControl.SchemaComparison[]{new CompareControl.SchemaComparison(CatalogAndSchema.DEFAULT, new CatalogAndSchema(null, "lbcat2"))}, originalSnapshot.getSnapshotControl().getTypesToInclude());
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(database, null, compareControl);

        File tempFile = File.createTempFile("liquibase-test", ".xml");

        FileOutputStream output = new FileOutputStream(tempFile);
        try {
            new DiffToChangeLog(diffResult, new DiffOutputControl()).print(new PrintStream(output));
            output.flush();
        } finally {
            output.close();
        }

        liquibase = createLiquibase(tempFile.getName());
        clearDatabase(liquibase);

        //run again to test changelog testing logic
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        try {
            executor.execute(new DropTableStatement("lbcat2", "lbcat2", database.getDatabaseChangeLogTableName(), false));
        } catch (DatabaseException e) {
            //ok
        }
        try {
            executor.execute(new DropTableStatement("lbcat2", "lbcat2", database.getDatabaseChangeLogLockTableName(), false));
        } catch (DatabaseException e) {
            //ok
        }
        database.commit();

        DatabaseConnection connection = DatabaseTestContext.getInstance().getConnection(url);
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        database.setDefaultSchemaName("lbcat2");
        liquibase = createLiquibase(tempFile.getName());
        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

        tempFile.deleteOnExit();

        DatabaseSnapshot finalSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));

        CompareControl finalCompareControl = new CompareControl();
        finalCompareControl.addSuppressedField(Column.class, "autoIncrementInformation");
        DiffResult finalDiffResult = DiffGeneratorFactory.getInstance().compare(originalSnapshot, finalSnapshot, finalCompareControl);
        new DiffToReport(finalDiffResult, System.out).print();
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
        liquibase.checkLiquibaseTables(false, null, new Contexts(), new LabelExpression());
        liquibase.tag("empty");

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.update(new Contexts());

        liquibase.rollback("empty", new Contexts());

    }

    @Test
    public void testUnrunChangeSetsEmptyDatabase() throws Exception {
        if (database == null) {
            return;
        }

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(completeChangeLog);
        List<ChangeSet> list = liquibase.listUnrunChangeSets(new Contexts(this.contexts), new LabelExpression());

        assertTrue(list.size() > 0);

    }

    @Test
    public void testAbsolutePathChangeLog() throws Exception {
        if (database == null) {
            return;
        }


        Set<String> urls = new JUnitResourceAccessor().list(null, includedChangeLog, true, false, true);
        String absolutePathOfChangeLog = urls.iterator().next();

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
//            Field changeLogTableExistsField = AbstractJdbcDatabase.class.getDeclaredField("changeLogTableExists");
//            changeLogTableExistsField.setAccessible(true);
//            changeLogTableExistsField.set(database2, false);
//
//            Field changeLogCreateAttemptedField = AbstractJdbcDatabase.class.getDeclaredField("changeLogCreateAttempted");
//            changeLogCreateAttemptedField.setAccessible(true);
//            changeLogCreateAttemptedField.set(database2, false);
//
//            Field changeLogLockTableExistsField = AbstractJdbcDatabase.class.getDeclaredField("changeLogLockTableExists");
//            changeLogLockTableExistsField.setAccessible(true);
//            changeLogLockTableExistsField.set(database2, false);
//
//            Field changeLogLockCreateAttemptedField = AbstractJdbcDatabase.class.getDeclaredField("changeLogLockCreateAttempted");
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

        liquibase = createLiquibase(completeChangeLog);
        liquibase.generateDocumentation(outputDir.getAbsolutePath(), this.contexts);

        FileUtil.deleteOnExit(outputDir);
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

       DiffResult diffResult = liquibase.diff(database, null, new CompareControl());
       DiffResultAssert.assertThat(diffResult).containsMissingForeignKeyWithName("fk_person_country");
   }

    @Test
    public void invalidIncludeDoesntBreakLiquibase() throws Exception{
        if (database == null) {
            return;
        }
        Liquibase liquibase = createLiquibase(invalidReferenceChangeLog);
        try {
            liquibase.update(new Contexts());
            fail("Did not fail with invalid include");
        } catch (ChangeLogParseException ignored) {
            //expected
        }

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        assertFalse(lockService.hasChangeLogLock());
    }

    @Test
    public void contextsWithHyphensWorkInFormattedSql() throws Exception {
        if (database == null) {
            return;
        }
        Liquibase liquibase = createLiquibase("changelogs/common/sqlstyle/formatted.changelog.sql");
        liquibase.update("hyphen-context-using-sql,camelCaseContextUsingSql");

        SnapshotGeneratorFactory tableSnapshotGenerator = SnapshotGeneratorFactory.getInstance();
        assertNotNull(tableSnapshotGenerator.has(new Table().setName("hyphen_context"), database));
        assertNotNull(tableSnapshotGenerator.has(new Table().setName("camel_context"), database));
        assertNotNull(tableSnapshotGenerator.has(new Table().setName("bar_id"), database));
        assertNotNull(tableSnapshotGenerator.has(new Table().setName("foo_id"), database));
    }

    @Test
    public void verifyObjectQuotingStrategy() throws Exception {
        if (database == null) {
            return;
        }
        if (!Arrays.asList("oracle,h2,hsqldb,postgresql,mysql").contains(database.getShortName())) {
            return;
        }

        Liquibase liquibase = createLiquibase(objectQuotingStrategyChangeLog);
        clearDatabase(liquibase);
        liquibase.update(contexts);
        clearDatabase(liquibase);
    }

    @Test
    public void testOutputChangeLogIgnoringSchema() throws Exception {
        if (getDatabase() == null) {
            return;
        }

        String schemaName = getDatabase().getDefaultSchemaName();
        if (schemaName == null) {
            return;
        }

        getDatabase().setOutputDefaultSchema(false);
        getDatabase().setOutputDefaultCatalog(false);

        StringWriter output = new StringWriter();
        Liquibase liquibase = createLiquibase(includedChangeLog);
        clearDatabase(liquibase);

        liquibase = createLiquibase(includedChangeLog);
        liquibase.update(contexts, output);

        String outputResult = output.getBuffer().toString();
        assertNotNull(outputResult);
        assertTrue(outputResult.length() > 100); //should be pretty big
//        System.out.println(outputResult);
        CharSequence expected = "CREATE TABLE "+getDatabase().escapeTableName(getDatabase().getLiquibaseCatalogName(), getDatabase().getLiquibaseSchemaName(), getDatabase().getDatabaseChangeLogTableName());
        assertTrue("create databasechangelog command not found in: \n" + outputResult, outputResult.contains(expected));
        assertTrue("create databasechangeloglock command not found in: \n" + outputResult, outputResult.contains(expected));
        assertFalse("the scheame name '"+schemaName+"' should be ignored\n\n"+outputResult, outputResult.contains(schemaName+"."));
//        System.out.println("expected    : " + expected);
//        System.out.println("outputResult: " + outputResult);
    }

    @Test
    public void generateChangeLog_noChanges() throws Exception{
        if (database == null) {
            return;
        }

        runCompleteChangeLog();

        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(database, database, new CompareControl());

        DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult, new DiffOutputControl(false, false, false));
        List<ChangeSet> changeSets = changeLogWriter.generateChangeSets();
        assertEquals(0, changeSets.size());
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
