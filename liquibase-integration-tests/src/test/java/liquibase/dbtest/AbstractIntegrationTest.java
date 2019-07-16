package liquibase.dbtest;

import liquibase.CatalogAndSchema;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.datatype.DataTypeFactory;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.diff.output.report.DiffToReport;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.servicelocator.ServiceLocator;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.core.DropTableStatement;
import liquibase.structure.core.*;
import liquibase.test.*;
import liquibase.util.RegexMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

/**
 * Base class for all database integration tests.  There is an AbstractIntegrationTest subclass for each supported database.
 * The database is assumed to exist at the host returned by getDatabaseServerHostname.  Currently this is hardcoded to an integration test server
 * at liquibase world headquarters.  Feel free to change the return value, but don't check it in.  We are going to improve the config of this at some point.
 */
public abstract class AbstractIntegrationTest {

    @Rule
    public TemporaryFolder tempDirectory = new TemporaryFolder();
    protected String completeChangeLog;
    protected String contexts = "test, context-b";
    protected String username;
    protected String password;
    Set<String> emptySchemas = new TreeSet<>();
    Logger logger;
    private String rollbackChangeLog;
    private String includedChangeLog;
    private String encodingChangeLog;
    private String externalfkInitChangeLog;
    private String externalEntityChangeLog;
    private String externalEntityChangeLog2;
    private String invalidReferenceChangeLog;
    private String objectQuotingStrategyChangeLog;
    private Database database;
    private String jdbcUrl;
    private String defaultSchemaName;

    protected AbstractIntegrationTest(String changelogDir, Database dbms) throws Exception {
        this.completeChangeLog = "changelogs/" + changelogDir + "/complete/root.changelog.xml";
        this.rollbackChangeLog = "changelogs/" + changelogDir + "/rollback/rollbackable.changelog.xml";
        this.includedChangeLog = "changelogs/" + changelogDir + "/complete/included.changelog.xml";
        this.encodingChangeLog = "changelogs/common/encoding.changelog.xml";
        this.externalfkInitChangeLog= "changelogs/common/externalfk.init.changelog.xml";
        this.externalEntityChangeLog= "changelogs/common/externalEntity.changelog.xml";
        this.externalEntityChangeLog2= "com/example/nonIncluded/externalEntity.changelog.xml";
        this.invalidReferenceChangeLog= "changelogs/common/invalid.reference.changelog.xml";
        this.objectQuotingStrategyChangeLog = "changelogs/common/object.quoting.strategy.changelog.xml";
        logger = LogService.getLog(getClass());

        // Get the integration test properties for both global settings and (if applicable) local overrides.
        Properties integrationTestProperties;
        integrationTestProperties = new Properties();
        integrationTestProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.properties"));
        InputStream localProperties=Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.local.properties");
        if(localProperties!=null)
            integrationTestProperties.load(localProperties);

        // Login username
        String username = integrationTestProperties.getProperty("integration.test." + dbms.getShortName() + ".username");
        if(username==null)
            username=integrationTestProperties.getProperty("integration.test.username");
        this.setUsername(username);

        // Login password
        String password = integrationTestProperties.getProperty("integration.test." + dbms.getShortName() + ".password");
        if(password==null)
            password=integrationTestProperties.getProperty("integration.test.password");
        this.setPassword(password);

        // JDBC URL (no global default so all databases!)
        String url = integrationTestProperties.getProperty("integration.test." + dbms.getShortName() + ".url");
        if ((url == null) || ((url.length()) == 0)) {
            throw new LiquibaseException("No JDBC URL found for integration test of database type " + dbms.getShortName());
        }
        this.setJdbcUrl(url);

        ServiceLocator.getInstance().setResourceAccessor(TestContext.getInstance().getTestResourceAccessor());
    }

