package liquibase.lockservice;

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
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.structure.core.Table;

import java.text.DateFormat;
import java.util.*;

import static java.util.ResourceBundle.getBundle;

public class StandardLockService implements LockService {
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    protected Database database;

    protected boolean hasChangeLogLock;

    private Long changeLogLockPollRate;
    private Long changeLogLockRecheckTime;

    private Boolean hasDatabaseChangeLogLockTable;
    private boolean isDatabaseChangeLogLockTableInitialized;
    private ObjectQuotingStrategy quotingStrategy;


    public StandardLockService() {
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
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
        return LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class)
                .getDatabaseChangeLogLockWaitTime();
    }

    @Override
    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
        this.changeLogLockPollRate = changeLogLockWaitTime;
    }

    public Long getChangeLogLockRecheckTime() {
        if (changeLogLockRecheckTime != null) {
            return changeLogLockRecheckTime;
        }
        return LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class)
                .getDatabaseChangeLogLockPollRate();
    }

    @Override
    public void setChangeLogLockRecheckTime(long changeLogLockRecheckTime) {
        this.changeLogLockRecheckTime = changeLogLockRecheckTime;
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
                LogService.getLog(getClass()).debug(
                        LogType.LOG, "Created database lock table with name: " +
                                database.escapeTableName(
                                        database.getLiquibaseCatalogName(),
                                        database.getLiquibaseSchemaName(),
                                        database.getDatabaseChangeLogLockTableName()
                                )
                );
            } catch (DatabaseException e) {
                if ((e.getMessage() != null) && e.getMessage().contains("exists")) {
                    //hit a race condition where the table got created by another node.
                    LogService.getLog(getClass()).debug(LogType.LOG, "Database lock table already appears to exist " +
                            "due to exception: " + e.getMessage() + ". Continuing on");
                }  else {
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

    }


    public boolean isDatabaseChangeLogLockTableInitialized(final boolean tableJustCreated) throws DatabaseException {
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
                LogService.getLog(getClass()).info(LogType.LOG, "Waiting for changelog lock....");
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

            Boolean locked = ExecutorService.getInstance().getExecutor(database).queryForObject(
                    new SelectFromDatabaseChangeLogLockStatement("LOCKED"), Boolean.class
            );

            if (locked) {
                return false;
            } else {

                executor.comment("Lock Database");
                int rowsUpdated = executor.update(new LockDatabaseChangeLogStatement());
                if ((rowsUpdated == -1) && (database instanceof MSSQLDatabase)) {
                    LogService.getLog(getClass()).debug(
                            LogType.LOG, "Database did not return a proper row count (Might have NOCOUNT enabled)"
                    );
                    database.rollback();
                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(
                            new LockDatabaseChangeLogStatement(), database
                    );
                    if (sql.length != 1) {
                        throw new UnexpectedLiquibaseException("Did not expect "+sql.length+" statements");
                    }
                    rowsUpdated = executor.update(new RawSqlStatement("EXEC sp_executesql N'SET NOCOUNT OFF " +
                            sql[0].toSql().replace("'", "''") + "'"));
                }
                if (rowsUpdated > 1) {
                    throw new LockException("Did not update change log lock correctly");
                }
                if (rowsUpdated == 0)
                {
                    // another node was faster
                    return false;
                }
                database.commit();
                LogService.getLog(getClass()).info(LogType.LOG, coreBundle.getString("successfully.acquired.change.log.lock"));

                hasChangeLogLock = true;

                database.setCanCacheLiquibaseTableInfo(true);
                return true;
            }
        } catch (Exception e) {
            throw new LockException(e);
        } finally {
            try {
                database.rollback();
            } catch (DatabaseException e) {
            }
        }

    }

    @Override
    public void releaseLock() throws LockException {

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
                    LogService.getLog(getClass()).debug(
                            LogType.LOG, "Database did not return a proper row count (Might have NOCOUNT enabled.)"
                    );
                    database.rollback();
                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(
                            new UnlockDatabaseChangeLogStatement(), database
                    );
                    if (sql.length != 1) {
                        throw new UnexpectedLiquibaseException("Did not expect "+sql.length+" statements");
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
                LogService.getLog(getClass()).info(LogType.LOG, "Successfully released change log lock");
                database.rollback();
            } catch (DatabaseException e) {
            }
            if (incomingQuotingStrategy != null) {
                database.setObjectQuotingStrategy(incomingQuotingStrategy);
            }
        }
    }

    @Override
    public DatabaseChangeLogLock[] listLocks() throws LockException {
        try {
            if (!this.hasDatabaseChangeLogLockTable()) {
                return new DatabaseChangeLogLock[0];
            }

            List<DatabaseChangeLogLock> allLocks = new ArrayList<>();
            SqlStatement sqlStatement = new SelectFromDatabaseChangeLogLockStatement(
                    "ID", "LOCKED", "LOCKGRANTED", "LOCKEDBY"
            );
            List<Map<String, ?>> rows = ExecutorService.getInstance().getExecutor(database).queryForList(sqlStatement);
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
            LogService.getLog(getClass()).info("Ignored exception in forceReleaseLock: " + e.getMessage());
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