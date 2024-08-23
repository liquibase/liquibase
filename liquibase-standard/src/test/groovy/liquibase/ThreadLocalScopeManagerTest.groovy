package liquibase

import liquibase.changelog.ChangeSet
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.LiquibaseException
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.util.StringUtil
import spock.lang.Ignore
import spock.lang.Specification

import java.sql.*
import java.util.concurrent.*
import java.util.stream.Collectors

import static org.junit.Assert.*

class ThreadLocalScopeManagerTest extends Specification {

    private static final String DATABASE_NAME_PREFIX = "DB_MT_"
    private final ExecutorService executor = Executors.newCachedThreadPool()
    private final Map<String, MemoryDatabase> liveConnections = new ConcurrentHashMap<>()

    private ScopeManager originalScopeManager

    def setup() {
        Scope.getCurrentScope()
        originalScopeManager = Scope.scopeManager.get()
        Scope.setScopeManager(new ThreadLocalScopeManager())
    }

    def cleanup() {
        teardownLiveConnections()
        shutdownExecutorService()

        Scope.setScopeManager(originalScopeManager);

    }

    void "maintain databases in parallel"() {
        when:
        /*
         * 4 threads seems to be sufficient to provoke most errors
         */
        final int threadCount = Math.min(16, Runtime.getRuntime().availableProcessors() * 2)

        System.out.println("Liquibase threading test will use " + threadCount + " threads.")

        final List<Future<?>> maintainTasks = new ArrayList<>()

        /*
         * We want to stress initialization as much as possible, so we
         * wait for all thread ready before we start.
         */
        final ThreadAligner threadAligner = new ThreadAligner(threadCount)

        for (int i = 0; i < threadCount; i++) {
            final String dbName = DATABASE_NAME_PREFIX + StringUtil.randomIdentifier(10) + i

            liveConnections.put(dbName, MemoryDatabase.create(dbName))

            maintainTasks.add(executor.submit({ ->
                threadAligner.awaitAllReady()
                maintainDatabase(dbName)
            }))
        }

        then:
        for (def task : maintainTasks) {
            task.get(30, TimeUnit.SECONDS)
        }
    }


    private void maintainDatabase(final String dbName) {
        final MemoryDatabase db = getDatabase(dbName)

        Connection con = db.getConnection()
        try {
            final Liquibase liquibase = new Liquibase(
                    "com/example/changelog.xml",
                    new ClassLoaderResourceAccessor(),
                    new JdbcConnection(con))

            final List<ChangeSet> pending = liquibase.listUnrunChangeSets(new Contexts(), new LabelExpression())
            assert !pending.isEmpty(): "Expected pending database changesets"

            liquibase.update(new Contexts(), new LabelExpression())

            final List<String> tableNames = db.queryTables()

            assert tableNames.contains("table1")
            assert tableNames.contains("table2")

        } catch (SQLException | LiquibaseException e) {
            throw new IllegalStateException(e.getMessage(), e)
        }
    }

    private MemoryDatabase getDatabase(final String dbName) {
        final MemoryDatabase db = liveConnections.get(dbName)
        assertNotNull("Memory database not created for " + dbName, db)
        return db
    }

    private void shutdownExecutorService() {
        executor.shutdownNow()

        try {
            executor.awaitTermination(5, TimeUnit.SECONDS)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt()
            throw new IllegalStateException("Failed to terminate all threads in a timely fashion")
        }
    }

    private void teardownLiveConnections() {
        for (MemoryDatabase db : liveConnections.values()) {
            db.close()
        }
    }

    private static class ThreadAligner {

        private final CyclicBarrier barrier

        ThreadAligner(int threads) {
            this.barrier = new CyclicBarrier(threads)
        }

        void awaitAllReady() {
            try {
                barrier.await()
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt()
                throw new IllegalStateException(e)
            } catch (BrokenBarrierException e) {
                throw new IllegalStateException(e)
            }
        }
    }

    static class MemoryDatabase implements AutoCloseable {

        private static final String SENSING_TABLE = "_fake_lock"

        private final String connectionUrl
        private final Connection mainConnection

        private boolean closed = false

        private MemoryDatabase(String connectionUrl, Connection mainConnection) {
            this.connectionUrl = connectionUrl
            this.mainConnection = mainConnection
        }

        static synchronized MemoryDatabase create(String dbName) {
            MemoryDatabase db = createUnvalidated(dbName)

            if (db.hasSensingTable()) {
                db.close()
                throw new IllegalStateException("Database already exists: " + dbName)
            }

            db.createSensingTable()

            return db
        }

        @Override
        synchronized void close() {
            closed = true
            try {
                mainConnection.close()
            } catch (SQLException e) {
                throw new IllegalStateException(e)
            }
        }

        synchronized Connection getConnection() {
            if (closed) {
                throw new IllegalStateException("Fake connection factory is closed!")
            }

            try {
                return createConnection(this.connectionUrl)
            } catch (SQLException e) {
                throw new IllegalStateException(e)
            }
        }

        List<String> queryTables() {
            return query("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA <> 'INFORMATION_SCHEMA'").stream()
                    .map({ rec -> rec.get("table_name").toLowerCase() })
                    .collect(Collectors.toList())
        }

        List<Map<String, String>> query(final String sql) {
            List<Map<String, String>> l = new ArrayList<>()

            Statement stat = this.mainConnection.createStatement()
            ResultSet rc = stat.executeQuery(sql)
            try {
                final ResultSetMetaData m = rc.getMetaData()
                while (rc.next()) {
                    Map<String, String> record = new LinkedHashMap<>()
                    for (int i = 1; i <= m.getColumnCount(); i++) {
                        record.put(m.getColumnName(i).toLowerCase(), rc.getString(i))
                    }
                    l.add(record)
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e)
            }

            return l
        }

        int update(String sql) {
            Statement stat = this.mainConnection.createStatement()
            try {
                return stat.executeUpdate(sql)
            } catch (SQLException e) {
                throw new IllegalStateException(e)
            }
        }


        private static MemoryDatabase createUnvalidated(String dbName) {
            try {
                final String connectionUrl = "jdbc:h2:mem:" + dbName
                final Connection con = createConnection(connectionUrl)
                return new MemoryDatabase(connectionUrl, con)
            } catch (SQLException e) {
                throw new IllegalStateException(e)
            }
        }

        private boolean hasSensingTable() {
            return queryTables().stream().anyMatch(SENSING_TABLE.&equals)
        }

        private void createSensingTable() {
            final String sql = "CREATE TABLE " + SENSING_TABLE + " ( DUMMY VARCHAR(100), PRIMARY KEY (DUMMY))"
            PreparedStatement stat = this.mainConnection.prepareStatement(sql)
            try {
                stat.executeUpdate()
            } catch (SQLException e) {
                throw new IllegalStateException(e)
            }
        }

        private static Connection createConnection(final String connectionUrl) throws SQLException {
            return DriverManager.getConnection(connectionUrl, "sa", "")
        }
    }
}
