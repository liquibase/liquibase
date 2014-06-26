package liquibase.lockservice;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.exception.*;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.Row;
import liquibase.logging.LogFactory;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.Statement;
import liquibase.statement.core.*;
import liquibase.structure.core.Table;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StandardLockService implements LockService {

    private Database database;

    private boolean hasChangeLogLock = false;

    private Long changeLogLockPollRate;
    private long changeLogLocRecheckTime;

    private boolean hasDatabaseChangeLogLockTable = false;
    private boolean isDatabaseChangeLogLockTableInitialized = false;

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
        return LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getDatabaseChangeLogLockWaitTime();
    }

    @Override
    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
        this.changeLogLockPollRate = changeLogLockWaitTime;
    }

    @Override
    public void setChangeLogLockRecheckTime(long changeLogLocRecheckTime) {
        this.changeLogLocRecheckTime = changeLogLocRecheckTime;
    }

    public Long getChangeLogLockRecheckTime() {
        if (changeLogLockPollRate != null) {
            return changeLogLockPollRate;
        }
        return LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getDatabaseChangeLogLockPollRate();
    }

    @Override
    public void init() throws DatabaseException {

        try {
            boolean createdTable = false;
            Executor executor = ExecutorService.getInstance().getExecutor(database);
            if (!hasDatabaseChangeLogLockTable && !hasDatabaseChangeLogLockTable()) {

                executor.comment("Create Database Lock Table");
                executor.execute(new CreateDatabaseChangeLogLockTableStatement());
                database.commit();
                LogFactory.getLogger().debug("Created database lock table with name: " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName()));
                this.hasDatabaseChangeLogLockTable = true;
                createdTable = true;
            }

            if (!isDatabaseChangeLogLockTableInitialized(createdTable)) {
                executor.comment("Initialize Database Lock Table");
                executor.execute(new InitializeDatabaseChangeLogLockTableStatement());
                database.commit();
            }

            if (executor.updatesDatabase() && database instanceof DerbyDatabase && ((DerbyDatabase) database).supportsBooleanDataType()) { //check if the changelog table is of an old smallint vs. boolean format
                String lockTable = database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName());
                Object obj = executor.query(new RawSqlStatement("select min(locked) as test from " + lockTable + " fetch first row only")).toObject(Object.class);
                if (!(obj instanceof Boolean)) { //wrong type, need to recreate table
                    executor.execute(new DropTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName(), false));
                    executor.execute(new CreateDatabaseChangeLogLockTableStatement());
                    executor.execute(new InitializeDatabaseChangeLogLockTableStatement());
                }
            }
        } catch (UnsupportedException e) {
            throw new DatabaseException(e);
        }

    }


    public boolean isDatabaseChangeLogLockTableInitialized(final boolean tableJustCreated) throws DatabaseException {
        boolean initialized;
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        try {
            initialized = executor.query(new RawSqlStatement("select count(*) from " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName()))).toObject(0) > 0;
        } catch (LiquibaseException e) {
            if (executor.updatesDatabase()) {
                throw new UnexpectedLiquibaseException(e);
            } else {
                //probably didn't actually create the table yet.

                initialized = !tableJustCreated;
            }
        }
        return initialized;
    }

    @Override
    public boolean hasChangeLogLock() {
        return hasChangeLogLock;
    }

    public boolean hasDatabaseChangeLogLockTable() throws DatabaseException {
        boolean hasTable = false;
        try {
            hasTable = SnapshotGeneratorFactory.getInstance().hasDatabaseChangeLogLockTable(database);
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return hasTable;
    }


    @Override
    public void waitForLock() throws LockException {

        boolean locked = false;
        long timeToGiveUp = new Date().getTime() + (getChangeLogLockWaitTime() * 1000 * 60);
        while (!locked && new Date().getTime() < timeToGiveUp) {
            locked = acquireLock();
            if (!locked) {
                LogFactory.getLogger().info("Waiting for changelog lock....");
                try {
                    Thread.sleep(getChangeLogLockRecheckTime() * 1000);
                } catch (InterruptedException e) {
                    ;
                }
            }
        }

        if (!locked) {
            DatabaseChangeLogLock[] locks = listLocks();
            String lockedBy;
            if (locks.length > 0) {
                DatabaseChangeLogLock lock = locks[0];
                lockedBy = lock.getLockedBy() + " since " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(lock.getLockGranted());
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

        Executor executor = ExecutorService.getInstance().getExecutor(database);

        try {
            database.rollback();
            this.init();

            Boolean locked = (Boolean) ExecutorService.getInstance().getExecutor(database).query(new SelectFromDatabaseChangeLogLockStatement("LOCKED")).toObject(Boolean.class);

            if (locked) {
                return false;
            } else {

                executor.comment("Lock Database");
                long rowsUpdated = executor.update(new LockDatabaseChangeLogStatement()).getRowsUpdated();
                if (rowsUpdated > 1) {
                    throw new LockException("Did not update change log lock correctly");
                }
                if (rowsUpdated == 0)
                {
                    // another node was faster
                    return false;
                }
                database.commit();
                LogFactory.getLogger().info("Successfully acquired change log lock");

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
                ;
            }
        }

    }

    @Override
    public void releaseLock() throws LockException {
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        try {
            if (this.hasDatabaseChangeLogLockTable()) {
                executor.comment("Release Database Lock");
                database.rollback();
                long updatedRows = executor.update(new UnlockDatabaseChangeLogStatement()).getRowsUpdated();
                if (updatedRows != 1) {
                    throw new LockException("Did not update change log lock correctly.\n\n" + updatedRows + " rows were updated instead of the expected 1 row using executor " + executor.getClass().getName()+" there are "+executor.query(new RawSqlStatement("select count(*) from "+database.getDatabaseChangeLogLockTableName())).toObject(0)+" rows in the table");
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
                ;
            }
        }
    }

    @Override
    public DatabaseChangeLogLock[] listLocks() throws LockException {
        try {
            if (!this.hasDatabaseChangeLogLockTable()) {
                return new DatabaseChangeLogLock[0];
            }

            List<DatabaseChangeLogLock> allLocks = new ArrayList<DatabaseChangeLogLock>();
            Statement statement = new SelectFromDatabaseChangeLogLockStatement("ID", "LOCKED", "LOCKGRANTED", "LOCKEDBY");
            List<Row> rows = ExecutorService.getInstance().getExecutor(database).query(statement).toList();
            for (Row columnMap : rows) {
                Boolean locked = columnMap.get("LOCKED", Boolean.class);
                if (locked != null && locked) {
                    allLocks.add(new DatabaseChangeLogLock(columnMap.get("ID", Integer.class), columnMap.get("LOCKGRANTED", Date.class), columnMap.get("LOCKEDBY", String.class)));
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
            LogFactory.getLogger().info("Ignored exception in forceReleaseLock: " + e.getMessage());
        }*/
    }

    @Override
    public void reset() {
        hasChangeLogLock = false;
    }

    @Override
    public void destroy() throws DatabaseException {
        try {
            if (SnapshotGeneratorFactory.getInstance().has(new Table().setName(database.getDatabaseChangeLogLockTableName()).setSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()), database)) {
                ExecutorService.getInstance().getExecutor(database).execute(new DropTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName(), false));
            }
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        } catch (liquibase.exception.UnsupportedException e) {
            throw new DatabaseException(e);
        }

    }
}
