package liquibase.changelog;

import liquibase.database.DatabaseConnection;
import liquibase.database.core.MockDatabase;
import org.junit.Test;

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
}
