package liquibase.changelog;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeLogHistoryServiceFactoryTest {

    private ChangeLogHistoryServiceFactory factory;

    @Before
    public void setUp() {
        factory = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class);
        factory.resetAll();
    }

    /**
     * Regression test for cross-module state corruption in parallel (-T) Maven reactor builds:
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

    /**
     * A service registered for a database never enters the plugin registry, so resetAll()'s loop over
     * findAllInstances() does not reach it. It must still be reset before the map is cleared, or a full
     * teardown would silently drop it without invalidating it.
     */
    @Test
    public void resetAllResetsServicesRegisteredForADatabase() {
        PostgresDatabase database = new PostgresDatabase();
        RecordingChangeLogHistoryService registered = new RecordingChangeLogHistoryService();
        registered.setDatabase(database);
        factory.registerForDatabase(database, registered);

        factory.resetAll();

        assertThat(registered.resetCount).isEqualTo(1);
    }

    /**
     * removeDatabase() ends the binding between a database and its service, for just that database:
     * other databases - including ones whose service was registered explicitly, like an offline
     * connection's - keep theirs.
     */
    @Test
    public void removeDatabaseOnlyRemovesGivenDatabase() throws Exception {
        PostgresDatabase databaseA = new PostgresDatabase();
        PostgresDatabase databaseB = new PostgresDatabase();
        RecordingChangeLogHistoryService serviceA = new RecordingChangeLogHistoryService();
        serviceA.setDatabase(databaseA);
        RecordingChangeLogHistoryService serviceB = new RecordingChangeLogHistoryService();
        serviceB.setDatabase(databaseB);
        factory.registerForDatabase(databaseA, serviceA);
        factory.registerForDatabase(databaseB, serviceB);

        factory.removeDatabase(databaseA);

        assertThat(serviceA.resetCount).isEqualTo(1);
        assertThat(cachedDatabases()).noneMatch(cached -> cached == databaseA);
        assertThat(serviceB.resetCount).isZero();
        assertThat(factory.getChangeLogService(databaseB)).isSameAs(serviceB);
    }

    /**
     * Regression test for #8064: once a database is closed, the JVM-wide factory must no longer hold
     * it. The cleanup at the end of a command only resets the database's entry in place
     * ({@link ChangeLogHistoryServiceFactory#resetDatabase}), so without this every closed database -
     * and through its connection whatever the connection references, e.g. a pooled DataSource and the
     * application context around it - stayed reachable for the life of the JVM.
     * <p>
     * The cache is inspected by identity rather than with containsKey(): a JdbcConnection's hashCode
     * changes once it is closed, so a lookup by key can miss an entry that is still retained.
     */
    @Test
    public void closingADatabaseReleasesItsService() throws Exception {
        PostgresDatabase otherDatabase = new PostgresDatabase();
        RecordingChangeLogHistoryService otherService = new RecordingChangeLogHistoryService();
        otherService.setDatabase(otherDatabase);
        factory.registerForDatabase(otherDatabase, otherService);

        for (int run = 0; run < 3; run++) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(
                    DriverManager.getConnection("jdbc:h2:mem:closingADatabaseReleasesItsService" + run)));
            try (Liquibase liquibase = new Liquibase(new DatabaseChangeLog("closingADatabaseReleasesItsService"),
                    new ClassLoaderResourceAccessor(), database)) {
                liquibase.update(new Contexts(), new LabelExpression());
                assertThat(cachedDatabases()).anyMatch(cached -> cached == database);
            }

            assertThat(cachedDatabases())
                    .as("closed database of run %d must not be retained", run)
                    .noneMatch(cached -> cached == database);
        }

        // closing a database must not touch the services of other databases
        assertThat(otherService.resetCount).isZero();
        assertThat(factory.getChangeLogService(otherDatabase)).isSameAs(otherService);
    }

    @SuppressWarnings("unchecked")
    private Set<Database> cachedDatabases() throws Exception {
        Field services = ChangeLogHistoryServiceFactory.class.getDeclaredField("services");
        services.setAccessible(true);
        return ((Map<Database, ChangeLogHistoryService>) services.get(factory)).keySet();
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
