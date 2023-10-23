package liquibase.changelog;

import liquibase.Scope;
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
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testGenerateDeploymentId() throws Exception{
        StringWriter writer = new StringWriter();
        OfflineChangeLogHistoryService service = createService(writer, "data_only");
        ChangeSet changeSet = createChangeSet();
        // When
        service.init();
        service.setExecType(changeSet, ChangeSet.ExecType.EXECUTED);
        writer.close();

        service.generateDeploymentId();
        String deploymentId = service.getDeploymentId();

        assertTrue(deploymentId.length() == 10);

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
     * Create ChangeLog and ChangeSet
     */
    private ChangeSet createChangeSet() {
        DatabaseChangeLog databaseChangeLog = new DatabaseChangeLog("/patch/changeLog.xml");
        ChangeSet changeSet = new ChangeSet("id", "author", false, false, "/path/changeSet.xml", "", "", databaseChangeLog);
        databaseChangeLog.addChangeSet(changeSet);
        return changeSet;
    }
}
