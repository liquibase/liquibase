package liquibase.changelog;

import liquibase.Labels;
import liquibase.Scope;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.MockDatabase;
import liquibase.executor.ExecutorService;
import liquibase.sdk.executor.MockExecutor;
import liquibase.sqlgenerator.core.MarkChangeSetRanGenerator;
import org.junit.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class StandardChangeLogHistoryServiceTest {

    /**
     * getNextSequenceValue() caches MAX(ORDEREXECUTED) and increments it in memory, so reset() has to
     * drop that cache: callers reset precisely when the database may have been written to by someone
     * else - most sharply in StandardLockService.acquireLock(), where holding the lock is what makes
     * the previous read stale. Unlike databaseChecksumsCompatible this value is not recomputed by
     * init(), so nothing else would clear it.
     */
    @Test
    public void resetClearsTheCachedSequenceValue() throws Exception {
        MockDatabase database = new MockDatabase();
        // No connection: getNextSequenceValue() then seeds from 0 instead of querying the database.
        database.setConnection((DatabaseConnection) null);
        StandardChangeLogHistoryService service = new StandardChangeLogHistoryService();
        service.setDatabase(database);

        assertThat(service.getNextSequenceValue()).isEqualTo(1);
        assertThat(service.getNextSequenceValue()).isEqualTo(2);

        service.reset();

        assertThat(service.getNextSequenceValue()).isEqualTo(1);
    }

    @Test
    public void setExecTypeCachesEntryMatchingThePersistedRow() throws Exception {
        final var database = new MockDatabase();
        final var executorService = Scope.getCurrentScope().getSingleton(ExecutorService.class);
        executorService.setExecutor("jdbc", database, new MockExecutor());
        try {
            final var service = new StandardChangeLogHistoryService() {
                @Override
                public boolean hasDatabaseChangeLogTable() {
                    return false;
                }
            };
            service.setDatabase(database);
            assertThat(service.getRanChangeSets()).isEmpty();

            final var changeLog = new DatabaseChangeLog("db/changelog.xml");
            changeLog.setIncludeLabels(new Labels("includeLabel"));
            final var changeSet = new ChangeSet("1", "author", false, false, "db/changelog.xml", "context1", null, changeLog);
            changeSet.setLabels(new Labels("label1"));

            Scope.child(Collections.singletonMap(Scope.Attr.deploymentId.name(), "1234567890"),
                    () -> service.setExecType(changeSet, ChangeSet.ExecType.EXECUTED));

            assertThat(service.getRanChangeSets()).hasSize(1);
            final var ranChangeSet = service.getRanChangeSets().get(0);
            assertThat(ranChangeSet.getExecType()).isEqualTo(ChangeSet.ExecType.EXECUTED);
            assertThat(ranChangeSet.getDeploymentId()).isEqualTo("1234567890");
            assertThat(ranChangeSet.getLiquibaseVersion()).isEqualTo(MarkChangeSetRanGenerator.getLiquibaseBuildVersion());
            assertThat(ranChangeSet.getContextExpression().toString()).isEqualTo(changeSet.buildFullContext());
            assertThat(ranChangeSet.getLabels().toString()).isEqualTo("includelabel,label1");
        } finally {
            executorService.clearExecutor("jdbc", database);
        }
    }
}
