package liquibase.threading;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.changelog.ChangeSet;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.After;
import org.junit.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @author github.com/bvremmeinfor
 */
public class LiquibaseThreadingTest {

    private static final String DATABASE_NAME_PREFIX = "DB_MT_";
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<String, MemoryDatabase> liveConnections = new ConcurrentHashMap<>();

    @After
    public void tearDown() {
        teardownLiveConnections();
        shutdownExecutorService();
    }

    @Test
    public void itCanMaintainDatabasesInParallel() {
        /*
         * 4 threads seems to be sufficient to provoke most errors
         */
        final int threadCount = Math.min(16, Runtime.getRuntime().availableProcessors() * 2);

        System.err.println("Liquibase threading test will use " + threadCount + " threads.");

        assertMaintainDatabases(threadCount);
    }


    private void assertMaintainDatabases(final int threadCount) {
        final List<Future<?>> maintainTasks = new ArrayList<>();

        /*
         * We want to stress initialization as much as possible, so we
         * wait for all thread ready before we start.
         */
        final ThreadAligner threadAligner = new ThreadAligner(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final String dbName = DATABASE_NAME_PREFIX + i;

            createMemoryDatabase(dbName);

            maintainTasks.add(executor.submit(() -> {
                threadAligner.awaitAllReady();
                maintainDatabase(dbName);
            }));
        }

        maintainTasks.forEach(LiquibaseThreadingTest::assertResolved);
    }

    private void maintainDatabase(final String dbName) {

        System.out.println("-- maintaining database: " + dbName);

        final MemoryDatabase db = getDatabase(dbName);

        try (Connection con = db.getConnection()) {

            final Map<String, Object> liquibaseConfiguration = new LinkedHashMap<>();
            liquibaseConfiguration.put("liquibase.analytics.devOverride", true);
            liquibaseConfiguration.put("liquibase.analytics.configEndpointUrl", "http://localhost:5000");

            Scope.child(liquibaseConfiguration, () -> {

                final Liquibase liquibase = new Liquibase("/changelogs/threading/changelog.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(con));

                final List<ChangeSet> pending = liquibase.listUnrunChangeSets(new Contexts(), new LabelExpression());
                if (pending.isEmpty()) {
                    fail("Expected pending database changesets");
                }

                liquibase.update(new Contexts(), new LabelExpression());

                final List<String> tableNames = db.queryTables();

                assertTrue("Expected to find table1 in " + tableNames, tableNames.contains("table1"));
                assertTrue("Expected to find table2 in " + tableNames, tableNames.contains("table2"));

                System.out.println("-- database maintenance OK for: " + dbName);
            });

        } catch (Exception e) {
            System.out.println("-- database maintenance failed for: " + dbName);
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void createMemoryDatabase(final String dbName) {
        System.out.println("-- creating memory database: " + dbName);
        liveConnections.put(dbName, MemoryDatabase.create(dbName));
        System.out.println("-- memory database created: " + dbName);
    }

    private MemoryDatabase getDatabase(final String dbName) {
        final MemoryDatabase db = liveConnections.get(dbName);
        assertNotNull("Memory database not created for " + dbName, db);
        return db;
    }

    private static <T> T assertResolved(Future<T> future) {
        try {
            return future.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted");
        } catch (TimeoutException | ExecutionException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void shutdownExecutorService() {
        executor.shutdownNow();

        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Failed to terminate all threads in a timely fashion");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for all threads to terminate in a timely fashion");
        }
    }

    private void teardownLiveConnections() {
        final Map<String, MemoryDatabase> all = new LinkedHashMap<>(liveConnections);
        liveConnections.clear();

        all.values().forEach(MemoryDatabase::close);

        final List<String> notClosed = all.keySet().stream().filter(MemoryDatabase::databaseExists).collect(Collectors.toList());

        if (!notClosed.isEmpty()) {
            throw new IllegalStateException("Failed to close all databases, open: " + notClosed);
        }
    }

    private static class ThreadAligner {

        private final CyclicBarrier barrier;

        public ThreadAligner(int threads) {
            this.barrier = new CyclicBarrier(threads);
        }

        void awaitAllReady() {
            try {
                barrier.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            } catch (BrokenBarrierException e) {
                throw new IllegalStateException(e);
            }
        }
    }

}
