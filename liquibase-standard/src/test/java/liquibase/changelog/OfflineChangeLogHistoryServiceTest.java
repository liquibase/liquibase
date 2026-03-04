package liquibase.changelog;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.OfflineConnection;
import liquibase.database.core.HsqlDatabase;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @see https://liquibase.jira.com/browse/CORE-2334
 */
public class OfflineChangeLogHistoryServiceTest {
    private static final String CHANGE_LOG_CSV = "changeLog.csv";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).resetAll();
    }

    /**
     * Test ChangeLog table update SQL generation with outputLiquibaseSql=true and outputLiquibaseSql=true
     */
    @Test
    public void testInitOfflineWithOutputLiquibaseSql() throws Exception {
        // Given
        StringWriter writer = new StringWriter();
        OfflineChangeLogHistoryService service = createService(writer, "true");
        ChangeSet changeSet = createChangeSet();
        // When
        service.init();
        service.setExecType(changeSet, ChangeSet.ExecType.EXECUTED);
        writer.close();
        unregisterService(service);

        // Assert
        assertTrue(writer.toString().contains("CREATE TABLE PUBLIC.DATABASECHANGELOG"));
        assertTrue(writer.toString().contains("INSERT INTO PUBLIC.DATABASECHANGELOG"));
    }

    /**
     * Test if the changelogCsv gets updated properly and the .new file gets deleted
     */
    @Test
    public void testNewCsvFileDeletion() throws Exception {
        // Given
        StringWriter writer = new StringWriter();
        OfflineChangeLogHistoryService service = createService(writer, "true");
        ChangeSet changeSet = createChangeSet();

        // When
        service.init();
        service.setExecType(changeSet, ChangeSet.ExecType.EXECUTED);
        writer.close();
        unregisterService(service);

        // Assert
        assertTrue(new File(temporaryFolder.getRoot(), CHANGE_LOG_CSV).exists());
        assertFalse(new File(temporaryFolder.getRoot(), CHANGE_LOG_CSV + ".new").exists());
    }

    /**
     * Test ChangeLog table update SQL generation with outputLiquibaseSql=true and outputLiquibaseSql=data_only
     */
    @Test
    public void testInitOfflineWithOutputLiquibaseSqlAndNoDdl() throws Exception {
        // Given
        StringWriter writer = new StringWriter();
        OfflineChangeLogHistoryService service = createService(writer, "data_only");
        ChangeSet changeSet = createChangeSet();
        // When
        service.init();
        service.setExecType(changeSet, ChangeSet.ExecType.EXECUTED);
        writer.close();
        unregisterService(service);
        // Assert
        assertFalse(writer.toString().contains("CREATE TABLE PUBLIC.DATABASECHANGELOG"));
        assertTrue(writer.toString().contains("INSERT INTO PUBLIC.DATABASECHANGELOG"));
    }

    /**
     *
     * Create OfflineChangeLogHistoryService and register LoggingExecutor
     *
     */
    private OfflineChangeLogHistoryService createService(Writer writer, String outputLiquibaseSql) {
        HsqlDatabase database = new HsqlDatabase();
        File changeLogCsvFile = new File(temporaryFolder.getRoot(), CHANGE_LOG_CSV);
        OfflineConnection connection = new OfflineConnection("offline:hsqldb?changeLogFile="+changeLogCsvFile.getAbsolutePath() + "&outputLiquibaseSql=" + outputLiquibaseSql, new ClassLoaderResourceAccessor());
        database.setConnection(connection);
        OfflineChangeLogHistoryService changeLogHistoryService = (OfflineChangeLogHistoryService) Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);

        //
        // Create the new LoggingExecutor and give it the original Executor as a delegator
        // We also set the LoggingExecutor as the JDBC Executor
        //
        LoggingExecutor loggingExecutor = new LoggingExecutor(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database), writer, database);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("logging", database, loggingExecutor);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, loggingExecutor);
        return changeLogHistoryService;
    }

    private void unregisterService(OfflineChangeLogHistoryService service) {
        Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).unregister(service);
    }

    /**
     * Test that clearAllCheckSums clears the FastCheck cache
     */
    @Test
    public void testClearAllCheckSumsClearsFastCheckCache() throws Exception {
        // Given
        StringWriter writer = new StringWriter();
        OfflineChangeLogHistoryService service = createService(writer, "true");
        ChangeSet changeSet = createChangeSet();
        FastCheckService fastCheckService = Scope.getCurrentScope().getSingleton(FastCheckService.class);
        Database database = service.getDatabase();

        // Initialize service and mark changeset as executed
        service.init();
        service.setExecType(changeSet, ChangeSet.ExecType.EXECUTED);

        DatabaseChangeLog databaseChangeLog = new DatabaseChangeLog("/patch/changeLog.xml");
        databaseChangeLog.addChangeSet(changeSet);

        // Populate the FastCheck cache by calling isUpToDateFastCheck
        fastCheckService.isUpToDateFastCheck(null, database, databaseChangeLog, new Contexts(), new LabelExpression());

        // Verify the cache was populated by using reflection to check the cache map size
        int cacheSizeBefore = getFastCheckCacheSize(fastCheckService);
        assertTrue("FastCheck cache should be populated after first check", cacheSizeBefore > 0);

        // When - Clear all checksums (this should clear the FastCheck cache)
        service.clearAllCheckSums();

        // Then - Verify the cache was actually cleared
        int cacheSizeAfter = getFastCheckCacheSize(fastCheckService);
        assertEquals("FastCheck cache should be empty after clearAllCheckSums", 0, cacheSizeAfter);

        writer.close();
        unregisterService(service);

        // Clean up
        fastCheckService.clearCache();
    }

    /**
     * Helper method to get the size of the FastCheck cache using reflection
     */
    private int getFastCheckCacheSize(FastCheckService fastCheckService) throws Exception {
        Field cacheField = FastCheckService.class.getDeclaredField("upToDateFastCheck");
        cacheField.setAccessible(true);
        Map<?, ?> cache = (Map<?, ?>) cacheField.get(fastCheckService);
        return cache.size();
    }

    /**
     * Create ChangeLog and ChangeSet
     */
    private ChangeSet createChangeSet() {
        DatabaseChangeLog databaseChangeLog = new DatabaseChangeLog("/patch/changeLog.xml");
        ChangeSet changeSet = new ChangeSet("id", "author", false, false, "/path/changeSet.xml", "", "", databaseChangeLog);
        databaseChangeLog.addChangeSet(changeSet);
        return changeSet;
    }
}
