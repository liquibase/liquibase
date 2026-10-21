package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.MockDatabase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author John Sanda
 */
public class LockServiceFactoryTest {

    @Before
    public void before() {
        LockServiceFactory.getInstance().resetAll();
    }

    @After
    public void after() {
        LockServiceFactory.getInstance().resetAll();
    }

    @Test
    public void getInstance() {
        assertNotNull(LockServiceFactory.getInstance());
        assertSame(LockServiceFactory.getInstance(), LockServiceFactory.getInstance());

//        Collection<LockService> lockServices = LockServiceFactory.getInstance().getLockServices();
//        assertEquals(0, lockServices.size());
    }

    @Test
    public void getLockService() {
        final Database oracle1 = new OracleDatabase() {
            @Override
            public boolean equals(Object o) {
                return o == this;
            }
        };
        final Database oracle2 = new OracleDatabase() {
            @Override
            public boolean equals(Object o) {
                return o == this;
            }

        };
        final Database mysql = new MySQLDatabase() {
            @Override
            public boolean equals(Object o) {
                return o == this;
            }
        };

        DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
        databaseFactory.register(oracle1);
        databaseFactory.register(oracle2);
        databaseFactory.register(mysql);

        LockServiceFactory lockServiceFactory = LockServiceFactory.getInstance();

        assertNotNull(lockServiceFactory.getLockService(oracle1));
        assertNotNull(lockServiceFactory.getLockService(oracle2));
        assertNotNull(lockServiceFactory.getLockService(mysql));

        assertSame(lockServiceFactory.getLockService(oracle1), lockServiceFactory.getLockService(oracle1));
        assertSame(lockServiceFactory.getLockService(oracle2), lockServiceFactory.getLockService(oracle2));
        assertSame(lockServiceFactory.getLockService(mysql), lockServiceFactory.getLockService(mysql));

        assertNotSame(lockServiceFactory.getLockService(oracle1), lockServiceFactory.getLockService(oracle2));
        assertNotSame(lockServiceFactory.getLockService(oracle1), lockServiceFactory.getLockService(mysql));

        assertTrue(lockServiceFactory.getLockService(getMockDatabase()) instanceof MockLockService);
    }

    @Test
    public void resetAll_isThreadSafe() throws InterruptedException {

        final int threadCount = 12;
        final ExecutorService executor = Executors.newCachedThreadPool();

        final AtomicLong errors = new AtomicLong();
        final AtomicLong npeErrors = new AtomicLong();

        final CyclicBarrier startBarrier = new CyclicBarrier(threadCount);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);

