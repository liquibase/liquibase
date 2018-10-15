package liquibase.lockservice;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
 */
public class ProlongingLockService implements LockService {

    protected Database database;

    protected boolean hasChangeLogLock;

    private Long changeLogLockPollRate;
    /** Must be > {@link #changeLogLockProlongingRateInSeconds}, ideally a multiple of it. */
    // TODO BST: add validation
    private Long changeLogLockRecheckTime;
    private Long changeLogLockProlongingRateInSeconds;

    private Boolean hasDatabaseChangeLogLockTable;
    private boolean isDatabaseChangeLogLockTableInitialized;
    private ObjectQuotingStrategy quotingStrategy;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private Optional<? extends ScheduledFuture<?>> logProlonger = Optional.empty();

    public ProlongingLockService() {
    }

    @Override
    public int getPriority() {
        // which priority to use here?
        return 2;
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
        if (changeLogLockPollRate != null) {
            return changeLogLockPollRate;
        }

        // wait one day, as we are actively prolonging the wait time
        return 60 * 24L;
    }

    @Override
    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
        this.changeLogLockPollRate = changeLogLockWaitTime;
    }

    public Long getChangeLogLockRecheckTime() {
        if (changeLogLockRecheckTime != null) {
            return changeLogLockRecheckTime;
        }

        // recheck every 100 seconds
        return getChangeLogLockProlongingRateInSeconds() * 4;
    }

    @Override
    public void setChangeLogLockRecheckTime(long changeLogLockRecheckTime) {
        this.changeLogLockRecheckTime = changeLogLockRecheckTime;
    }

    public Long getChangeLogLockProlongingRateInSeconds() {
        if (changeLogLockProlongingRateInSeconds != null) {
            return changeLogLockProlongingRateInSeconds;
        }

        return 1L;
//        return 30L;
    }

    public void setChangeLogLockProlongingRateInSeconds(Long changeLogLockProlongingRateInSeconds) {
        this.changeLogLockProlongingRateInSeconds = changeLogLockProlongingRateInSeconds;
    }

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
                        database.escapeTableName(
                            database.getLiquibaseCatalogName(),
                            database.getLiquibaseSchemaName(),
                            database.getDatabaseChangeLogLockTableName()
                        )
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
            String lockTable = database.escapeTableName(
                database.getLiquibaseCatalogName(),
                database.getLiquibaseSchemaName(),
                database.getDatabaseChangeLogLockTableName()
            );
            Object obj = executor.queryForObject(
                new RawSqlStatement(
                    "SELECT MIN(locked) AS test FROM " + lockTable + " FETCH FIRST ROW ONLY"
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
        }

        // TODO BST: upgrade old tables

    }


    public boolean isDatabaseChangeLogLockTableInitialized(

        // TODO BST: check if column prolonged exists

        final boolean tableJustCreated) throws DatabaseException {
        if (!isDatabaseChangeLogLockTableInitialized) {
            Executor executor = ExecutorService.getInstance().getExecutor(database);

            try {
                isDatabaseChangeLogLockTableInitialized = executor.queryForInt(
                    new RawSqlStatement("SELECT COUNT(*) FROM " +
                        database.escapeTableName(
                            database.getLiquibaseCatalogName(),
                            database.getLiquibaseSchemaName(),
                            database.getDatabaseChangeLogLockTableName()
                        )
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
        return hasChangeLogLock;
    }

    public boolean hasDatabaseChangeLogLockTable() throws DatabaseException {

        // TODO BST: check if in new format needed here?

        if (hasDatabaseChangeLogLockTable == null) {
            try {
                hasDatabaseChangeLogLockTable = SnapshotGeneratorFactory.getInstance()
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
        if (hasChangeLogLock) {
            return true;
        }

        quotingStrategy = database.getObjectQuotingStrategy();

        Executor executor = ExecutorService.getInstance().getExecutor(database);

        try {
            database.rollback();
            this.init();

            if (logProlonger.isPresent()){
                logProlonger.get().cancel(false);
            }

            // Remove all locks that should get actively prolonged (aka, have a value in the
            // LOCKPROLONGED field) and are stale
            executor.update(new RemoveStaleLocksStatement(getChangeLogLockRecheckTime()));

            Boolean locked = executor.queryForObject(
                new SelectFromDatabaseChangeLogLockStatement("LOCKED"),
                Boolean.class
            );

            if (locked) {
                return false;
            } else {

                executor.comment("Lock Database");
                int rowsUpdated = executor.update(new LockDatabaseChangeLogStatement(true));
                if ((rowsUpdated == -1) && (database instanceof MSSQLDatabase)) {
                    LogFactory.getLogger().debug("Database did not return a proper row count (Might have " +
                            "NOCOUNT enabled)"
                    );
                    database.rollback();
                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(
                        new LockDatabaseChangeLogStatement(true), database
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

                hasChangeLogLock = true;

                database.setCanCacheLiquibaseTableInfo(true);

                logProlonger = Optional.of(
                    executorService.scheduleAtFixedRate(
                        new Runnable() {
                            @Override
                            public void run() {
                                prolongLock();
                            }
                        },
                        getChangeLogLockProlongingRateInSeconds(),
                        getChangeLogLockProlongingRateInSeconds(),
                        TimeUnit.SECONDS));

                return true;
            }
        } catch (Exception e) {

            try {
                cancelLogProlonging();

            } finally {
                // TODO BST: throw in finally
                throw new LockException(e);
            }

        } finally {
            try {
                database.rollback();
            } catch (DatabaseException e) {
            }
        }

    }

    private void prolongLock() {

        if (!hasChangeLogLock) {
            if (logProlonger.isPresent()) {
                logProlonger.get().cancel(false);
            }
            return;
        }

        Executor executor = ExecutorService.getInstance().getExecutor(database);

        try {
            database.rollback();
            this.init();

            executor.comment("Prolonging change log lock");
            executor.update(new ProlongDatabaseChangeLogLockStatement());

            database.commit();
            LogFactory.getLogger().info("Successfully prolonged change log lock");

        } catch (Exception e) {

           // TODO BST: cancel prolonging?

        } finally {
            try {
                database.rollback();
            } catch (DatabaseException e) {
            }
        }
    }

    @Override
    public void releaseLock() throws LockException {

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
                int updatedRows = executor.update(new UnlockDatabaseChangeLogStatement());
                if ((updatedRows == -1) && (database instanceof MSSQLDatabase)) {
                    LogFactory.getLogger().debug("Database did not return a proper row count (Might have " +
                            "NOCOUNT enabled.)"
                    );
                    database.rollback();
                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(
                        new UnlockDatabaseChangeLogStatement(), database
                    );
                    if (sql.length != 1) {
                        throw new UnexpectedLiquibaseException("Did not expect " + sql.length +
                            " statements");
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
                            " rows in the table"
                    );
                }
                database.commit();
            }
        } catch (Exception e) {
            throw new LockException(e);
        } finally {
            try {
                hasChangeLogLock = false;

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
        if (logProlonger.isPresent()){
            logProlonger.get().cancel(false);
        }
        logProlonger = Optional.empty();
    }

    @Override
    public DatabaseChangeLogLock[] listLocks() throws LockException {
        try {
            if (!this.hasDatabaseChangeLogLockTable()) {
                return new DatabaseChangeLogLock[0];
            }

            List<DatabaseChangeLogLock> allLocks = new ArrayList<DatabaseChangeLogLock>();
            SqlStatement sqlStatement = new SelectFromDatabaseChangeLogLockStatement(
                "ID", "LOCKED", "LOCKGRANTED", "LOCKPROLONGED", "LOCKEDBY"
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
                            (Date) columnMap.get("LOCKPROLONGED"),
                            (String) columnMap.get("LOCKEDBY")
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
        /*try {
            releaseLock();
        } catch (LockException e) {
            // ignore ?
            LogService.getLog(getClass()).info("Ignored exception in forceReleaseLock: " + e
            .getMessage());
        }*/
    }

    @Override
    public void reset() {
        hasChangeLogLock = false;
        hasDatabaseChangeLogLockTable = null;
        isDatabaseChangeLogLockTableInitialized = false;
    }

    @Override
    public void destroy() throws DatabaseException {
        // TODO BST: need to cancel prolonging here as well?
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