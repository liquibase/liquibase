package liquibase.lockservice;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.DropTableStatement;
import liquibase.statement.core.InitializeDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.ProlongDatabaseChangeLogLockStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.RemoveStaleLocksStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogLockStatement;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;
import liquibase.structure.core.Table;

/**
 * Prolongs the current lock every 30 seconds.
 * <p>
 * Locks older than 100 seconds are considered stale and will get removed.
 * <p>
 * Since locks get actively prolonged, do use a very high lock wait time (1 day).
 * <p>
 * Must <b>not</b> be used in junction with the {@link StandardLockService}!
 * <p>
 * Note: This is in alpha state. We do not use the time of the database server right now,
 * but a local date, so this might not work properly when several services with very
 * un-synchronised locks connect to the same database.
 */
public class ProlongingLockService implements LockService {

    private static final int LOW_PRIO = -1;
    private static final int PRIO_HIGHER_THAN_STANDARD_LOCK_SERVICE = 2;

    // Only prolong your own lock, hence use a unique id for each service
    // accessing the database
    private static final String LOCKED_BY_ID = UUID.randomUUID().toString();

    protected Database database;

    /**
     * This is a local timestamp. We record when our lock would be stale.
     * <p>
     * On the server, however, we use current_timestamp instead.
     * <p>
     * So these two values could be different, but as long as we compare
     * values on the server with each other, and local Dates with each other,
     * it's not a problem.
     * <p>
     * Treat this as the local, cached version of the value that is written
     * in LOCKEXPIRES in the database.
     */
    private volatile Date lockExpires = null;

    private volatile ScheduledFuture<?> logProlonger = null;

    /**
     * How long shall we wait for a lock before we give up?
     * <p>
     * Unit: minutes
     */
    private Long changeLogLockWaitTime;

    /**
     * How often shall we recheck
     * <p>
     * Should be &gt; {@link #changeLogLockProlongingRateInSeconds},
     * <p>
     * Units: seconds
     */
    private Long changeLogLockRecheckTime;

    /**
     * How often do we want to prolong the lock?
     * <p>
     * Unit: seconds
     */
    private Long changeLogLockProlongingRateInSeconds;

    /**
     * After what time shall we remove a lock?
     * <p>
     * This must be larger than {@link #changeLogLockProlongingRateInSeconds}. Ideally add 40
     * seconds to that.
     * <p>
     * If this is just a little larger than {@link #changeLogLockProlongingRateInSeconds} you
     * might end up
     * removing locks another (active) service is just about to prolong.
     * <p>
     * Unit: seconds
     */
    private Long staleChangeLogLockRemovalTimeInSeconds;

    // TODO BST: second db connection for locks
    // TODO BST: transactions per change set? rollback?

    private Boolean hasDatabaseChangeLogLockTable;
    private boolean isDatabaseChangeLogLockTableInitialized;
    private ObjectQuotingStrategy quotingStrategy;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public ProlongingLockService() {
    }

    @Override
    public int getPriority() {
        // which priority to use here?
        return LiquibaseConfiguration
            .getInstance()
            .getConfiguration(GlobalConfiguration.class)
            .getAutomaticChangeLogLockProlongingEnabled()
            ? PRIO_HIGHER_THAN_STANDARD_LOCK_SERVICE : LOW_PRIO;
    }

    @Override
    public boolean supports(Database database) {
        return true;
    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }

    public Long getChangeLogLockWaitTime() {
        if (changeLogLockWaitTime != null) {
            return changeLogLockWaitTime;
        }

        return LiquibaseConfiguration
            .getInstance()
            .getConfiguration(GlobalConfiguration.class)
            .getDatabaseChangeLogLockWaitTime();
    }