        try {

            for (int i = 0; i < threadCount; i++) {
                executor.execute(() -> {
                    try {
                        startBarrier.await();

                        for (int j = 0; j < 10000; j++) {
                            final LockServiceFactory instance = LockServiceFactory.getInstance();
                            Thread.currentThread().sleep(0); // Thread interleaving
                            instance.resetAll();
                        }

                    } catch (NullPointerException e) {
                        errors.incrementAndGet();
                        npeErrors.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        errors.incrementAndGet();
                    } catch (BrokenBarrierException | RuntimeException e) {
                        errors.incrementAndGet();
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            endLatch.await();
            Assert.assertEquals("NPE Errors", 0, npeErrors.get());
            Assert.assertEquals("Errors", 0, errors.get());

        } finally {
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }
    }

    /**
     * Regression test: a finishing execution must not strip a concurrently running one of the
     * {@link LockService} that knows it holds the changelog lock. {@link LockServiceFactory} is a
     * JVM-wide singleton, so the blanket {@link LockServiceFactory#resetAll()} that command cleanup
     * paths used to call discarded every database's lock service, not just the finishing one's. The
     * peer then looked up a freshly built service, whose {@link LockService#hasChangeLogLock()} is
     * false, silently skipped its release, and left its DATABASECHANGELOGLOCK row set until the next
     * start had waited out the full changelog lock wait time.
     */
    @Test
    public void resetDatabaseLeavesOtherDatabasesLockServicesIntact() throws Exception {
        LockServiceFactory factory = LockServiceFactory.getInstance();
        factory.register(new FakeLockService());
        Database databaseA = new PostgresDatabase();
        Database databaseB = new PostgresDatabase();

        FakeLockService serviceA = (FakeLockService) factory.getLockService(databaseA);
        FakeLockService serviceB = (FakeLockService) factory.getLockService(databaseB);
        serviceA.acquireLock();
        serviceB.acquireLock();

        // execution A finishes while B is still between acquiring and releasing its lock
        factory.resetDatabase(databaseA);

        assertSame("B must keep the very service that holds its lock", serviceB,
                factory.getLockService(databaseB));
        assertTrue("B must still know it holds the changelog lock, so it can release it",
                factory.getLockService(databaseB).hasChangeLogLock());
        assertEquals("B's service must not be reset by A's cleanup", 0, serviceB.resetCount);
    }

    /**
     * resetDatabase() is a teardown for the given database: its service is reset and dropped, so the
     * next execution against that database builds a fresh one rather than inheriting stale state.
     */
    @Test
    public void resetDatabaseResetsAndDropsTheGivenDatabasesLockService() throws Exception {
        LockServiceFactory factory = LockServiceFactory.getInstance();
        factory.register(new FakeLockService());
        Database database = new PostgresDatabase();

        FakeLockService service = (FakeLockService) factory.getLockService(database);
        service.acquireLock();

        factory.resetDatabase(database);

        assertEquals(1, service.resetCount);
        assertFalse(service.hasChangeLogLock());
        assertNotSame(service, factory.getLockService(database));
    }

    /**
     * resetDatabase() on a database this factory never handed a service out for is a no-op, not an NPE:
     * cleanup paths run even when the pipeline failed before any lock was taken.
     */
    @Test
    public void resetDatabaseOnAnUnknownDatabaseIsANoOp() {
        LockServiceFactory.getInstance().resetDatabase(new PostgresDatabase());
    }

    /**
     * resetAll() used to iterate only the registry - the prototypes the service locator discovered,
     * which never hold a lock - and then null the singleton, orphaning the per-database services
     * getLockService() actually hands out without ever resetting them. It must reset the open services
     * too.
     */
    @Test
    public void resetAllResetsTheServicesThatActuallyHoldLocks() throws Exception {
        LockServiceFactory factory = LockServiceFactory.getInstance();
        factory.register(new FakeLockService());

        FakeLockService openService = (FakeLockService) factory.getLockService(new PostgresDatabase());
        openService.acquireLock();

        factory.resetAll();

        assertEquals(1, openService.resetCount);
        assertFalse(openService.hasChangeLogLock());
    }

    /**
     * A LockService that can be driven without a database, and that outranks StandardLockService in
     * {@link LockServiceFactory#getLockService(Database)}'s priority-based lookup. Public with a
     * no-arg constructor because that lookup instantiates the winning candidate reflectively.
     */
    public static class FakeLockService extends StandardLockService {

        private boolean locked;
        private int resetCount;

        @Override
        public int getPriority() {
            return PRIORITY_DATABASE;
        }

        @Override
        public boolean supports(Database database) {
            return true;
        }

        @Override
        public boolean hasChangeLogLock() {
            return locked;
        }

        @Override
        public boolean acquireLock() {
            locked = true;
            return true;
        }

        @Override
        public void releaseLock() {
            locked = false;
        }

        @Override
        public void reset() {
            resetCount++;
            locked = false;
        }
    }

    private MockDatabase getMockDatabase() {
        DatabaseFactory factory = DatabaseFactory.getInstance();
        for (Database db : factory.getInternalDatabases()) {
            if (db instanceof MockDatabase) {
                return (MockDatabase) db;
            }
        }
        return null;
    }

}
