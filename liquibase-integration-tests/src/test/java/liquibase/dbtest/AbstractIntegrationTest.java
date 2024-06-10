package liquibase.dbtest;

import groovy.lang.Tuple2;
import liquibase.*;
import liquibase.change.ColumnConfig;
import liquibase.change.core.LoadDataChange;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.command.core.DropAllCommandStep;
import liquibase.command.core.SnapshotCommandStep;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.*;
import liquibase.database.jvm.JdbcConnection;
import liquibase.datatype.DataTypeFactory;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.diff.output.report.DiffToReport;
import liquibase.exception.*;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.TestSystemFactory;
import liquibase.listener.SqlListener;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.logging.core.JavaLogService;
import liquibase.precondition.core.RowCountPrecondition;
import liquibase.precondition.core.TableExistsPrecondition;
import liquibase.precondition.core.TableIsEmptyPrecondition;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.InsertExecutablePreparedStatement;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.core.DropTableStatement;
import liquibase.structure.core.*;
import liquibase.test.DiffResultAssert;
import liquibase.test.JUnitResourceAccessor;
import liquibase.util.ExceptionUtil;
import liquibase.util.RegexMatcher;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static liquibase.test.SnapshotAssert.assertThat;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Base class for all database integration tests.  There is an AbstractIntegrationTest subclass for each supported database.
 * The database is assumed to exist at the host returned by getDatabaseServerHostname.  Currently this is hardcoded to an integration test server
 * at liquibase world headquarters.  Feel free to change the return value, but don't check it in.  We are going to improve the config of this at some point.
 */
public abstract class AbstractIntegrationTest {

    @Rule
    public DatabaseTestSystem testSystem;

    @Rule
    public TemporaryFolder tempDirectory = new TemporaryFolder();
    protected String completeChangeLog;
    protected String contexts = "test, context-b";
    Set<String> emptySchemas = new TreeSet<>();
    Logger logger;
    private final String rollbackChangeLog;

    private final String emptyRollbackSqlChangeLog;
    private final String includedChangeLog;
    private final String encodingChangeLog;
    private final String externalfkInitChangeLog;
    private final String invalidReferenceChangeLog;
    private final String invalidSqlChangeLog;
    private final String objectQuotingStrategyChangeLog;
    private final String commonChangeLog;

    private final String indexWithAssociatedWithChangeLog;
    private Database database;
    private String defaultSchemaName;

    private final String pathChangeLog;

    protected AbstractIntegrationTest(String changelogDir, Database dbms) throws Exception {
        if (dbms != null) {
            this.testSystem = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem(dbms.getShortName());
        }

        this.completeChangeLog = "changelogs/" + changelogDir + "/complete/root.changelog.xml";
        this.rollbackChangeLog = "changelogs/" + changelogDir + "/rollback/rollbackable.changelog.xml";
        this.includedChangeLog = "changelogs/" + changelogDir + "/complete/included.changelog.xml";
        this.encodingChangeLog = "changelogs/common/encoding.changelog.xml";
        this.commonChangeLog = "changelogs/common/common.tests.changelog.xml";
        this.externalfkInitChangeLog= "changelogs/common/externalfk.init.changelog.xml";
        this.invalidReferenceChangeLog= "changelogs/common/invalid.reference.changelog.xml";
        this.invalidSqlChangeLog= "changelogs/common/invalid.sql.changelog.xml";
        this.objectQuotingStrategyChangeLog = "changelogs/common/object.quoting.strategy.changelog.xml";
        this.emptyRollbackSqlChangeLog = "changelogs/common/rollbackable.changelog.sql";
        this.pathChangeLog = "changelogs/common/pathChangeLog.xml";
        this.indexWithAssociatedWithChangeLog = "changelogs/common/index.with.associatedwith.changelog.xml";
        logger = Scope.getCurrentScope().getLog(getClass());

        Scope.setScopeManager(new TestScopeManager(Scope.getCurrentScope()));
    }

    private void openConnection() throws Exception {
        DatabaseConnection connection = new JdbcConnection(testSystem.getConnection());

        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
    }

