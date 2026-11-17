package liquibase.changelog;

import liquibase.GlobalConfiguration;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.MockDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.executor.ExecutorService;
import liquibase.sdk.executor.MockExecutor;
import liquibase.sqlgenerator.core.MarkChangeSetRanGenerator;
import org.junit.Test;

import java.sql.DriverManager;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * By default, duplicate tags are intentional (#4317: rollback --tag-version defaults to resolving to the
     * oldest match), so re-tagging must leave the earlier cached entry's tag untouched.
     */
    @Test
    public void tagLeavesEarlierCachedTagInPlaceByDefault() throws Exception {
        withTaggedTwice((service, changeSet1Id, changeSet2Id) -> {
            List<RanChangeSet> taggedEntries = service.getRanChangeSets().stream()
                    .filter(ranChangeSet -> "release_tag".equals(ranChangeSet.getTag()))
                    .collect(Collectors.toList());

            assertThat(taggedEntries).extracting(RanChangeSet::getId)
                    .containsExactlyInAnyOrder(changeSet1Id, changeSet2Id);
        });
    }

    /**
     * The service caches the deployed changesets in ranChangeSetList and keeps that cache across calls
     * within the same JVM (ChangeLogHistoryServiceFactory reuses the same service per Database), so with
     * liquibase.clearDuplicateTags enabled, re-tagging has to clear the tag off whichever cached entry
     * already carries it, otherwise the in-memory history disagrees with DATABASECHANGELOG about which
     * changeset the tag belongs to (#3763).
     */
    @Test
    public void tagClearsTheTagFromAnyCachedRanChangeSetThatAlreadyCarriesItWhenEnabled() throws Exception {
        Scope.child(Collections.singletonMap(GlobalConfiguration.CLEAR_DUPLICATE_TAGS.getKey(), "true"), () ->
                withTaggedTwice((service, changeSet1Id, changeSet2Id) -> {
                    List<RanChangeSet> taggedEntries = service.getRanChangeSets().stream()
                            .filter(ranChangeSet -> "release_tag".equals(ranChangeSet.getTag()))
                            .collect(Collectors.toList());

                    assertThat(taggedEntries).hasSize(1);
                    assertThat(taggedEntries.get(0).getId()).isEqualTo(changeSet2Id);
                }));
    }

    private interface TaggedTwiceAssertion {
        void accept(StandardChangeLogHistoryService service, String changeSet1Id, String changeSet2Id) throws Exception;
    }

    private void withTaggedTwice(TaggedTwiceAssertion assertion) throws Exception {
        java.sql.Connection jdbcConnection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID(), "sa", "");
        try {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(jdbcConnection));
            StandardChangeLogHistoryService service = new StandardChangeLogHistoryService();
            service.setDatabase(database);
            service.init();
            service.getRanChangeSets(); // populate the cache so setExecType()/tag() start updating it

            DatabaseChangeLog changeLog = new DatabaseChangeLog("changelog.xml");
            ChangeSet changeSet1 = new ChangeSet("1", "author", false, false, "changelog.xml", null, null, changeLog);
            ChangeSet changeSet2 = new ChangeSet("2", "author", false, false, "changelog.xml", null, null, changeLog);

            service.setExecType(changeSet1, ChangeSet.ExecType.EXECUTED);
            service.tag("release_tag");

            service.setExecType(changeSet2, ChangeSet.ExecType.EXECUTED);
            service.tag("release_tag");

            assertion.accept(service, "1", "2");
        } finally {
            jdbcConnection.close();
        }
    }
}