    public static DatabaseTestURL getDatabaseTestURL(String databaseManager) {
        try {
            Properties integrationTestProperties;
            integrationTestProperties = new Properties();
            integrationTestProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.properties"));
            InputStream localProperties = Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.local.properties");
            if (localProperties != null)
                integrationTestProperties.load(localProperties);

            String url = integrationTestProperties.getProperty("integration.test." + databaseManager + ".jdbcUrl");
            if (url == null)
                return null;

            DatabaseTestURL testUrl = new DatabaseTestURL(databaseManager, url,
                // These may be set to null if not defined as properties.
                integrationTestProperties.getProperty("integration.test." + databaseManager + ".username"),
                integrationTestProperties.getProperty("integration.test." + databaseManager + ".password")
            );
            return testUrl;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void openConnection(String url, String username, String password) throws Exception {
        DatabaseConnection connection = DatabaseTestContext.getInstance().getConnection(url, username, password);

        if (connection != null) {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        }
    }

    /**
     * @return true if this database is provided by the Travis CI build on GitHub.
     *
     * See https://docs.travis-ci.com/user/database-setup/
     */
    protected abstract boolean isDatabaseProvidedByTravisCI();

    /**
     * Reset database connection and internal objects before each test.
     * CAUTION! Does not wipe the schemas - if you want that, you must call {@link #clearDatabase()} .
     */
    @Before
    public void setUp() throws Exception {
        // Try to get a connection to the DBMS (or start the embedded DBMS types)
        openConnection(getJdbcUrl(), getUsername(), getPassword());

        // Do not count the test as successful if we skip it because of a failed login. Count it as skipped instead.
        org.junit.Assume.assumeTrue(database != null);

        if (database != null) {
            if (database.supportsTablespaces()) {
                // Use the opportunity to test if the DATABASECHANGELOG table is placed in the correct tablespace
                database.setLiquibaseTablespaceName(DatabaseTestContext.ALT_TABLESPACE);
            }
            if (!database.getConnection().getAutoCommit()) {
                database.rollback();
            }

            // If we should test with a custom defaultSchemaName:
            if (getDefaultSchemaName() != null && getDefaultSchemaName().length() > 0) {
                database.setDefaultSchemaName(getDefaultSchemaName());
            }

            SnapshotGeneratorFactory.resetAll();
            ExecutorService.getInstance().reset();

            LockServiceFactory.getInstance().resetAll();
            LockServiceFactory.getInstance().getLockService(database).init();

            ChangeLogHistoryServiceFactory.getInstance().resetAll();
        }
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
                    LogService.getLog(getClass()).info(LogType.WRITE_SQL, sql);
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

            if (database.supportsSchemas()) {
                emptyTestSchema(null, DatabaseTestContext.ALT_SCHEMA, database);
            }
            if (supportsAltCatalogTests()) {
                if (database.supportsSchemas() && database.supportsCatalogs()) {
                    emptyTestSchema(DatabaseTestContext.ALT_CATALOG, DatabaseTestContext.ALT_SCHEMA, database);
                }
            }

            /*
             * There is a special treatment for identifiers in the case when (a) the RDBMS does NOT support
             * schemas AND (b) the RDBMS DOES support catalogs AND (c) someone uses "schemaName=..." in a
             * Liquibase ChangeSet. In this case, AbstractJdbcDatabase.escapeObjectName assumes the author
             * was intending to write "catalog=..." and transparently rewrites the expression.
             * For us, this means that we have to wipe both ALT_SCHEMA and ALT_CATALOG to be sure we
             * are doing a thorough cleanup.
             */
            CatalogAndSchema[] alternativeLocations = new CatalogAndSchema[]{
                new CatalogAndSchema(DatabaseTestContext.ALT_CATALOG, null),
                new CatalogAndSchema(null, DatabaseTestContext.ALT_SCHEMA),
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
        }
        SnapshotGeneratorFactory.resetAll();
    }

    protected boolean shouldRollBack() {
        return true;
    }

    protected Liquibase createLiquibase(String changeLogFile) throws Exception {
        CompositeResourceAccessor fileOpener = new CompositeResourceAccessor(new JUnitResourceAccessor(), new FileSystemResourceAccessor());
        return createLiquibase(changeLogFile, fileOpener);
    }

    private Liquibase createLiquibase(String changeLogFile, ResourceAccessor resourceAccessor) {
        ExecutorService.getInstance().clearExecutor(database);
        database.resetInternalState();
        return new Liquibase(changeLogFile, resourceAccessor, database);
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


    @Test
    public void testDatabaseIsReachableIfRequired() {
        if (isDatabaseProvidedByTravisCI()) {
            assertNotNull(
                    "This integration test is expected to pass on Travis CI.\n" +
                            "If you are running on a dev machine and do not have the required\n" +
                            "database installed, you may choose to ignore this failed test.\n" +
                            "To run this test on a dev machine, you will need to install the corresponding\n" +
                            "database and configure liquibase.integrationtest.local.properties",
                    getDatabase());
        } else {
            assumeNotNull(this.getDatabase());
        }
    }

    @Test
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testRunChangeLog() throws Exception {
        assumeNotNull(this.getDatabase());

        runCompleteChangeLog();
    }

    protected void runCompleteChangeLog() throws Exception {
        runChangeLogFile(completeChangeLog);
    }

    protected void runChangeLogFile(String changeLogFile) throws Exception {
        Liquibase liquibase = createLiquibase(changeLogFile);
        clearDatabase();

        //run again to test changelog testing logic
        liquibase = createLiquibase(changeLogFile);
        liquibase.setChangeLogParameter( "loginuser", getUsername());

        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.err);
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
        LogService.getLog(getClass()).info(LogType.WRITE_SQL, sql);

        Connection conn = ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
        boolean savedAcSetting = conn.getAutoCommit();
        conn.setAutoCommit(false);
        conn.createStatement().execute(sql);
        conn.commit();
        conn.setAutoCommit(savedAcSetting);

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", getUsername());
        liquibase.update(this.contexts);

    }

    @Test
    public void testOutputChangeLog() throws Exception {
        assumeNotNull(this.getDatabase());

        StringWriter output = new StringWriter();
        Liquibase liquibase;
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter("loginuser", getUsername());
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
                    LogService.getLog(getClass()).info(LogType.WRITE_SQL, sql);
                    statement.execute(sql);
                    database.commit();
                }
            } catch (Exception e) {
                LogService.getLog(getClass()).warning(LogType.LOG, "Probably expected error dropping databasechangelog table");
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
                    LogService.getLog(getClass()).info(LogType.WRITE_SQL, sql);
                    statement.execute(sql);
                    database.commit();
                }
            } catch (Exception e) {
                LogService.getLog(getClass()).warning(LogType.LOG, "Probably expected error dropping databasechangeloglock table");
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
        liquibase.setChangeLogParameter( "loginuser", getUsername());
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
        liquibase.setChangeLogParameter( "loginuser", getUsername());
        liquibase.update(this.contexts);
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", getUsername());
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
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(completeChangeLog);
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", getUsername());
        liquibase.update(this.contexts);

        liquibase.tag("Test Tag");
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
            assertTrue("comapring a database with itself should return a result of 'DBs are equal'",
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

            DiffOutputControl diffOutputControl = new DiffOutputControl();
            File tempFile = tempDirectory.getRoot().createTempFile("liquibase-test", ".xml");

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
            LogService.getLog(getClass()).info(LogType.LOG, "updating from "+tempFile.getCanonicalPath());
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
        if (!database.supportsSchemas()) {
            return;
        }

        Liquibase liquibase = createLiquibase(includedChangeLog);
        database.setDefaultSchemaName("lbcat2");
        clearDatabase();


        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.forceReleaseLock();

        liquibase.update(includedChangeLog);

        DatabaseSnapshot originalSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));

