package liquibase.lockservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.core.CockroachDatabase;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LockException;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Verifies the opt-in gating: {@link PostgreSQLSessionLockService} is only selectable when
 * {@code liquibase.useSessionLock} is enabled AND the database is PostgreSQL 9.1+, and that it
 * outranks {@link StandardLockService} so Liquibase picks it once enabled. Default behaviour
 * (flag off) is unchanged. Also covers the lock-key scoping, configurable wait/recheck times, the
 * already-released unlock path, and null {@code backend_start} handling with mocked JDBC; the
 * end-to-end session-death guarantee lives in the integration test of the same name.
 */
public class PostgreSQLSessionLockServiceTest {

    private static final String TRY_LOCK_SQL = "SELECT pg_try_advisory_lock(?, ?)";
    private static final String UNLOCK_SQL = "SELECT pg_advisory_unlock(?, ?)";
    private static final String LOCK_TABLE = "DATABASECHANGELOGLOCK";

    private final PostgreSQLSessionLockService lockService = new PostgreSQLSessionLockService();

    @Test
    public void notSupportedByDefaultEvenOnPostgres() {
        assertThat(lockService.supports(postgres(14, 0))).isFalse();
    }

    @Test
    public void supportedOnPostgres91PlusWhenEnabled() throws Exception {
        Scope.child(GlobalConfiguration.USE_SESSION_LOCK.getKey(), true, () -> {
            assertThat(lockService.supports(postgres(14, 0))).isTrue();
            assertThat(lockService.supports(postgres(9, 1))).isTrue();
        });
    }

    @Test
    public void notSupportedOnOlderPostgresEvenWhenEnabled() throws Exception {
        Scope.child(GlobalConfiguration.USE_SESSION_LOCK.getKey(), true, () -> {
            assertThat(lockService.supports(postgres(9, 0))).isFalse();
        });
    }

    @Test
    public void notSupportedOnOtherDatabasesEvenWhenEnabled() throws Exception {
        Scope.child(GlobalConfiguration.USE_SESSION_LOCK.getKey(), true, () -> {
            assertThat(lockService.supports(new MockDatabase())).isFalse();
        });
    }

    @Test
    public void notSupportedOnPostgresVariantsWithoutAdvisoryLocks() throws Exception {
        // CockroachDatabase extends PostgresDatabase, so instanceof alone would pick it up even
        // though its advisory-lock support is partial. It opts down via supportsAdvisoryLocks().
        Scope.child(GlobalConfiguration.USE_SESSION_LOCK.getKey(), true, () -> {
            assertThat(lockService.supports(new CockroachDatabase())).isFalse();
            assertThat(new CockroachDatabase().supportsAdvisoryLocks()).isFalse();
            assertThat(new PostgresDatabase().supportsAdvisoryLocks()).isTrue();
        });
    }

    @Test
    public void outranksStandardLockService() {
        assertThat(lockService.getPriority()).isGreaterThan(new StandardLockService().getPriority());
    }

    @Test
    public void acquireLockKeysOnLiquibaseSchemaNotCurrentSchema() throws Exception {
        // The lock key must be scoped to the schema the lock table lives in (liquibaseSchemaName),
        // not the connection's current_schema(). A deployment that sets liquibase.liquibaseSchemaName
        // explicitly relies on this to keep mutual exclusion aligned with the table's location.
        Connection connection = mock(Connection.class);
        PreparedStatement tryLock = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(TRY_LOCK_SQL)).thenReturn(tryLock);
        when(tryLock.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject(1)).thenReturn(Boolean.TRUE);

        PostgresDatabase database = databaseOn(connection, "migrations", null);
        lockService.setDatabase(database);