    /**
     * Reset database connection and internal objects before each test.
     * CAUTION! Does not wipe the schemas - if you want that, you must call {@link #clearDatabase()} .
     */
    @Before
    public void setUp() throws Exception {
        // Try to get a connection to the DBMS (or start the embedded DBMS types)
        openConnection();

        // Do not count the test as successful if we skip it because of a failed login. Count it as skipped instead.
        org.junit.Assume.assumeTrue(database != null);

        if (database.supportsTablespaces()) {
            // Use the opportunity to test if the DATABASECHANGELOG table is placed in the correct tablespace
            String altTablespace = testSystem.getAltTablespace();
            database.setLiquibaseTablespaceName(altTablespace);
        }
        if (!database.getConnection().getAutoCommit()) {
            database.rollback();
        }

        // If we should test with a custom defaultSchemaName:
        if (getDefaultSchemaName() != null && getDefaultSchemaName().length() > 0) {
            database.setDefaultSchemaName(getDefaultSchemaName());
        }

        SnapshotGeneratorFactory.resetAll();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();

        LockServiceFactory.getInstance().resetAll();
        LockServiceFactory.getInstance().getLockService(database).init();

        Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).resetAll();
    }

    /**
     * Wipes all Liquibase schemas in the database before testing starts. This includes the DATABASECHANGELOG/LOCK
     * tables.
     */
    protected void wipeDatabase() {
        emptySchemas.clear();
        try {
            // Try to erase the DATABASECHANGELOGLOCK (not: -LOG!) table that might be a leftover from a previously
            // crashed or interrupted integration test.
            // TODO the cleaner solution would be to have a noCachingHasObject() Method in SnapshotGeneratorFactory
            try {
                if (database.getConnection() != null) {
                    String sql = "DROP TABLE " + database.getDatabaseChangeLogLockTableName();
                    for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
                        listener.writeSqlWillRun(sql);
                    }
                    ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement().executeUpdate(
                            sql
                    );
                    database.commit();
                }
            } catch (SQLException e) {
                if (database instanceof PostgresDatabase) { // throws "current transaction is aborted" unless we roll back the connection
                    database.rollback();
                }
            }

            SnapshotGeneratorFactory.resetAll();
            LockService lockService = LockServiceFactory.getInstance().getLockService(database);
            emptyTestSchema(CatalogAndSchema.DEFAULT.getCatalogName(), CatalogAndSchema.DEFAULT.getSchemaName(),
                    database);
            SnapshotGeneratorFactory factory = SnapshotGeneratorFactory.getInstance();

            String altSchema = testSystem.getAltSchema();
            String altCatalog = testSystem.getAltCatalog();

            if (database.supports(Schema.class)) {
                emptyTestSchema(null, altSchema, database);
            }
            if (supportsAltCatalogTests()) {
                if (database.supports(Schema.class) && database.supports(Catalog.class)) {
                    emptyTestSchema(altCatalog, altSchema, database);
                }
            }

            /*
             * There is a special treatment for identifiers in the case when (a) the RDBMS does NOT support
             * schemas AND (b) the RDBMS DOES support catalogs AND (c) someone uses "schemaName=..." in a
             * Liquibase ChangeSet. In this case, AbstractJdbcDatabase.escapeObjectName assumes the author
             * was intending to write "catalog=..." and transparently rewrites the expression.
             * For us, this means that we have to wipe both altSchema and altCatalog to be sure we
             * are doing a thorough cleanup.
             */
            CatalogAndSchema[] alternativeLocations = new CatalogAndSchema[]{
                new CatalogAndSchema(altCatalog, null),
                new CatalogAndSchema(null, altSchema),
                new CatalogAndSchema("LBCAT2", database.getDefaultSchemaName()),
                new CatalogAndSchema(null, "LBCAT2"),
                new CatalogAndSchema("lbcat2", database.getDefaultSchemaName()),
                new CatalogAndSchema(null, "lbcat2")
            };
            for (CatalogAndSchema location : alternativeLocations) {
                emptyTestSchema(location.getCatalogName(), location.getSchemaName(), database);
            }

            database.commit();
            SnapshotGeneratorFactory.resetAll();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Transforms a given combination of catalogName and schemaName into the standardized format for the given
     * database. If the database has the
     *
     * @param catalogName catalog name (or null)
     * @param schemaName  schema name (or null)
     * @param database    the database where the target might exist
     * @throws LiquibaseException if any problem occurs during the process
     */
    protected void emptyTestSchema(String catalogName, String schemaName, Database database)
            throws LiquibaseException {
        SnapshotGeneratorFactory factory = SnapshotGeneratorFactory.getInstance();

        CatalogAndSchema target = new CatalogAndSchema(catalogName, schemaName).standardize(database);
        Schema schema = new Schema(target.getCatalogName(), target.getSchemaName());
        if (factory.has(schema, database)) {
            if (!emptySchemas.contains(target.toString())) {
                database.dropDatabaseObjects(target);
                emptySchemas.add(target.toString());
            }
        }

    }

    protected boolean supportsAltCatalogTests() {
        return database.supports(Catalog.class);
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
            Scope.getCurrentScope().getSingleton(ExecutorService.class).clearExecutor("jdbc", database);
            database.setDefaultSchemaName(null);
            database.setOutputDefaultCatalog(true);
            database.setOutputDefaultSchema(true);
            try {
                LockService lockService = LockServiceFactory.getInstance().getLockService(database);
                lockService.releaseLock();
            } catch (Exception ignored) {
            }
            try {
                CommandScope commandScope = new CommandScope(DropAllCommandStep.COMMAND_NAME);
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
                commandScope.execute();
            } catch (Exception ignored) {
            }
        }
        SnapshotGeneratorFactory.resetAll();
    }

    protected boolean shouldRollBack() {
        return true;
    }

    protected Liquibase createLiquibase(String changeLogFile) throws Exception {
        ResourceAccessor fileOpener = new JUnitResourceAccessor();
        return createLiquibase(changeLogFile, fileOpener);
    }

    private Liquibase createLiquibase(String changeLogFile, ResourceAccessor resourceAccessor) {
        Scope.getCurrentScope().getSingleton(ExecutorService.class).clearExecutor("jdbc", database);
        database.resetInternalState();
        return new Liquibase(changeLogFile, resourceAccessor, database);
    }

    @Test
    public void testEmptyRollbackableSqlChangeLog() throws Exception {
        assumeNotNull(this.getDatabase());
        createLiquibase(emptyRollbackSqlChangeLog);
        clearDatabase();

        Liquibase liquibase = createLiquibase(emptyRollbackSqlChangeLog);
        liquibase.update(this.contexts);

        liquibase = createLiquibase(emptyRollbackSqlChangeLog);
        liquibase.rollback(new Date(0), this.contexts);
    }

    @Test
    public void testSnapshotReportsAllObjectTypes() throws Exception {
        assumeNotNull(this.getDatabase());
        runCompleteChangeLog();
        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(getDatabase().getDefaultSchema(), getDatabase(), new SnapshotControl(getDatabase()));

        assertThatSnapshotReportsAllObjectTypes(snapshot);
    }

    protected void assertThatSnapshotReportsAllObjectTypes(DatabaseSnapshot snapshot) {
        // TODO add more object types
        assertThat(snapshot).containsObject(UniqueConstraint.class, constraint ->
            "UQ_UQTEST1".equalsIgnoreCase(constraint.getName())
            && "CREATETABLENAMEDUQCONST".equalsIgnoreCase(constraint.getRelation().getName())
            && "ID".equalsIgnoreCase(constraint.getColumns().get(0).getName()));
    }

    @Test
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testBatchInsert() throws Exception {
        if (this.getDatabase() == null) {
            return;
        }
        clearDatabase();

        createLiquibase("changelogs/common/batchInsert.changelog.xml").update(this.contexts);
        // ChangeLog already contains the verification code
    }

    protected Liquibase runCompleteChangeLog() throws Exception {
        return runChangeLogFile(completeChangeLog);
    }

    protected Liquibase runChangeLogFile(String changeLogFile) throws Exception {
        Liquibase liquibase = createLiquibase(changeLogFile);
        clearDatabase();

        //run again to test changelog testing logic
        liquibase = createLiquibase(changeLogFile);
        liquibase.setChangeLogParameter( "loginuser", testSystem.getUsername());

        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.err);
            throw e;
        }
        return liquibase;
    }

    protected CatalogAndSchema[] getSchemasToDrop() throws DatabaseException {
        return new CatalogAndSchema[]{
                new CatalogAndSchema(database.correctObjectName("lbcat2", Catalog.class), database.correctObjectName("lbschem2", Schema.class)),
                new CatalogAndSchema(null, database.getDefaultSchemaName())
        };
    }

    @Test
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testRunUpdateOnOldChangelogTableFormat() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase();

        String nullableKeyword = database.requiresExplicitNullForColumns() ? " NULL" : "";

        String sql = "CREATE TABLE " +
                database.escapeTableName(
                        database.getDefaultCatalogName(), database.getDefaultSchemaName(), "DATABASECHANGELOG"
                ) +
                " (id varchar(150) NOT NULL, " +
                "author VARCHAR(150) NOT NULL, " +
                "filename VARCHAR(255) NOT NULL, " +
                "dateExecuted " +
                DataTypeFactory.getInstance().fromDescription(
                        "datetime", database
                ).toDatabaseDataType(database) + " NOT NULL, " +
                "md5sum VARCHAR(32)" + nullableKeyword + ", " +
                "description VARCHAR(255)" + nullableKeyword + ", " +
                "comments VARCHAR(255)" + nullableKeyword + ", " +
                "tag VARCHAR(255)" + nullableKeyword + ", " +
                "liquibase VARCHAR(10)" + nullableKeyword + ", " +
                "PRIMARY KEY (id, author, filename))";
        for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
            listener.writeSqlWillRun(sql);
        }

        Connection conn = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
        boolean savedAcSetting = conn.getAutoCommit();
        conn.setAutoCommit(false);
        conn.createStatement().execute(sql);
        conn.commit();
        conn.setAutoCommit(savedAcSetting);

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", testSystem.getUsername());
        liquibase.update(this.contexts);

    }

    @Test
    public void testOutputChangeLog() throws Exception {
        assumeNotNull(this.getDatabase());

        StringWriter output = new StringWriter();
        Liquibase liquibase;
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter("loginuser", testSystem.getUsername());
        liquibase.update(this.contexts, output);

        String outputResult = output.getBuffer().toString();
        assertNotNull("generated output change log must not be empty", outputResult);
        assertTrue("generated output change log is at least 100 bytes long", outputResult.length() > 100);

        // TODO should better written to a file so CI servers can pick it up as test artifacts.
        System.out.println(outputResult);
        assertTrue("create databasechangelog command not found in: \n" + outputResult, outputResult.contains("CREATE TABLE "+database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())));
        assertTrue("create databasechangeloglock command not found in: \n" + outputResult, outputResult.contains("CREATE TABLE "+database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())));

        assertTrue("generated output contains a correctly encoded Euro sign", outputResult.contains("€"));

        DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));
        assertEquals("no database objects were actually created during creation of the output changelog",
                0, snapshot.get(Schema.class).iterator().next().getDatabaseObjects(Table.class).size());
    }

    /**
     * Drops all supported object types in all testing schemas and the DATABASECHANGELOG table if it resides in a
     * different schema from the test schemas.
     *
     * @throws DatabaseException if something goes wrong during object deletion
     */
    protected void clearDatabase() throws DatabaseException {
        wipeDatabase();
        try {
            Statement statement = null;
            try {
                // only drop the DATABASECHANGELOG table if it really exists.
                if (SnapshotGeneratorFactory.getInstance().has(
                    new Table()
                        .setName(database.getDatabaseChangeLogTableName())
                        .setSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())),
                        database))
                {
                    statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
                    final String sql = "DROP TABLE " +
                            database.escapeTableName(
                                    database.getLiquibaseCatalogName(),
                                    database.getLiquibaseSchemaName(),
                                    database.getDatabaseChangeLogTableName()
                            );
                    for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
                        listener.writeSqlWillRun(sql);
                    }
                    statement.execute(sql);
                    database.commit();
                }
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Probably expected error dropping databasechangelog table");
                e.printStackTrace();
                database.rollback();
            } finally {
                if (statement != null) {
                    statement.close();
                }
            }

            // Now drop the DATABASECHANGELOGLOCK table (if it exists)
            try {
                if (SnapshotGeneratorFactory.getInstance().has(
                        new Table()
                                .setName(database.getDatabaseChangeLogLockTableName())
                                .setSchema(new Schema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName())),
                        database)) {
                    statement = ((JdbcConnection) database.getConnection()).getUnderlyingConnection().createStatement();
                    String sql = "DROP TABLE " +
                            database.escapeTableName(
                                    database.getLiquibaseCatalogName(),
                                    database.getLiquibaseSchemaName(),
                                    database.getDatabaseChangeLogLockTableName()
                            );
                    for (SqlListener listener : Scope.getCurrentScope().getListeners(SqlListener.class)) {
                        listener.writeSqlWillRun(sql);
                    }
                    statement.execute(sql);
                    database.commit();
                }
            } catch (Exception e) {
                Scope.getCurrentScope().getLog(getClass()).warning("Probably expected error dropping databasechangeloglock table");
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
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testUpdateTwice() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", testSystem.getUsername());
        liquibase.update(this.contexts);
        liquibase.update(this.contexts);
    }

    @Test
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testUpdateClearUpdate() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", testSystem.getUsername());
        liquibase.update(this.contexts);
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", testSystem.getUsername());
        liquibase.update(this.contexts);
    }

    @Test
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testRollbackableChangeLog() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(rollbackChangeLog);
        clearDatabase();

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
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testRollbackableChangeLogScriptOnExistingDatabase() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(rollbackChangeLog);
        clearDatabase();

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.update(this.contexts);

        StringWriter writer = new StringWriter();

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.rollback(new Date(0), this.contexts, writer);
    }

    @Test
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testRollbackableChangeLogScriptOnFutureDatabase() throws Exception {
        assumeNotNull(this.getDatabase());

        StringWriter writer = new StringWriter();

        Liquibase liquibase = createLiquibase(rollbackChangeLog);
        clearDatabase();

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.futureRollbackSQL(new Contexts(this.contexts), new LabelExpression(), writer);
    }

    @Test
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testTag() throws Exception {
        //This test will validate a tag can be set successfully to the DB, plus make sure the given tag exists in the DB.
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", testSystem.getUsername());
        liquibase.update(this.contexts);

        liquibase.tag("Test Tag");
        assertTrue(liquibase.tagExists("Test Tag"));
    }

    @Test
    public void testDiff() throws Exception {
        assumeNotNull(this.getDatabase());

        runCompleteChangeLog();

        CompareControl compareControl = new CompareControl();
        compareControl.addSuppressedField(Column.class, "defaultValue");  //database returns different data even if the same
        compareControl.addSuppressedField(Column.class, "autoIncrementInformation"); //database returns different data even if the same
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(database, database, compareControl);

        try {
            assertTrue("comparing a database with itself should return a result of 'DBs are equal'",
                    diffResult.areEqual());
        } catch (AssertionError e) {
            new DiffToReport(diffResult, System.err).print();
            throw e;
        }
    }

    @Test
    public void testRerunDiffChangeLog() throws Exception {
        assumeNotNull(this.getDatabase());

        for (int run=0; run < 2; run++) { //run once outputting data as insert, once as csv
            boolean outputCsv = run == 1;
            runCompleteChangeLog();

            SnapshotControl snapshotControl = new SnapshotControl(database);

            DatabaseSnapshot originalSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, snapshotControl);

            CompareControl compareControl = new CompareControl();
            compareControl.addSuppressedField(Column.class, "defaultValue");  //database returns different data even if the same
            compareControl.addSuppressedField(Column.class, "autoIncrementInformation"); //database returns different data even if the same
            if (database instanceof OracleDatabase) {
                compareControl.addSuppressedField(Column.class, "type"); //database returns different nvarchar2 info even though they are the same
                compareControl.addSuppressedField(Column.class, "nullable"); // database returns different nullable on views, e.g. v_person.id
            }
            if (database instanceof PostgresDatabase) {
                compareControl.addSuppressedField(Column.class, "type"); //database returns different nvarchar2 info even though they are the same
            }
            if (database instanceof H2Database) {
                //original changeset 2659-Create-MyView-with-quotes in the h2 changelog uses QUOTE_ALL_OBJECTS, but generated changesets do not use that attribute so the name comes through as different
                compareControl.addSuppressedField(View.class, "name");
            }

            DiffOutputControl diffOutputControl = new DiffOutputControl();
            File tempFile = tempDirectory.getRoot().createTempFile("liquibase-test", ".xml");

            if (outputCsv) {
                diffOutputControl.setDataDir(new File(tempFile.getParentFile(), "liquibase-data").getCanonicalPath().replaceFirst("\\w:",""));
            }

            DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(database, null, compareControl);


            OutputStream output = Files.newOutputStream(tempFile.toPath());
            try {
                new DiffToChangeLog(diffResult, new DiffOutputControl()).print(new PrintStream(output));
                output.flush();
            } finally {
                output.close();
            }

            Liquibase liquibase = createLiquibase(tempFile.getName());
            clearDatabase();

            DatabaseSnapshot emptySnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));

            //run again to test changelog testing logic
            liquibase = createLiquibase(tempFile.getName());
            try {
                liquibase.update(this.contexts);
            } catch (ValidationFailedException e) {
                e.printDescriptiveError(System.out);
                throw e;
            }

            DatabaseSnapshot migratedSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));

            DiffResult finalDiffResult = DiffGeneratorFactory.getInstance().compare(originalSnapshot, migratedSnapshot, compareControl);
            try {
                assertTrue("recreating the database from the generated change log should cause both 'before' and " +
                                "'after' snapshots to be equal.",
                        finalDiffResult.areEqual());
            } catch (AssertionError e) {
                new DiffToReport(finalDiffResult, System.err).print();
                throw e;
            }

            //diff to empty and drop all
            DiffResult emptyDiffResult = DiffGeneratorFactory.getInstance().compare(emptySnapshot, migratedSnapshot, compareControl);
            output = new FileOutputStream(tempFile);
            try {
                new DiffToChangeLog(emptyDiffResult, new DiffOutputControl(true, true, true, null)).print(new PrintStream(output));
                output.flush();
            } finally {
                output.close();
            }

            liquibase = createLiquibase(tempFile.getName());
            Scope.getCurrentScope().getLog(getClass()).info("updating from "+tempFile.getCanonicalPath());
            try {
                liquibase.update(this.contexts);
            } catch (LiquibaseException e) {
                throw e;
            }

            DatabaseSnapshot emptyAgainSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));
            assertEquals("a database that was 'updated' to an empty snapshot should only have 2 tables left: " +
                            "the database change log table and the lock table.",
                    2, emptyAgainSnapshot.get(Table.class).size());
            assertEquals("a database that was 'updated' to an empty snapshot should not contain any views.",
                    0, emptyAgainSnapshot.get(View.class).size());
        }
    }

    @Test
    public void testRerunDiffChangeLogAltSchema() throws Exception {
        assumeNotNull(this.getDatabase());
        if (database.getShortName().equalsIgnoreCase("mssql")) {
            return; // not possible on MSSQL.
        }
        if (!database.supports(Schema.class)) {
            return;
        }

        Liquibase liquibase = createLiquibase(includedChangeLog);
        database.setDefaultSchemaName("lbschem2");
        clearDatabase();


        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.forceReleaseLock();

        liquibase.update(includedChangeLog);

        DatabaseSnapshot originalSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));

        CompareControl compareControl = new CompareControl(
                new CompareControl.SchemaComparison[]{
                        new CompareControl.SchemaComparison(
                                CatalogAndSchema.DEFAULT,
                                new CatalogAndSchema("lbcat2", null)
                        )
                },
                originalSnapshot.getSnapshotControl().getTypesToInclude()
        );
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(database, null, compareControl);

        File tempFile = File.createTempFile("liquibase-test", ".xml");
        tempFile.deleteOnExit();

        try (FileOutputStream output = new FileOutputStream(tempFile)) {
            new DiffToChangeLog(diffResult, new DiffOutputControl()).print(new PrintStream(output));
            output.flush();
        }

        liquibase = createLiquibase(tempFile.getName());
        clearDatabase();

        //run again to test changelog testing logic
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        try {
            executor.execute(new DropTableStatement("lbcat2", null, database.getDatabaseChangeLogTableName(), false));
        } catch (DatabaseException e) {
            //ok
        }
        try {
            executor.execute(new DropTableStatement("lbcat2", null, database.getDatabaseChangeLogLockTableName(), false));
        } catch (DatabaseException e) {
            //ok
        }
        database.commit();

        DatabaseConnection connection = new JdbcConnection(testSystem.getConnection());
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        database.setDefaultSchemaName("lbschem2");
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
        assertTrue("running the same change log two times against an alternative schema should produce " +
                        "equal snapshots.",
                finalDiffResult.areEqual());
    }

    @Test
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testClearChecksums() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", testSystem.getUsername());
        liquibase.update(this.contexts);

        liquibase.clearCheckSums();
    }

    @Test
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testTagEmptyDatabase() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.checkLiquibaseTables(false, null, new Contexts(), new LabelExpression());
        liquibase.tag("empty");

        liquibase = createLiquibase(rollbackChangeLog);
        liquibase.update(new Contexts());

        liquibase.rollback("empty", new Contexts());

    }

    @Test
    public void testUnrunChangeSetsEmptyDatabase() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", testSystem.getUsername());
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", testSystem.getUsername());
        List<ChangeSet> list = liquibase.listUnrunChangeSets(new Contexts(this.contexts), new LabelExpression());

        assertFalse("querying the changelog table on an empty target should return at least 1 un-run changeset", list.isEmpty());

    }

    private void dropDatabaseChangeLogTable(String catalog, String schema, Database database) {
        try {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).execute(
                new DropTableStatement(catalog, schema, database.getDatabaseChangeLogTableName(), false)
            );
        } catch (DatabaseException e) {
            //ok
        }
    }

    @Test
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testRollbackToChange() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(rollbackChangeLog);
        wipeDatabase();

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
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testDbDoc() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase;
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", testSystem.getUsername());
        liquibase.update(this.contexts);

        Path outputDir = tempDirectory.newFolder().toPath().normalize();
        logger.fine("Database documentation will be written to this temporary folder: " + outputDir);

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", testSystem.getUsername());
        liquibase.generateDocumentation(outputDir.toAbsolutePath().toString(), this.contexts);
    }

    /**
     * Create an SQL script from a changeset which inserts data from CSV files. The first CSV file is encoded in
     * UTF-8, the second is encoded in Latin-1. The test is successful if the CSV data is converted into correct
     * INSERT INTO statements in the final generated SQL file.
     * @throws Exception
     */
    @Test
    public void testEncodingUpdating2SQL() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(encodingChangeLog);

        StringWriter writer=new StringWriter();
        liquibase.update(this.contexts,writer);
        assertTrue("Update to SQL preserves encoding",
            new RegexMatcher(writer.toString(), new String[] {
                //For the UTF-8 encoded cvs
                "^.*INSERT.*VALUES.*àèìòùáéíóúÀÈÌÒÙÁÉÍÓÚâêîôûäëïöü.*?\\)",
                "çñ®",
                //For the latin1 one
                "^.*INSERT.*VALUES.*àèìòùáéíóúÀÈÌÒÙÁÉÍÓÚâêîôûäëïöü.*?\\)",
                "çñ®"
            }).allMatchedInSequentialOrder());
    }

    /**
     * Test that diff is capable to detect foreign keys to external schemas that doesn't appear in the changelog
     */
   @Test
   @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
   public void testDiffExternalForeignKeys() throws Exception {
       assumeNotNull(this.getDatabase());
       clearDatabase();
       Liquibase liquibase = createLiquibase(externalfkInitChangeLog);
       liquibase.update(contexts);

       DiffResult diffResult = liquibase.diff(database, null, new CompareControl());
       DiffResultAssert.assertThat(diffResult).containsMissingForeignKeyWithName("fk_person_country");
   }

    @Test
    public void testInvalidIncludeDoesntBreakLiquibase() throws Exception {
        assumeNotNull(this.getDatabase());
        Liquibase liquibase = createLiquibase(invalidReferenceChangeLog);
        try {
            liquibase.update(new Contexts());
            fail("Did not fail with invalid include");
        } catch (CommandExecutionException executionException) {
            //expected
        }

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        assertFalse(lockService.hasChangeLogLock());
    }

    @Test
    public void testInvalidSqlThrowsException() throws Exception {
        assumeNotNull(this.getDatabase());
        Liquibase liquibase = createLiquibase(invalidSqlChangeLog);
        Throwable exception = null;
        try {
            liquibase.update(new Contexts());
            fail("Did not fail with invalid SQL");
        } catch (CommandExecutionException executionException) {
            exception = ExceptionUtil.findExceptionInCauseChain(executionException.getCause(), DatabaseException.class);
            Assert.assertTrue(exception.getCause() instanceof DatabaseException);
        }

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        assertFalse(lockService.hasChangeLogLock());
    }

    @Test
    public void testContextsWithHyphensWorkInFormattedSql() throws Exception {
        assumeNotNull(this.getDatabase());
        Liquibase liquibase = createLiquibase("changelogs/common/sqlstyle/formatted.changelog.sql");
        liquibase.update("hyphen-context-using-sql,camelCaseContextUsingSql");

        SnapshotGeneratorFactory tableSnapshotGenerator = SnapshotGeneratorFactory.getInstance();
        assertTrue(tableSnapshotGenerator.has(new Table().setName("hyphen_context"), database));
        assertTrue(tableSnapshotGenerator.has(new Table().setName("camel_context"), database));
        assertTrue(tableSnapshotGenerator.has(new Table().setName("bar_id"), database));
        assertTrue(tableSnapshotGenerator.has(new Table().setName("foo_id"), database));
    }

    @Test
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testObjectQuotingStrategy() throws Exception {
        assumeNotNull(this.getDatabase());
        if (!Arrays.asList("oracle,h2,hsqldb,postgresql,mysql").contains(database.getShortName())) {
            return;
        }

        Liquibase liquibase = createLiquibase(objectQuotingStrategyChangeLog);
        clearDatabase();
        liquibase.update(contexts);
        clearDatabase();
    }

    @Test
    public void testOutputChangeLogIgnoringSchema() throws Exception {
        assumeNotNull(this.getDatabase());

        String schemaName = getDatabase().getDefaultSchemaName();
        if (schemaName == null) {
            return;
        }

        getDatabase().setOutputDefaultSchema(false);
        getDatabase().setOutputDefaultCatalog(false);

        StringWriter output = new StringWriter();
        Liquibase liquibase = createLiquibase(includedChangeLog);
        clearDatabase();

        liquibase = createLiquibase(includedChangeLog);
        liquibase.update(contexts, output);

        String outputResult = output.getBuffer().toString();
        assertNotNull("generated SQL may not be empty", outputResult);
        assertTrue("Expect at least 100 bytes of output in generated SQL", outputResult.length() > 100);
        CharSequence expected = "CREATE TABLE "+getDatabase().escapeTableName(getDatabase().getLiquibaseCatalogName(), getDatabase().getLiquibaseSchemaName(), getDatabase().getDatabaseChangeLogTableName());
        assertTrue("create databasechangelog command not found in: \n" + outputResult, outputResult.contains(expected));
        assertTrue("create databasechangeloglock command not found in: \n" + outputResult, outputResult.contains(expected));
        assertFalse("the schema name '" + schemaName + "' should be ignored\n\n" + outputResult, outputResult.contains
                (schemaName+"."));
    }

    @Test
    public void testGenerateChangeLogWithNoChanges() throws Exception {
        assumeNotNull(this.getDatabase());

        runCompleteChangeLog();

        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(database, database, new CompareControl());

        DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult, new DiffOutputControl(false, false, false, null));
        List<ChangeSet> changeSets = changeLogWriter.generateChangeSets();
        assertEquals("generating two change logs without any changes in between should result in an empty generated " +
                "differential changeset.", 0, changeSets.size());
    }

    @Test
    public void testInsertLongClob() {
        assumeNotNull(this.getDatabase());

        DatabaseChangeLog longClobChangelog = new DatabaseChangeLog();
        ChangeSet longClobInsert = new ChangeSet(longClobChangelog);
        ColumnConfig clobColumn = new ColumnConfig();
        clobColumn.setName("clobColumn");
        clobColumn.setType(LoadDataChange.LOAD_DATA_TYPE.CLOB.name());
        // Oracle database only allows string values of up to 4000 characters
        // so we test that the CLOB insertion is actually done as a CLOB in the JDBC statement
        StringBuilder longClobString = new StringBuilder(4001);
        for (int i=0;i<4001;i++) {
            longClobString.append('a');
        }
        clobColumn.setValue(longClobString.toString());

        CreateTableStatement clobTableCreator = new CreateTableStatement(
                database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), "tableWithClob");
        clobTableCreator.addColumn("clobColumn", DataTypeFactory.getInstance().fromDescription("clob", database));
        InsertExecutablePreparedStatement insertStatement = new InsertExecutablePreparedStatement(
                database, database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(),
                "tableWithClob", Arrays.asList(clobColumn),
                longClobInsert, Scope.getCurrentScope().getResourceAccessor());
        try {
            database.execute(new SqlStatement[] {clobTableCreator, insertStatement}, new ArrayList<>());
        } catch(LiquibaseException ex) {
            ex.printStackTrace();
            fail("Long clob insertion failed!");
        }
    }

    @Test
    public void testTableExistsPreconditionTableNameMatch() throws Exception {
        assumeNotNull(this.getDatabase());
        runChangeLogFile(commonChangeLog);

        TableExistsPrecondition precondition = new TableExistsPrecondition();
        precondition.setTableName("standardTypesTest");
        precondition.check(this.getDatabase(), null, null, null);
    }

    @Test
    public void testTableIsEmptyPrecondition() throws Exception {
        assumeNotNull(this.getDatabase());
        runChangeLogFile(commonChangeLog);

        checkTableIsEmptyPrecondition("empty_table", 0L);
        checkTableIsEmptyPrecondition("single_row_table", 1L);
        checkTableIsEmptyPrecondition("multi_row_table", 2L);
    }

    private void checkTableIsEmptyPrecondition(String tableName, long actualRows) throws PreconditionFailedException, PreconditionErrorException {
        TableIsEmptyPrecondition precondition = new TableIsEmptyPrecondition();
        precondition.setTableName(tableName);
        if (actualRows == 0L) {
            precondition.check(this.getDatabase(), null, null, null);
        } else {
            PreconditionFailedException ex = assertThrows(PreconditionFailedException.class, () -> precondition.check(this.getDatabase(), null, null, null));
            assertEquals(1, ex.getFailedPreconditions().size());

            String expectedMessage = String.format("Table %s is not empty.", tableName);
            assertEquals(expectedMessage, ex.getFailedPreconditions().get(0).getMessage());
        }
    }

    @Test
    public void testRowCountPrecondition() throws Exception {
        assumeNotNull(this.getDatabase());
        runChangeLogFile(commonChangeLog);

        checkRowCountPrecondition("empty_table", 0L, 0L);
        checkRowCountPrecondition("empty_table", 1L, 0L);
        checkRowCountPrecondition("empty_table", Long.MAX_VALUE, 0L);
        checkRowCountPrecondition("single_row_table", 0L, 1L);
        checkRowCountPrecondition("single_row_table", 1L, 1L);
        checkRowCountPrecondition("single_row_table", Long.MAX_VALUE, 1L);
        checkRowCountPrecondition("multi_row_table", 0L, 2L);
        checkRowCountPrecondition("multi_row_table", 2L, 2L);
        checkRowCountPrecondition("multi_row_table", Long.MAX_VALUE, 2L);
    }

    private void checkRowCountPrecondition(String tableName, long expectedRows, long actualRows) throws PreconditionFailedException, PreconditionErrorException {
        RowCountPrecondition precondition = new RowCountPrecondition();
        precondition.setTableName(tableName);
        precondition.setExpectedRows(expectedRows);
        if (expectedRows == actualRows) {
            precondition.check(this.getDatabase(), null, null, null);
        } else {
            PreconditionFailedException ex = assertThrows(PreconditionFailedException.class, () -> precondition.check(this.getDatabase(), null, null, null));
            assertEquals(1, ex.getFailedPreconditions().size());

            String expectedMessage = String.format("Table %s does not have the expected row count of %s. It contains %s rows", tableName, expectedRows, actualRows);
            assertEquals(expectedMessage, ex.getFailedPreconditions().get(0).getMessage());
        }
    }

    protected Database getDatabase(){
        return database;
    }

    public String getDefaultSchemaName() {
        return defaultSchemaName;
    }

    public void setDefaultSchemaName(String defaultSchemaName) {
        this.defaultSchemaName = defaultSchemaName;
    }

    @Test
    public void testUpdateChangelogChecksum() throws Exception {
       //NOTE: This test does as much to test the handing of the StandardHistoryService.ranChangeSetList cache when the underlying checksums need to be updated as anything
       //NOTE: The end-user behaviour it's replicating is: `liquibase update` with a version that uses an old checksum version then `liquibase update` with the current version
        assumeNotNull(this.getDatabase());

        String schemaName = getDatabase().getDefaultSchemaName();
        if (schemaName == null) {
            return;
        }

        getDatabase().setOutputDefaultSchema(false);
        getDatabase().setOutputDefaultCatalog(false);

        Liquibase liquibase = new Liquibase(includedChangeLog, new JUnitResourceAccessor(), database.getConnection());
        liquibase.setChangeLogParameter("loginuser", testSystem.getUsername());
        liquibase.update(contexts);

        Connection conn = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
        conn.createStatement().execute("update DATABASECHANGELOG set md5sum = null");
        if (!conn.getAutoCommit()) {
            conn.commit();
        }

        liquibase.getDatabase().getRanChangeSetList();
        liquibase.update(contexts);
    }

    @Ignore //this test is still randomly failing, and the underlying problem needs to be figured out
    @Test
    public void testThatMultipleJVMsCanApplyChangelog() throws Exception {
        clearDatabase();

        List<ProcessBuilder> processBuilders = Arrays.asList(
            prepareExternalLiquibaseProcess(),
            prepareExternalLiquibaseProcess(),
            prepareExternalLiquibaseProcess()
        );

        List<Process> processes = new ArrayList<>();
        for (ProcessBuilder builder : processBuilders) {
            Process process = builder.redirectErrorStream(true).start();
            processes.add(process);
        }

        List<Tuple2<Integer, String>> outputs = new ArrayList<>();
        for (Process process : processes) {
            boolean exitedWithinTimeout = !process.waitFor(2, TimeUnit.MINUTES);

            String output;
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                output = input.lines().limit(100).collect(Collectors.joining(System.lineSeparator()));
            }

            if (!exitedWithinTimeout) {
                process.destroy();
            }

            outputs.add(new Tuple2<>(process.exitValue(), output));
        }

        for (Tuple2<Integer, String> output : outputs) {
            if (output.getFirst() == 0) {
                continue;
            }

            fail("Migration JVM failed with exit code " + output.getFirst() + ": " + output.getSecond());
        }
    }

    @Test
    public void testPathFromChangeObjectIsDeployed() throws Exception {
        assumeNotNull(this.getDatabase());
        Liquibase liquibase;
        clearDatabase();

        String pathToSet = "changelogs/common/commentTest.sql";

        liquibase = createLiquibase(pathChangeLog);
        liquibase.update(this.contexts);

        assertTrue(liquibase.getDatabaseChangeLog().getChangeSets().stream().allMatch(changeSet -> changeSet.getDescription().contains(pathToSet)));
    }

    @Test
    public void allowsDbChangelogTableNameAsLowerCase() throws DatabaseException {
        clearDatabase();
        String oldDbChangelogTableName = this.getDatabase().getDatabaseChangeLogTableName();
        try {
            getDatabase().setDatabaseChangeLogTableName("lowercase");
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME);
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase());
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, objectQuotingStrategyChangeLog);
            commandScope.execute();
        } catch (Exception e) {
            Assert.fail("Should not fail. Reason: " + e.getMessage());
        } finally {
            getDatabase().setDatabaseChangeLogTableName(oldDbChangelogTableName);
        }
    }

    @Test
    public void verifyIndexIsCreatedWhenAssociatedWithPropertyIsSetAsNone() throws DatabaseException {
       clearDatabase();
        try {
            Database database = getDatabase();
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME);
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, indexWithAssociatedWithChangeLog);
            commandScope.execute();

            final CommandScope snapshotScope = new CommandScope("snapshot");
            snapshotScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
            snapshotScope.addArgumentValue(SnapshotCommandStep.SNAPSHOT_FORMAT_ARG, "json");
            CommandResults results = snapshotScope.execute();
            DatabaseSnapshot snapshot = (DatabaseSnapshot) results.getResult("snapshot");
            Index index = snapshot.get(new Index("idx_test"));
            Assert.assertNotNull(index );
        } catch (Exception e) {
            Assert.fail("Should not fail. Reason: " + e.getMessage());
        } finally {
            clearDatabase();
        }

    }

    @Test
    public void makeSureNoErrorIsReturnedWhenTableNameIsNotSpecified() throws DatabaseException {
        // This is a test to validate most of the DBs do not throw an error when tableName is not
        // specified as part of primaryKeyExistsPrecondition.
        clearDatabase();
        String errorMsg = "";
        try {
            runUpdate("changelogs/common/preconditions/preconditions.changelog.xml");
        }catch(CommandExecutionException e) {
            errorMsg = e.getMessage();
        }
        finally {
            clearDatabase();
        }

        if(!(database instanceof H2Database || database instanceof MySQLDatabase || database instanceof HsqlDatabase
                || database instanceof SQLiteDatabase || database instanceof DB2Database)) {
            Assert.assertTrue(errorMsg.isEmpty());
        }
    }

    @Test
    public void makeSureErrorIsReturnedWhenTableNameIsNotSpecified() throws DatabaseException {
        // This is a test to validate some DBs do require a tableName as part of primaryKeyExistsPrecondition,
        // if it's not specified an error will be thrown.
        clearDatabase();
        String errorMsg = "";
        try {
            runUpdate("changelogs/common/preconditions/preconditions.changelog.xml");
        }catch(CommandExecutionException e) {
            errorMsg = e.getMessage();
        }
        finally {
            clearDatabase();
        }

        if(database instanceof H2Database || database instanceof MySQLDatabase || database instanceof HsqlDatabase
                || database instanceof SQLiteDatabase || database instanceof DB2Database) {
            Assert.assertTrue(errorMsg.contains("Database driver requires a table name to be specified in order to search for a primary key."));
        }
    }

    private ProcessBuilder prepareExternalLiquibaseProcess() {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");

        List<String> command = new LinkedList<>();
        command.add(javaBin);
        command.add("-cp");
        command.add(classpath);
        command.add(ApplyTestChangelog.class.getName());

        command.add(includedChangeLog);
        command.add(testSystem.getConnectionUrl());
        command.add(testSystem.getUsername());
        command.add(testSystem.getPassword());
        command.add(contexts);

        return new ProcessBuilder(command);
    }

    protected void runUpdate(String changelog) throws CommandExecutionException {
        CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME);
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, testSystem.getConnectionUrl());
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, testSystem.getUsername());
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, testSystem.getPassword());
        commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelog);
        commandScope.execute();
    }

    public static final class ApplyTestChangelog {

        private static void initLogLevel() {
            java.util.logging.Logger liquibaseLogger = java.util.logging.Logger.getLogger("liquibase");

            final JavaLogService logService = (JavaLogService) Scope.getCurrentScope().get(Scope.Attr.logService, LogService.class);
            logService.setParent(liquibaseLogger);
            java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");

            rootLogger.setLevel(java.util.logging.Level.INFO);
            liquibaseLogger.setLevel(java.util.logging.Level.INFO);

            for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
                handler.setLevel(java.util.logging.Level.INFO);
            }
        }

        public static void main(String[] args) throws Exception {
            String changeLogFile = Objects.requireNonNull(args[0], "Changelog is required");
            String url = Objects.requireNonNull(args[1], "JDBC url is required");
            String username = Objects.requireNonNull(args[2], "JDBC username is required");
            String password = Objects.requireNonNull(args[3], "JDBC password is required");
            String contexts = Objects.requireNonNull(args[4], "Liquibase contexts is required");

            initLogLevel();

            DatabaseConnection connection = new JdbcConnection(DriverManager.getConnection(url, username, password));
            ResourceAccessor fileOpener = new JUnitResourceAccessor();

            Liquibase liquibase = new Liquibase(changeLogFile, fileOpener, DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection));
            liquibase.setChangeLogParameter("loginuser", username);
            liquibase.update(contexts);
        }
    }
}