        CompareControl compareControl = new CompareControl(
                new CompareControl.SchemaComparison[]{
                        new CompareControl.SchemaComparison(
                                CatalogAndSchema.DEFAULT,
                                new CatalogAndSchema(null, "lbcat2")
                        )
                },
                originalSnapshot.getSnapshotControl().getTypesToInclude()
        );
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
        clearDatabase();

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

        DatabaseConnection connection = DatabaseTestContext.getInstance().getConnection(getJdbcUrl(), username, password);
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
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", getUsername());
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
        liquibase.setChangeLogParameter( "loginuser", getUsername());
        clearDatabase();

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", getUsername());
        List<ChangeSet> list = liquibase.listUnrunChangeSets(new Contexts(this.contexts), new LabelExpression());

        assertTrue("querying the changelog table on an empty target should return at least 1 un-run change set", !list.isEmpty());

    }

    @Test
    @SuppressWarnings("squid:S2699") // Successful execution qualifies as test success.
    public void testAbsolutePathChangeLog() throws Exception {
        assumeNotNull(this.getDatabase());


        Set<String> urls = new JUnitResourceAccessor().list(null, includedChangeLog, true, false, true);
        String absolutePathOfChangeLog = urls.iterator().next();

        absolutePathOfChangeLog = absolutePathOfChangeLog.replaceFirst("file:\\/", "");
        if (System.getProperty("os.name").startsWith("Windows ")) {
            absolutePathOfChangeLog = absolutePathOfChangeLog.replace('/', '\\');
        } else {
            absolutePathOfChangeLog = "/" + absolutePathOfChangeLog;
        }
        Liquibase liquibase = createLiquibase(absolutePathOfChangeLog, new FileSystemResourceAccessor());
        clearDatabase();

        liquibase.update(this.contexts);

        liquibase.update(this.contexts); //try again, make sure there are no errors

        clearDatabase();
    }

    private void dropDatabaseChangeLogTable(String catalog, String schema, Database database) {
        try {
            ExecutorService.getInstance().getExecutor(database).execute(
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
        liquibase.setChangeLogParameter( "loginuser", getUsername());
        liquibase.update(this.contexts);

        Path outputDir = tempDirectory.newFolder().toPath().normalize();
        logger.debug(LogType.LOG, "Database documentation will be written to this temporary folder: " + outputDir);

        liquibase = createLiquibase(completeChangeLog);
        liquibase.setChangeLogParameter( "loginuser", getUsername());
        liquibase.generateDocumentation(outputDir.toAbsolutePath().toString(), this.contexts);
    }

    /**
     * Create an SQL script from a change set which inserts data from CSV files. The first CSV file is encoded in
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
        } catch (ChangeLogParseException ignored) {
            //expected
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
        assertNotNull(tableSnapshotGenerator.has(new Table().setName("hyphen_context"), database));
        assertNotNull(tableSnapshotGenerator.has(new Table().setName("camel_context"), database));
        assertNotNull(tableSnapshotGenerator.has(new Table().setName("bar_id"), database));
        assertNotNull(tableSnapshotGenerator.has(new Table().setName("foo_id"), database));
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
                "differential change set.", 0, changeSets.size());
    }

    protected Database getDatabase(){
        return database;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    protected String getJdbcUrl() {
        return jdbcUrl;
    }

    protected void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getDefaultSchemaName() {
        return defaultSchemaName;
    }

    public void setDefaultSchemaName(String defaultSchemaName) {
        this.defaultSchemaName = defaultSchemaName;
    }
}