        assertThat(lockService.acquireLock()).isTrue();
        verify(tryLock).setInt(1, LOCK_TABLE.hashCode());
        verify(tryLock).setInt(2, "migrations".hashCode());
        // The implicit transaction is ended after the lock SQL so the migration does not start
        // mid-transaction.
        verify(database).rollback();
    }

    @Test
    public void releaseLockFailsWhenTheSessionNoLongerHoldsTheLock() throws Exception {
        // We acquired the lock, so pg_advisory_unlock returning false means the session lock was
        // lost mid-run (a backend re-established behind a pool, a DISCARD ALL). The change log was
        // unguarded for part of the run, which is precisely what this service exists to prevent,
        // so it must fail rather than log.
        Connection connection = mock(Connection.class);
        PreparedStatement tryLock = mock(PreparedStatement.class);
        ResultSet acquireResultSet = mock(ResultSet.class);
        when(connection.prepareStatement(TRY_LOCK_SQL)).thenReturn(tryLock);
        when(tryLock.executeQuery()).thenReturn(acquireResultSet);
        when(acquireResultSet.next()).thenReturn(true);
        when(acquireResultSet.getObject(1)).thenReturn(Boolean.TRUE);
        PreparedStatement unlock = mock(PreparedStatement.class);
        ResultSet unlockResultSet = mock(ResultSet.class);
        when(connection.prepareStatement(UNLOCK_SQL)).thenReturn(unlock);
        when(unlock.executeQuery()).thenReturn(unlockResultSet);
        when(unlockResultSet.next()).thenReturn(true);
        when(unlockResultSet.getObject(1)).thenReturn(Boolean.FALSE);

        lockService.setDatabase(databaseOn(connection, "public", null));
        assertThat(lockService.acquireLock()).isTrue();

        assertThatThrownBy(lockService::releaseLock)
                .isInstanceOf(LockException.class)
                .hasMessageContaining("no longer held by this database session");
        // The service must not keep claiming a lock the database says it does not hold.
        assertThat(lockService.hasChangeLogLock()).isFalse();
    }

    @Test
    public void forceReleaseLockTreatsFalseAsAlreadyReleased() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement unlock = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(UNLOCK_SQL)).thenReturn(unlock);
        when(unlock.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        // false: the session no longer holds the lock (e.g. the connection was dropped). Not an error.
        when(resultSet.getObject(1)).thenReturn(Boolean.FALSE);
        // forceReleaseLock re-reads the locks afterwards; nobody else holds this one.
        stubLockInfo(connection, false);

        lockService.setDatabase(databaseOn(connection, "public", null));

        // Must not throw: forceReleaseLock is the administrative path and runs whether or not
        // this session holds the lock, so not holding it is normal rather than a lost lock.
        lockService.forceReleaseLock();
        assertThat(lockService.hasChangeLogLock()).isFalse();
    }

    @Test
    public void forceReleaseLockCannotBreakAnotherSessionsLock() throws Exception {
        // pg_advisory_unlock only releases locks held by the calling session, so releaseLocks
        // cannot break a lock another live session holds. It must not claim it did: the lock is
        // still reported as held, and it goes away by itself when that session ends.
        Connection connection = mock(Connection.class);
        PreparedStatement unlock = mock(PreparedStatement.class);
        ResultSet unlockResultSet = mock(ResultSet.class);
        when(connection.prepareStatement(UNLOCK_SQL)).thenReturn(unlock);
        when(unlock.executeQuery()).thenReturn(unlockResultSet);
        when(unlockResultSet.next()).thenReturn(true);
        when(unlockResultSet.getObject(1)).thenReturn(Boolean.FALSE);
        stubLockInfo(connection, true);

        lockService.setDatabase(databaseOn(connection, "public", null));

        lockService.forceReleaseLock();

        assertThat(lockService.hasChangeLogLock()).isFalse();
        assertThat(lockService.listLocks()).hasSize(1);
    }

    @Test
    public void listLocksToleratesNullBackendStart() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement lockInfo = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(Mockito.startsWith("SELECT l.pid"))).thenReturn(lockInfo);
        when(lockInfo.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getTimestamp("backend_start")).thenReturn(null);
        when(resultSet.getString("client_hostname")).thenReturn("db.example");
        when(resultSet.getString("state")).thenReturn(null);

        lockService.setDatabase(databaseOn(connection, "public", null));

        DatabaseChangeLogLock[] locks = lockService.listLocks();
        assertThat(locks).hasSize(1);
        // A null backend_start (system process, restricted visibility, termination race) is coerced
        // to the epoch instead of NPEing in the DatabaseChangeLogLock constructor.
        assertThat(locks[0].getLockGranted()).isEqualTo(new Date(0L));
        assertThat(locks[0].getLockedBy()).isEqualTo("db.example (unknown)");
    }

    @Test
    public void waitAndRecheckTimesFallBackToGlobalConfigButHonourSetters() {
        SessionLockService service = new PostgreSQLSessionLockService();
        assertThat(service.getChangeLogLockWaitTimeMinutes())
                .isEqualTo(GlobalConfiguration.CHANGELOGLOCK_WAIT_TIME.getCurrentValue());
        assertThat(service.getChangeLogLockRecheckTimeSeconds())
                .isEqualTo(GlobalConfiguration.CHANGELOGLOCK_POLL_RATE.getCurrentValue());

        service.setChangeLogLockWaitTime(42);
        service.setChangeLogLockRecheckTime(7);
        assertThat(service.getChangeLogLockWaitTimeMinutes()).isEqualTo(42);
        assertThat(service.getChangeLogLockRecheckTimeSeconds()).isEqualTo(7);
    }

    /** Stubs the pg_locks lookup used by listLocks(), optionally reporting one holder. */
    private static void stubLockInfo(Connection connection, boolean locked) throws Exception {
        PreparedStatement lockInfo = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(Mockito.startsWith("SELECT l.pid"))).thenReturn(lockInfo);
        when(lockInfo.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(locked);
        when(resultSet.getTimestamp("backend_start")).thenReturn(new Timestamp(0L));
        when(resultSet.getString("client_hostname")).thenReturn("other.example");
        when(resultSet.getString("state")).thenReturn("idle");
    }

    private static PostgresDatabase databaseOn(Connection connection, String schemaName, String catalogName) {
        try {
            JdbcConnection jdbcConnection = mock(JdbcConnection.class);
            when(jdbcConnection.getUnderlyingConnection()).thenReturn(connection);
            PostgresDatabase database = mock(PostgresDatabase.class);
            when(database.getConnection()).thenReturn(jdbcConnection);
            when(database.getLiquibaseSchemaName()).thenReturn(schemaName);
            when(database.getLiquibaseCatalogName()).thenReturn(catalogName);
            when(database.getDatabaseChangeLogLockTableName()).thenReturn(LOCK_TABLE);
            return database;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PostgresDatabase postgres(int majorVersion, int minorVersion) {
        try {
            PostgresDatabase database = Mockito.mock(PostgresDatabase.class);
            Mockito.when(database.getDatabaseMajorVersion()).thenReturn(majorVersion);
            Mockito.when(database.getDatabaseMinorVersion()).thenReturn(minorVersion);
            // A mock defaults to false; real PostgreSQL reports true. The false case is covered
            // against a real CockroachDatabase in notSupportedOnPostgresVariantsWithoutAdvisoryLocks.
            Mockito.when(database.supportsAdvisoryLocks()).thenReturn(true);
            return database;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
