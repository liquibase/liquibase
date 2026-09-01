package liquibase.changelog;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.MockDatabase;
import liquibase.database.jvm.JdbcConnection;
import org.junit.Test;

import java.sql.DriverManager;
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

    /**
     * The service caches the deployed changesets in ranChangeSetList and keeps that cache across calls
     * within the same JVM (ChangeLogHistoryServiceFactory reuses the same service per Database), so
     * re-tagging has to clear the tag off whichever cached entry already carries it -- otherwise the
     * in-memory history disagrees with DATABASECHANGELOG about which changeset the tag belongs to (#3763).
     */
    @Test
    public void tagClearsTheTagFromAnyCachedRanChangeSetThatAlreadyCarriesIt() throws Exception {
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

            List<RanChangeSet> taggedEntries = service.getRanChangeSets().stream()
                    .filter(ranChangeSet -> "release_tag".equals(ranChangeSet.getTag()))
                    .collect(Collectors.toList());

            assertThat(taggedEntries).hasSize(1);
            assertThat(taggedEntries.get(0).getId()).isEqualTo("2");
        } finally {
            jdbcConnection.close();
        }
    }
}
