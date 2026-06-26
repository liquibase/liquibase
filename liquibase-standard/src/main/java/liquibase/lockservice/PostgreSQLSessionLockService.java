package liquibase.lockservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;

/**
 * A {@link SessionLockService} backed by PostgreSQL <a
 * href="https://www.postgresql.org/docs/current/explicit-locking.html#ADVISORY-LOCKS">session-level
 * advisory locks</a> ({@code pg_try_advisory_lock}). The lock is held on the session and released
 * automatically by the server when the connection ends, so a process killed mid-update never leaves
 * a stale {@code DATABASECHANGELOGLOCK} row behind.
 * <p>
 * Selected only when {@code liquibase.useSessionLock} is enabled and the database is PostgreSQL 9.1
 * or newer; otherwise {@link StandardLockService} remains in effect.
 */
public class PostgreSQLSessionLockService extends SessionLockService {

    private static final String SQL_TRY_LOCK = "SELECT pg_try_advisory_lock(?, ?)";
    private static final String SQL_UNLOCK = "SELECT pg_advisory_unlock(?, ?)";
    private static final String SQL_LOCK_INFO =
            "SELECT l.pid, a.client_hostname, a.backend_start, a.state"
                    + " FROM pg_locks l"
                    + " LEFT JOIN pg_stat_activity a ON a.pid = l.pid"
                    + " WHERE l.locktype = 'advisory'"
                    + " AND l.classid = ?"
                    + " AND l.objid = ?"
                    // objsubid = 2 is the two-int4 advisory key form (objsubid = 1 is the single-int8 form)
                    + " AND l.objsubid = 2"
                    + " AND l.granted";

    @Override
    public boolean supports(Database database) {
        return GlobalConfiguration.USE_SESSION_LOCK.getCurrentValue()
                && (database instanceof PostgresDatabase)
                && isAtLeastPostgres91(database);
    }

    @Override
    protected boolean acquireLock(Connection connection) throws LockException {
        int[] lockId = getChangeLogLockId();
        try (PreparedStatement tryLockStatement = connection.prepareStatement(SQL_TRY_LOCK)) {
            tryLockStatement.setInt(1, lockId[0]);
            tryLockStatement.setInt(2, lockId[1]);
            return Boolean.TRUE.equals(queryForBoolean(tryLockStatement));
        } catch (SQLException e) {
            throw new LockException(e);
        }
    }

    @Override
    protected void releaseLock(Connection connection) throws LockException {
        int[] lockId = getChangeLogLockId();
        try (PreparedStatement unlockStatement = connection.prepareStatement(SQL_UNLOCK)) {
            unlockStatement.setInt(1, lockId[0]);
            unlockStatement.setInt(2, lockId[1]);
            Boolean released = queryForBoolean(unlockStatement);
            if (!Boolean.TRUE.equals(released)) {
                // false means this session no longer holds the lock (e.g. the connection was
                // dropped and PostgreSQL already released it). Nothing to release, and not an
                // error: let the caller clear hasChangeLogLock so a reused instance does not keep
                // reporting a lock it does not own.
                Scope.getCurrentScope().getLog(getClass()).warning("pg_advisory_unlock() returned " + released + "; the session lock was already released");
            }
        } catch (SQLException e) {
            throw new LockException(e);
        }
    }

    @Override
    public DatabaseChangeLogLock[] listLocks() throws LockException {
        int[] lockId = getChangeLogLockId();
        try (PreparedStatement lockInfoStatement = getConnection().prepareStatement(SQL_LOCK_INFO)) {
            lockInfoStatement.setInt(1, lockId[0]);
            lockInfoStatement.setInt(2, lockId[1]);
            try (ResultSet lockInfoResultSet = lockInfoStatement.executeQuery()) {
                if (!lockInfoResultSet.next()) {
                    return new DatabaseChangeLogLock[0];
                }
                // This reports any session in the cluster holding an advisory lock with our
                // classid/objid; because the key embeds the lock-table schema, that is effectively
                // only sessions guarding the same changelog, so the first row is the current holder.
                //
                // backend_start (session start) is only an upper bound on when the lock was taken;
                // PostgreSQL does not record the actual advisory-lock acquisition time. It comes
                // from the LEFT-JOINed pg_stat_activity and can be null (system processes, a race
                // with backend termination, or restricted visibility for non-superusers); the
                // DatabaseChangeLogLock constructor would NPE on null, so fall back to the epoch.
                Timestamp lockGranted = lockInfoResultSet.getTimestamp("backend_start");
                Date lockGrantedDate = lockGranted == null ? new Date(0L) : lockGranted;
                DatabaseChangeLogLock lock = new DatabaseChangeLogLock(1, lockGrantedDate, describeLockHolder(lockInfoResultSet));
                return new DatabaseChangeLogLock[]{lock};
            }
        } catch (SQLException e) {
            throw new LockException(e);
        }
    }

    /**
     * The 64-bit advisory key split into the two {@code int4} arguments {@code pg_advisory_lock}
     * takes. It is scoped exactly like the {@code DATABASECHANGELOGLOCK} table {@link
     * StandardLockService} uses: by the catalog and schema the lock table lives in (its
     * {@code liquibaseSchemaName}), not the connection's {@code current_schema()}. Keying on
     * {@code getDefaultSchemaName()} would diverge from the table's real location whenever
     * {@code liquibase.liquibaseSchemaName} is set explicitly, either over-serializing unrelated
     * deployments or, worse, losing mutual exclusion between sessions that share the table but see
     * a different {@code current_schema()}. {@link String#hashCode()} is stable across JVMs. Two
     * {@code int4} hashes can theoretically collide, but for any single deployment the
     * (catalog/schema, table-name) pair is constant, so a collision cannot affect correctness.
     */
    private int[] getChangeLogLockId() throws LockException {
        String liquibaseSchemaName = database.getLiquibaseSchemaName();
        if (liquibaseSchemaName == null) {
            throw new LockException("Liquibase schema name is not set for the current database connection");
        }
        String liquibaseCatalogName = database.getLiquibaseCatalogName();
        String schemaScope = liquibaseCatalogName == null
                ? liquibaseSchemaName
                : liquibaseCatalogName + "." + liquibaseSchemaName;
        return new int[]{
                database.getDatabaseChangeLogLockTableName().hashCode(),
                schemaScope.hashCode()
        };
    }

    private static String describeLockHolder(ResultSet lockInfoResultSet) throws SQLException {
        String clientHostname = lockInfoResultSet.getString("client_hostname");
        if (clientHostname == null) {
            return "pid#" + lockInfoResultSet.getInt("pid");
        }
        String state = lockInfoResultSet.getString("state");
        return clientHostname + " (" + (state == null ? "unknown" : state) + ")";
    }

    private static Boolean queryForBoolean(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return (Boolean) resultSet.getObject(1);
        }
    }

    private static boolean isAtLeastPostgres91(Database database) {
        try {
            int majorVersion = database.getDatabaseMajorVersion();
            return majorVersion > 9 || (majorVersion == 9 && database.getDatabaseMinorVersion() >= 1);
        } catch (DatabaseException e) {
            Scope.getCurrentScope().getLog(PostgreSQLSessionLockService.class).warning("Could not read PostgreSQL version", e);
            return false;
        }
    }
}
