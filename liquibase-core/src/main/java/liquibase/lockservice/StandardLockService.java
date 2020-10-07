package liquibase.lockservice;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.BooleanType;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.plugin.Plugin;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.DataType;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.util.*;

public class StandardLockService extends AbstractLockService {

    protected boolean hasChangeLogLock;

    public StandardLockService() {
    }

    @Override
    public int getPriority() {
        if (LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getDatabaseChangeLogLockEnabled()) {
            return Plugin.PRIORITY_DEFAULT;
        } else {
            return Plugin.PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public boolean supports(Database database) {
        return true;
    }

    @Override
    public void init() throws DatabaseException {
        boolean createdTable = false;
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);

        ObjectQuotingStrategy originalQuotingStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(getRequiredQuotingStrategy());

        try {
            if (!hasDatabaseChangeLogLockTable()) {
                try {
                    executor.comment("Create and initialize Database Lock Table");
                    for (SqlStatement statement : getSetupStatements()) {
                        executor.execute(statement);
                    }
                    database.commit();

                    Scope.getCurrentScope().getLog(getClass()).fine(
                            "Created database lock table: " +
                                    database.escapeTableName(
                                            database.getLiquibaseCatalogName(),
                                            database.getLiquibaseSchemaName(),
                                            database.getDatabaseChangeLogLockTableName()
                                    )
                    );

                    createdTable = true;
                } catch (DatabaseException e) {
                    if ((e.getMessage() != null) && e.getMessage().contains("exists")) {
                        //hit a race condition where the table got created by another node.
                        Scope.getCurrentScope().getLog(getClass()).fine("Database lock table already appears to exist " +
                                "due to exception: " + e.getMessage() + ". Continuing on");
                    } else {
                        throw e;
                    }
                }
            }

            if (!createdTable && executor.updatesDatabase()) {
                checkChangeLogLockTableStructure(executor);
            }
        } finally {
            database.setObjectQuotingStrategy(originalQuotingStrategy);
        }

    }

    /**
     * Upgrades old table structures to match the current expectation
     */
    protected void checkChangeLogLockTableStructure(Executor executor) throws DatabaseException {
        Table changeLogTableDef = (Table) new Table()
                .setName(database.getDatabaseChangeLogLockTableName())
                .setSchema(new Schema(
                        database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()
                ));

        try {
            final Table existingTable = SnapshotGeneratorFactory.getInstance().createSnapshot(changeLogTableDef, database);
            String recreateReason = null;

            if ((database instanceof DerbyDatabase && ((DerbyDatabase) database).supportsBooleanDataType())
                    || database instanceof DB2Database && ((DB2Database) database).supportsBooleanDataType()) {

                final DataType lockedType = existingTable.getColumn("locked").getType();
                LiquibaseDataType lockedDataType = DataTypeFactory.getInstance().fromDescription(lockedType.toString(), database);

                if (!(lockedDataType instanceof BooleanType)) {
                    recreateReason = "locked column is not a boolean";
                }
            }

            if (existingTable.getColumn("heartbeat") == null) {
                recreateReason = "missing heartbeat column";
            }

            if (recreateReason != null) {
                executor.comment("Rebuilding lock table because " + recreateReason);
                executor.execute(
                        new DropTableStatement(
                                database.getLiquibaseCatalogName(),
                                database.getLiquibaseSchemaName(),
                                database.getDatabaseChangeLogLockTableName(),
                                false
                        )
                );

                for (SqlStatement statement : getSetupStatements()) {
                    executor.execute(statement);
                }
                database.commit();
            }

        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * The quoting strategy to use when accessing the databsechangelog table.
     * This implementation uses {@link ObjectQuotingStrategy#LEGACY} for historical reasons
     */
    protected ObjectQuotingStrategy getRequiredQuotingStrategy() {
        return ObjectQuotingStrategy.LEGACY;
    }


    /**
     * Checks if the changelog lock table exists
     */
    protected boolean hasDatabaseChangeLogLockTable() throws DatabaseException {
        try {
            return SnapshotGeneratorFactory.getInstance().has(
                    new Table()
                            .setName(database.getDatabaseChangeLogLockTableName())
                            .setSchema(new Schema(
                                    database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()
                            )),
                    database);
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    public boolean acquireLock() throws LockException {
        if (hasChangeLogLock) {
            return true;
        }

        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);

        ObjectQuotingStrategy originalQuotingStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(getRequiredQuotingStrategy());

        try {
            database.rollback();

            Boolean locked = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForObject(
                    new SelectFromDatabaseChangeLogLockStatement("LOCKED"), Boolean.class
            );

            if (locked) {
                return false;
            } else {
                executor.comment("Lock Database");
                int rowsUpdated = executor.update(new LockDatabaseChangeLogStatement());
                if ((rowsUpdated == -1) && (database instanceof MSSQLDatabase)) {
                    Scope.getCurrentScope().getLog(getClass()).fine(
                            "Database did not return a proper row count (Might have NOCOUNT enabled)"
                    );
                    database.rollback();
                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(
                            new LockDatabaseChangeLogStatement(), database
                    );
                    if (sql.length != 1) {
                        throw new UnexpectedLiquibaseException("Did not expect " + sql.length + " statements");
                    }
                    rowsUpdated = executor.update(new RawSqlStatement("EXEC sp_executesql N'SET NOCOUNT OFF " +
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
                Scope.getCurrentScope().getLog(getClass()).info("Successfully acquired change log lock");

                hasChangeLogLock = true;

                database.setCanCacheLiquibaseTableInfo(true);
                return true;
            }
        } catch (Exception e) {
            throw new LockException(e);
        } finally {
            database.setObjectQuotingStrategy(originalQuotingStrategy);
            try {
                database.rollback();
            } catch (DatabaseException e) {
            }
        }

    }

    @Override
    public void releaseLock() throws LockException {
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);

        ObjectQuotingStrategy originalQuotingStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(getRequiredQuotingStrategy());
        try {
            if (this.hasDatabaseChangeLogLockTable()) {
                executor.comment("Release Database Lock");
                database.rollback();
                int updatedRows = executor.update(new UnlockDatabaseChangeLogStatement());
                if ((updatedRows == -1) && (database instanceof MSSQLDatabase)) {
                    Scope.getCurrentScope().getLog(getClass()).fine(
                            "Database did not return a proper row count (Might have NOCOUNT enabled.)"
                    );
                    database.rollback();
                    Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(
                            new UnlockDatabaseChangeLogStatement(), database
                    );
                    if (sql.length != 1) {
                        throw new UnexpectedLiquibaseException("Did not expect " + sql.length + " statements");
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
                Scope.getCurrentScope().getLog(getClass()).info("Successfully released change log lock");
                database.rollback();
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).fine("Error rolling back: "+e.getMessage(), e);
            }
            database.setObjectQuotingStrategy(originalQuotingStrategy);
        }
    }

    @Override
    public DatabaseChangeLogLock[] listLocks() throws LockException {
        ObjectQuotingStrategy originalQuotingStrategy = database.getObjectQuotingStrategy();
        database.setObjectQuotingStrategy(getRequiredQuotingStrategy());

        try {
            if (!this.hasDatabaseChangeLogLockTable()) {
                return new DatabaseChangeLogLock[0];
            }

            List<DatabaseChangeLogLock> allLocks = new ArrayList<>();
            SqlStatement sqlStatement = new SelectFromDatabaseChangeLogLockStatement(
                    "ID", "LOCKED", "LOCKGRANTED", "LOCKEDBY"
            );
            List<Map<String, ?>> rows = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).queryForList(sqlStatement);
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
            return allLocks.toArray(new DatabaseChangeLogLock[0]);
        } catch (Exception e) {
            throw new LockException(e);
        } finally {
            database.setObjectQuotingStrategy(originalQuotingStrategy);
        }
    }

    @Override
    public void forceReleaseLock() throws LockException, DatabaseException {
        releaseLock();
    }

    @Override
    public void reset() {
        hasChangeLogLock = false;
    }

    @Override
    public void destroy() throws DatabaseException {
        try {
            //
            // This code now uses the ChangeGeneratorFactory to
            // allow extension code to be called in order to
            // delete the changelog lock table.
            //
            // To implement the extension, you will need to override:
            // DropTableStatement
            // DropTableChange
            // DropTableGenerator
            //
            //
            DatabaseObject example =
                    new Table().setName(database.getDatabaseChangeLogLockTableName())
                            .setSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName());
            if (SnapshotGeneratorFactory.getInstance().has(example, database)) {
                DatabaseObject table = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
                DiffOutputControl diffOutputControl = new DiffOutputControl(true, true, false, null);
                Change[] change = ChangeGeneratorFactory.getInstance().fixUnexpected(table, diffOutputControl, database, database);
                SqlStatement[] sqlStatement = change[0].generateStatements(database);
                Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database).execute(sqlStatement[0]);
            }
            reset();
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    protected List<SqlStatement> getSetupStatements() {
        List<SqlStatement> returnList = new ArrayList<>();

        String charTypeName = getCharTypeName(database);
        String dateTimeTypeString = getDateTimeTypeString(database);

        returnList.add(new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .setTablespace(database.getLiquibaseTablespaceName())
                .addPrimaryKeyColumn("ID", DataTypeFactory.getInstance().fromDescription("int", database), null, null, null, new NotNullConstraint())
                .addColumn("LOCKED", DataTypeFactory.getInstance().fromDescription("boolean", database), null, null, new NotNullConstraint())
                .addColumn("LOCKGRANTED", DataTypeFactory.getInstance().fromDescription(dateTimeTypeString, database))
                .addColumn("LOCKEDBY", DataTypeFactory.getInstance().fromDescription(charTypeName + "(255)", database)));

        returnList.add(new DeleteStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName()));
        returnList.add(new InsertStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .addColumnValue("ID", 1)
                .addColumnValue("LOCKED", Boolean.FALSE));


        return returnList;
    }

    protected String getCharTypeName(Database database) {
        if ((database instanceof MSSQLDatabase) && ((MSSQLDatabase) database).sendsStringParametersAsUnicode()) {
            return "nvarchar";
        }
        return "varchar";
    }

    protected String getDateTimeTypeString(Database database) {
        if (database instanceof MSSQLDatabase) {
            return "datetime2(3)";
        }
        return "datetime";
    }
}