    @Override
    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
        this.changeLogLockWaitTime = changeLogLockWaitTime;
    }

    public Long getChangeLogLockRecheckTime() {
        if (changeLogLockRecheckTime != null) {
            return changeLogLockRecheckTime;
        }

        return LiquibaseConfiguration
            .getInstance()
            .getConfiguration(GlobalConfiguration.class)
            .getDatabaseChangeLogLockPollRate();
    }

    @Override
    public void setChangeLogLockRecheckTime(long changeLogLockRecheckTime) {
        this.changeLogLockRecheckTime = changeLogLockRecheckTime;
    }

    public Long getChangeLogLockProlongingRateInSeconds() {
        if (changeLogLockProlongingRateInSeconds != null) {
            return changeLogLockProlongingRateInSeconds;
        }

        return LiquibaseConfiguration
            .getInstance()
            .getConfiguration(GlobalConfiguration.class)
            .getChangeLogLockProlongingRateInSeconds();
    }

    public void setChangeLogLockProlongingRateInSeconds(long changeLogLockProlongingRateInSeconds) {
        this.changeLogLockProlongingRateInSeconds = changeLogLockProlongingRateInSeconds;
    }

    public Long getStaleChangeLogLockRemovalTimeInSeconds() {
        if (staleChangeLogLockRemovalTimeInSeconds != null) {
            return staleChangeLogLockRemovalTimeInSeconds;
        }

        return LiquibaseConfiguration
            .getInstance()
            .getConfiguration(GlobalConfiguration.class)
            .getStaleChangeLogLockRemovalTimeInSeconds();
    }

    public void setStaleChangeLogLockRemovalTimeInSeconds(
        Long staleChangeLogLockRemovalTimeInSeconds) {
        this.staleChangeLogLockRemovalTimeInSeconds = staleChangeLogLockRemovalTimeInSeconds;
    }

    /**
     * Creates / updates lock table, if not existing.
     * <p>
     * In case no lock is in there, deletes all locks (yes...) and inserts one with ID 1,
     * which is not locked.
     *
     * @throws DatabaseException
     */
    @Override
    public void init() throws DatabaseException {
        boolean createdTable = false;
        Executor executor = ExecutorService.getInstance().getExecutor(database);

        if (!hasDatabaseChangeLogLockTable()) {
            try {
                executor.comment("Create Database Lock Table");
                executor.execute(new CreateDatabaseChangeLogLockTableStatement());

                database.commit();
                LogFactory.getLogger().debug("Created database lock table with name: " +
                    databaseLockTableName()
                );
            } catch (DatabaseException e) {
                if ((e.getMessage() != null) && e.getMessage().contains("exists")) {
                    //hit a race condition where the table got created by another node.
                    LogFactory.getLogger().debug("Database lock table already appears to exist " +
                        "due to exception: " + e.getMessage() + ". Continuing on");
                } else {
                    throw e;
                }
            }
            this.hasDatabaseChangeLogLockTable = true;
            createdTable = true;
            hasDatabaseChangeLogLockTable = true;
        }

        if (!isDatabaseChangeLogLockTableInitialized(createdTable)) {
            executor.comment("Initialize Database Lock Table");
            executor.execute(new InitializeDatabaseChangeLogLockTableStatement());
            database.commit();
        }

        if (executor.updatesDatabase() && (database instanceof DerbyDatabase) && ((DerbyDatabase) database)
            .supportsBooleanDataType()) {
            //check if the changelog table is of an old smallint vs. boolean format
            Object obj = executor.queryForObject(
                new RawSqlStatement(
                    "SELECT MIN(locked) AS test FROM " + databaseLockTableName() + " FETCH FIRST " +
                        "ROW ONLY"
                ), Object.class
            );
            if (!(obj instanceof Boolean)) { //wrong type, need to recreate table
                executor.execute(
                    new DropTableStatement(
                        database.getLiquibaseCatalogName(),
                        database.getLiquibaseSchemaName(),
                        database.getDatabaseChangeLogLockTableName(),
                        false
                    )
                );
                executor.execute(new CreateDatabaseChangeLogLockTableStatement());
                executor.execute(new InitializeDatabaseChangeLogLockTableStatement());
            }

            database.commit();
        }

        checkAndUpdateLockTableWithLockExpiresAndById(executor);

    }

    /**
     * If the table does not already have the LOCKEXPIRES and LOCKEDBYID columns, upgrade.
     *
     * @param executor
     * @throws DatabaseException
     */
    private void checkAndUpdateLockTableWithLockExpiresAndById(Executor executor)
        throws DatabaseException {

        if (newColumnsExist(executor)) {
            return;
        }

        AddColumnStatement addLockExpires =
            new AddColumnStatement(database.getLiquibaseCatalogName(),
                database.getLiquibaseSchemaName(),
                database.getDatabaseChangeLogLockTableName(),
                "LOCKEXPIRES",
                getCharTypeName(database) + "(255)",
                null);

        AddColumnStatement addLockedById =
            new AddColumnStatement(database.getLiquibaseCatalogName(),
                database.getLiquibaseSchemaName(),
                database.getDatabaseChangeLogLockTableName(),
                "LOCKEDBYID",
                getCharTypeName(database) + "(36)",
                null);

        executor.execute(addLockExpires);
        executor.execute(addLockedById);

        database.commit();
    }

    private boolean newColumnsExist(Executor executor) throws DatabaseException {

        Set<String> currentColumns = executor
            .queryForList(new RawSqlStatement("SELECT * FROM " + databaseLockTableName()))
            // (table is already initialised with one lock when we arrive here)
            .get(0)
            .keySet();

        boolean hasLockExpiresColumn = false;
        boolean hasLockedByIdColumn = false;

        for (String c : currentColumns) {
            if (c.toUpperCase().equals("LOCKEXPIRES")) {
                hasLockExpiresColumn = true;
            } else if (c.toUpperCase().equals("LOCKEDBYID")) {
                hasLockedByIdColumn = true;
            }
        }

        return hasLockExpiresColumn && hasLockedByIdColumn;
    }

    private String getCharTypeName(Database database) {
        if (database instanceof MSSQLDatabase && ((MSSQLDatabase) database).sendsStringParametersAsUnicode()) {
            return "nvarchar";
        }
        return "varchar";
    }

    private String databaseLockTableName() {
        return database.escapeTableName(
            database.getLiquibaseCatalogName(),
            database.getLiquibaseSchemaName(),
            database.getDatabaseChangeLogLockTableName()
        );
    }

    public boolean isDatabaseChangeLogLockTableInitialized(
        final boolean tableJustCreated) throws DatabaseException {

        if (!isDatabaseChangeLogLockTableInitialized) {
            Executor executor = ExecutorService.getInstance().getExecutor(database);

            try {
                isDatabaseChangeLogLockTableInitialized = executor.queryForInt(
                    new RawSqlStatement("SELECT COUNT(*) FROM " +
                        databaseLockTableName()
                    )
                ) > 0;
            } catch (LiquibaseException e) {
                if (executor.updatesDatabase()) {
                    throw new UnexpectedLiquibaseException(e);
                } else {
                    //probably didn't actually create the table yet.
                    isDatabaseChangeLogLockTableInitialized = !tableJustCreated;
                }
            }
        }
        return isDatabaseChangeLogLockTableInitialized;
    }

    @Override
    public boolean hasChangeLogLock() {

        if (lockExpires == null) {
            return false;
        }

        Date now = new Date();

        // no need to check in database here, as this is already happening
        // in the lock prolonging function. It will remove our local lock
        // in case we lost it.
        return lockExpires.after(now);
    }

    public boolean hasDatabaseChangeLogLockTable() throws DatabaseException {

        if (hasDatabaseChangeLogLockTable == null) {
            try {
                hasDatabaseChangeLogLockTable = SnapshotGeneratorFactory
                    .getInstance()
                    .hasDatabaseChangeLogLockTable(database);

            } catch (LiquibaseException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }

        return hasDatabaseChangeLogLockTable;
    }


    @Override
    public void waitForLock() throws LockException {

        boolean locked = false;
        long timeToGiveUp = new Date().getTime() + (getChangeLogLockWaitTime() * 1000 * 60);
        while (!locked && (new Date().getTime() < timeToGiveUp)) {
            locked = acquireLock();
            if (!locked) {
                LogFactory.getLogger().info("Waiting for changelog lock....");
                try {
                    Thread.sleep(getChangeLogLockRecheckTime() * 1000);
                } catch (InterruptedException e) {
                    // Restore thread interrupt status
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!locked) {
            DatabaseChangeLogLock[] locks = listLocks();
            String lockedBy;
            if (locks.length > 0) {
                DatabaseChangeLogLock lock = locks[0];
                lockedBy = lock.getLockedBy() + " since " +
                    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                        .format(lock.getLockGranted());
            } else {
                lockedBy = "UNKNOWN";
            }
            throw new LockException("Could not acquire change log lock.  Currently locked by " + lockedBy);
        }
    }

    @Override
    public boolean acquireLock() throws LockException {
        if (hasChangeLogLock()) {
            return true;
        }

        quotingStrategy = database.getObjectQuotingStrategy();

        Executor executor = ExecutorService.getInstance().getExecutor(database);

        try {
            database.rollback();
            this.init();

            cancelLogProlonging();

            // Remove all locks that should get actively prolonged (aka, have a value in the
            // LOCKEXPIRES field) and are stale
            executor.comment("Attempting to remove stale locks from database");
            executor.update(new RemoveStaleLocksStatement());

            database.commit();

            Boolean locked = executor.queryForObject(
                new SelectFromDatabaseChangeLogLockStatement("LOCKED"),
                Boolean.class
            );

            if (locked) {
                return false;
            } else {

                LogFactory
                    .getLogger()
                    .info("Locking database with prolonging lock. ID of this service instance " +
                        "(LOCKED_BY_ID): " + LOCKED_BY_ID);

                executor.comment("Lock Database");

                // do not overwrite lockExpires yet, only in case its a success
                Date lockExpiresLocal = whenLockExpiresFrom(new Date());

                Date lockExpiresOnServer =
                    whenLockExpiresFrom(currentTimeOnDatabaseServer(executor));

                int rowsUpdated = executor.update(
                    LockDatabaseChangeLogStatement.forProlongingLockService(LOCKED_BY_ID,
                        lockExpiresOnServer));

                if ((rowsUpdated == -1) && (database instanceof MSSQLDatabase)) {
                    LogFactory
                        .getLogger()
                        .debug("Database did not return a proper row count (Might have " +
                            "NOCOUNT enabled)"
                        );
                    database.rollback();
                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(
                        LockDatabaseChangeLogStatement
                            .forProlongingLockService(LOCKED_BY_ID, lockExpiresOnServer),
                        database
                    );
                    if (sql.length != 1) {
                        throw new UnexpectedLiquibaseException("Did not expect " + sql.length +
                            " statements");
                    }
                    rowsUpdated = executor.update(new RawSqlStatement("EXEC sp_executesql N'SET " +
                        "NOCOUNT OFF " +
                        sql[0].toSql().replace("'", "''") + "'"));
                }
                if (rowsUpdated > 1) {
                    throw new LockException("Did not update change log lock correctly");
                }
                if (rowsUpdated == 0) {
                    // another node was faster
                    return false;
                }
                database.commit();
                LogFactory.getLogger().info("Successfully acquired change log lock");

                this.lockExpires = lockExpiresLocal;

                database.setCanCacheLiquibaseTableInfo(false);

                logProlonger = executorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            prolongLock();
                        }
                    },
                    getChangeLogLockProlongingRateInSeconds(),
                    getChangeLogLockProlongingRateInSeconds(),
                    TimeUnit.SECONDS);

                return true;
            }
        } catch (Exception e) {

            try {
                cancelLogProlonging();

            } finally {
                throw new LockException(e);
            }

        } finally {
            try {
                database.rollback();
            } catch (DatabaseException e) {
            }
        }

    }

    private Date currentTimeOnDatabaseServer(Executor executor)
        throws DatabaseException {

        return executor.queryForObject(
            new RawSqlStatement("SELECT " + database.getCurrentDateTimeFunction() +
                " FROM " + databaseLockTableName() + " WHERE ID = 1;"),
            Date.class
        );
    }

    /**
     * To the given reference point, add the time after a lock got stale.
     *
     * @param reference
     *     the time when you set a lock.
     * @return the time when the lock will expire
     */
    private Date whenLockExpiresFrom(Date reference) {

        long maxTTLMillis =
            reference.getTime() + getStaleChangeLogLockRemovalTimeInSeconds() * 1000;

        Date expirationTime = new Date(maxTTLMillis);

        return expirationTime;
    }


    private void prolongLock() {

        if (!hasChangeLogLock()) {
            cancelLogProlonging();
            return;
        }

        Executor executor = ExecutorService.getInstance().getExecutor(database);

        try {
            database.rollback();
            this.init();

            executor.comment("Prolonging change log lock");

            // do not overwrite lockExpires yet, only in case its a success
            Date lockExpiresLocal = whenLockExpiresFrom(new Date());

            Date lockExpiresOnServer =
                whenLockExpiresFrom(currentTimeOnDatabaseServer(executor));

            int rowsUpdated =
                executor.update(new ProlongDatabaseChangeLogLockStatement(LOCKED_BY_ID,
                    lockExpiresOnServer));

            if ((rowsUpdated == -1) && (database instanceof MSSQLDatabase)) {
                LogFactory
                    .getLogger()
                    .debug("Database did not return a proper row count (Might have " +
                        "NOCOUNT enabled)"
                    );
                database.rollback();
                Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(
                    new ProlongDatabaseChangeLogLockStatement(LOCKED_BY_ID, lockExpiresOnServer),
                    database
                );
                if (sql.length != 1) {
                    throw new UnexpectedLiquibaseException("Did not expect " + sql.length +
                        " statements");
                }
                rowsUpdated = executor.update(new RawSqlStatement("EXEC sp_executesql N'SET " +
                    "NOCOUNT OFF " +
                    sql[0].toSql().replace("'", "''") + "'"));
            }

            if (rowsUpdated > 1) {
                throw new LockException("Did not prolong change log lock correctly");
            }
            if (rowsUpdated == 0) {
                // we lost our lock, cancel updating
                cancelLogProlonging();

                return;
            }

            database.commit();
            lockExpires = lockExpiresLocal;
            LogFactory.getLogger().info("Successfully prolonged change log lock");

        } catch (Exception e) {

            cancelLogProlonging();

        } finally {
            try {
                database.rollback();
            } catch (DatabaseException e) {
                LogFactory
                    .getLogger()
                    .debug("Unable to roll back database", e);
            }
        }
    }

    @Override
    public void releaseLock() throws LockException {

        lockExpires = null;

        cancelLogProlonging();

        ObjectQuotingStrategy incomingQuotingStrategy = null;
        if (this.quotingStrategy != null) {
            incomingQuotingStrategy = database.getObjectQuotingStrategy();
            database.setObjectQuotingStrategy(this.quotingStrategy);
        }

        Executor executor = ExecutorService.getInstance().getExecutor(database);
        try {

            if (this.hasDatabaseChangeLogLockTable()) {
                executor.comment("Release Database Lock");
                database.rollback();
                int updatedRows =
                    executor.update(new UnlockDatabaseChangeLogStatement(LOCKED_BY_ID));
                if ((updatedRows == -1) && (database instanceof MSSQLDatabase)) {
                    LogFactory
                        .getLogger()
                        .debug("Database did not return a proper row count (Might have " +
                            "NOCOUNT enabled, or we lost the lock already.)"
                        );
                    database.rollback();
                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(
                        new UnlockDatabaseChangeLogStatement(LOCKED_BY_ID), database
                    );
                    if (sql.length != 1) {
                        throw new UnexpectedLiquibaseException("Did not expect " + sql.length +
                            " statements. If zero, could be that we lost our lock.");
                    }
                    updatedRows = executor.update(
                        new RawSqlStatement(
                            "EXEC sp_executesql N'SET NOCOUNT OFF " +
                                sql[0].toSql().replace("'", "''") + "'"
                        )
                    );
                }
                if (updatedRows != 1) {
                    throw new LockException(
                        "Did not update change log lock correctly.\n\n" +
                            updatedRows +
                            " rows were updated instead of the expected 1 row using executor " +
                            executor.getClass().getName() + "" +
                            " there are " +
                            executor.queryForInt(
                                new RawSqlStatement(
                                    "SELECT COUNT(*) FROM " +
                                        database.getDatabaseChangeLogLockTableName()
                                )
                            ) +
                            " rows in the table. If zero, we might have lost our lock."
                    );
                }
                database.commit();
            }

        } catch (Exception e) {
            throw new LockException(e);

        } finally {

            try {
                database.setCanCacheLiquibaseTableInfo(false);
                LogFactory.getLogger().info("Successfully released change log lock");
                database.rollback();
            } catch (DatabaseException e) {
            }
            if (incomingQuotingStrategy != null) {
                database.setObjectQuotingStrategy(incomingQuotingStrategy);
            }
        }
    }

    /**
     * Visible for tests
     */
    void cancelLogProlonging() {
        if (logProlonger != null) {
            logProlonger.cancel(false);
        }
        logProlonger = null;
        lockExpires = null;
    }

    @Override
    public DatabaseChangeLogLock[] listLocks() throws LockException {
        try {
            if (!this.hasDatabaseChangeLogLockTable()) {
                return new DatabaseChangeLogLock[0];
            }

            List<DatabaseChangeLogLock> allLocks = new ArrayList<DatabaseChangeLogLock>();
            SqlStatement sqlStatement = new SelectFromDatabaseChangeLogLockStatement(
                "ID", "LOCKED", "LOCKGRANTED", "LOCKEXPIRES", "LOCKEDBY", "LOCKEDBYID"
            );
            List<Map<String, ?>> rows = ExecutorService
                .getInstance()
                .getExecutor(database)
                .queryForList(sqlStatement);
            for (Map columnMap : rows) {
                Object lockedValue = columnMap.get("LOCKED");
                Boolean locked;
                if (lockedValue instanceof Number) {
                    locked = ((Number) lockedValue).intValue() == 1;
                } else {
                    locked = (Boolean) lockedValue;
                }
                if ((locked != null) && locked) {
                    allLocks.add(
                        new DatabaseChangeLogLock(
                            ((Number) columnMap.get("ID")).intValue(),
                            (Date) columnMap.get("LOCKGRANTED"),
                            (Date) columnMap.get("LOCKEXPIRES"),
                            (String) columnMap.get("LOCKEDBY"),
                            (String) columnMap.get("LOCKEDBYID")
                        )
                    );
                }
            }
            return allLocks.toArray(new DatabaseChangeLogLock[allLocks.size()]);
        } catch (Exception e) {
            throw new LockException(e);
        }
    }

    @Override
    public void forceReleaseLock() throws LockException, DatabaseException {
        this.init();
        releaseLock();
    }

    @Override
    public void reset() {
        cancelLogProlonging();
        lockExpires = null;
        hasDatabaseChangeLogLockTable = null;
        isDatabaseChangeLogLockTableInitialized = false;
    }

    @Override
    public void destroy() throws DatabaseException {

        cancelLogProlonging();

        try {
            if (SnapshotGeneratorFactory.getInstance().has(
                new Table().setName(
                    database.getDatabaseChangeLogLockTableName()
                ).setSchema(
                    database.getLiquibaseCatalogName(),
                    database.getLiquibaseSchemaName()
                ),
                database
            )) {
                ExecutorService.getInstance().getExecutor(database).execute(
                    new DropTableStatement(
                        database.getLiquibaseCatalogName(),
                        database.getLiquibaseSchemaName(),
                        database.getDatabaseChangeLogLockTableName(),
                        false
                    )
                );
                hasDatabaseChangeLogLockTable = null;
            }
            reset();
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }


}