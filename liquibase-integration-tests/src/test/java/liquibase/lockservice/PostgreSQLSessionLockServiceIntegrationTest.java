package liquibase.lockservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * End-to-end proof of the whole reason {@link PostgreSQLSessionLockService} exists: when the
 * session holding the lock dies (a pod kill, OOM, {@code kill -9}), PostgreSQL releases the
 * session-level advisory lock automatically and the next run acquires it without a manual
 * {@code releaseLocks}. {@link StandardLockService} would leave a committed {@code LOCKED} row
 * behind here.
 * <p>
 * Killing the session is simulated with {@code pg_terminate_backend(pid)} rather than closing the
 * JDBC connection: a graceful close lets the driver/pool tidy up, whereas terminate severs the
 * backend the way a crash does, which is the case the lock service is built for.
 */
public class PostgreSQLSessionLockServiceIntegrationTest {

    @SuppressWarnings("resource")
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:16-alpine"));

    @BeforeClass
    public static void startContainer() {
        POSTGRES.start();
    }

    @AfterClass
    public static void stopContainer() {
        POSTGRES.stop();
    }

    @Test
    public void sessionLockIsReleasedWhenTheHoldingSessionDies() throws Exception {
        Scope.child(GlobalConfiguration.USE_SESSION_LOCK.getKey(), true, () -> {
            PostgresDatabase holderDatabase = openDatabase();
            PostgresDatabase contenderDatabase = openDatabase();
            try {
                PostgreSQLSessionLockService holder = lockServiceFor(holderDatabase);
                PostgreSQLSessionLockService contender = lockServiceFor(contenderDatabase);

                assertThat(holder.acquireLock()).as("holder acquires the free lock").isTrue();
                assertThat(holder.listLocks()).as("the held lock is listed").hasSize(1);
                assertThat(contender.acquireLock())
                        .as("a second session cannot take a held session lock").isFalse();

                // Simulate the holder process dying mid-update.
                int holderBackendPid = backendPidOf(holderDatabase);
                terminateBackend(contenderDatabase, holderBackendPid);

                assertThat(acquiredWithin(contender, 15_000))
                        .as("the lock is auto-released when the holding session dies, so the "
                                + "contender eventually acquires it without a manual releaseLocks")
                        .isTrue();
            } finally {
                closeQuietly(holderDatabase);
                closeQuietly(contenderDatabase);
            }
        });
    }

    private static PostgreSQLSessionLockService lockServiceFor(Database database) {
        PostgreSQLSessionLockService lockService = new PostgreSQLSessionLockService();
        lockService.setDatabase(database);
        return lockService;
    }

    private static PostgresDatabase openDatabase() throws Exception {
        Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
        connection.setAutoCommit(false);
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(connection));
        return (PostgresDatabase) database;
    }

    private static int backendPidOf(Database database) throws Exception {
        Connection connection = underlyingConnection(database);
        try (PreparedStatement statement = connection.prepareStatement("SELECT pg_backend_pid()");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private static void terminateBackend(Database database, int backendPid) throws Exception {
        Connection connection = underlyingConnection(database);
        try (PreparedStatement statement = connection.prepareStatement("SELECT pg_terminate_backend(?)")) {
            statement.setInt(1, backendPid);
            statement.execute();
        }
        connection.rollback();
    }

    /** Polls acquireLock() until it succeeds or the timeout elapses. */
    private static boolean acquiredWithin(PostgreSQLSessionLockService lockService, long timeoutMillis)
            throws Exception {
        long giveUpAt = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < giveUpAt) {
            if (lockService.acquireLock()) {
                return true;
            }
            Thread.sleep(250);
        }
        return false;
    }

    private static Connection underlyingConnection(Database database) {
        return ((JdbcConnection) database.getConnection()).getUnderlyingConnection();
    }

    private static void closeQuietly(Database database) {
        try {
            underlyingConnection(database).close();
        } catch (Exception e) {
            // The holder connection is already dead after pg_terminate_backend; nothing to do.
        }
    }
}
