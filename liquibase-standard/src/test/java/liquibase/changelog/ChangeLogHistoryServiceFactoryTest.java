package liquibase.changelog;

import liquibase.Scope;
import liquibase.database.core.PostgresDatabase;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeLogHistoryServiceFactoryTest {

    private ChangeLogHistoryServiceFactory factory;

    @Before
    public void setUp() {
        factory = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class);
        factory.resetAll();
    }

    /**
     * Regression test for https://github.com/liquibase/liquibase/issues/6927 (comment thread):
     * two pre-configured services registered for two different databases must resolve back to
     * their own database, not collapse into a single shared instance. Before the fix, registering
     * through the generic {@link ChangeLogHistoryServiceFactory#register(ChangeLogHistoryService)}
     * path let {@link liquibase.plugin.AbstractPluginFactory#getPlugins} silently collapse both
     * registrations into one, because its TreeSet orders candidates by (priority, class name) and
     * both services share the same class and priority.
     */
    @Test
    public void registerForDatabaseIsolatesInstancesPerDatabase() {
        PostgresDatabase databaseA = new PostgresDatabase();
        PostgresDatabase databaseB = new PostgresDatabase();
        ChangeLogHistoryService serviceA = new StandardChangeLogHistoryService();
        serviceA.setDatabase(databaseA);
        ChangeLogHistoryService serviceB = new StandardChangeLogHistoryService();
        serviceB.setDatabase(databaseB);

        factory.registerForDatabase(databaseA, serviceA);
        factory.registerForDatabase(databaseB, serviceB);

        assertThat(factory.getChangeLogService(databaseA)).isSameAs(serviceA);
        assertThat(factory.getChangeLogService(databaseB)).isSameAs(serviceB);
        assertThat(factory.getChangeLogService(databaseA))
                .isNotSameAs(factory.getChangeLogService(databaseB));
    }

    /**
     * Regression test: {@link ChangeLogHistoryServiceFactory#resetDatabase} must invalidate only the
     * given database's service, unlike {@link ChangeLogHistoryServiceFactory#resetAll()} which (by
     * design) invalidates and drops every database's entry. Several command-step cleanUp() paths used
     * to call the blanket resetAll() on every command completion; since this factory is a single
     * JVM-wide singleton (since #7877), that discarded other concurrently-running Maven modules'
     * cached services too.
     */
    @Test
    public void resetDatabaseOnlyAffectsGivenDatabase() {
        PostgresDatabase databaseA = new PostgresDatabase();
        PostgresDatabase databaseB = new PostgresDatabase();
        RecordingChangeLogHistoryService serviceA = new RecordingChangeLogHistoryService();
        serviceA.setDatabase(databaseA);
        RecordingChangeLogHistoryService serviceB = new RecordingChangeLogHistoryService();
        serviceB.setDatabase(databaseB);

        factory.registerForDatabase(databaseA, serviceA);
        factory.registerForDatabase(databaseB, serviceB);

        factory.resetDatabase(databaseA);

        assertThat(serviceA.resetCount).isEqualTo(1);
        assertThat(serviceB.resetCount).isZero();
    }

    /**
     * resetDatabase() resets the service in place rather than evicting it. The binding survives, so a
     * database used for more than one command in the same JVM run - e.g. an offline connection, whose
     * service the generic plugin lookup cannot rebuild - keeps its own service for the next command
     * instead of silently falling back to StandardChangeLogHistoryService.
     */
    @Test
    public void resetDatabaseKeepsTheServiceBoundToItsDatabase() {
        PostgresDatabase database = new PostgresDatabase();
        ChangeLogHistoryService service = new StandardChangeLogHistoryService();
        service.setDatabase(database);
        factory.registerForDatabase(database, service);

        factory.resetDatabase(database);

        assertThat(factory.getChangeLogService(database)).isSameAs(service);
    }

    private static class RecordingChangeLogHistoryService extends StandardChangeLogHistoryService {
        private int resetCount;

        @Override
        public void reset() {
            resetCount++;
            super.reset();
        }
    }
}
