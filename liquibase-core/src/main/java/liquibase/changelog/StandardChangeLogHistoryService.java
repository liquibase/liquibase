package liquibase.changelog;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.change.CheckSum;
import liquibase.change.ColumnConfig;
import liquibase.changelog.definition.ChangeLogTableDefinition;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import java.util.*;

public class StandardChangeLogHistoryService extends AbstractChangeLogHistoryService {

    private List<RanChangeSet> ranChangeSetList;
    private boolean serviceInitialized;
    private Boolean hasDatabaseChangeLogTable;
    private boolean databaseChecksumsCompatible = true;
    private Integer lastChangeSetSequenceValue;
    private StandardChangeLogHistorySqlStatementGenerator sqlGenerator = new StandardChangeLogHistorySqlStatementGenerator();

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(Database database) {
        return true;
    }

    public String getDatabaseChangeLogTableName() {
        return getDatabase().getDatabaseChangeLogTableName();
    }

    public String getLiquibaseSchemaName() {
        return getDatabase().getLiquibaseSchemaName();
    }

    public String getLiquibaseCatalogName() {
        return getDatabase().getLiquibaseCatalogName();
    }

    public boolean canCreateChangeLogTable() {
        return true;
    }

    private RanChangeSetService<? extends RanChangeSet> getRanChangeSetService() {
        return new RanChangeSetService<>(getStandardRanChangeSetFactory());
    }

    private RanChangeSetFactory<? extends RanChangeSet> getStandardRanChangeSetFactory() {
        return new StandardRanChangeSetFactory();
    }

    @Override
    public void reset() {
        this.ranChangeSetList = null;
        this.serviceInitialized = false;
        this.hasDatabaseChangeLogTable = null;
    }

    public void init() throws DatabaseException {
        if (serviceInitialized) {
            return;
        }

        Database database = getDatabase();

        Table changeLogTable = getChangelogTable(database);

        List<SqlStatement> statementsToExecute = new ArrayList<>();

        Executor executor = ExecutorService.getInstance().getExecutor(database);

        if (changeLogTable != null) {
            statementsToExecute.addAll(sqlGenerator.changeLogTableUpdate(database, changeLogTable, getTableDefinition()));

            databaseChecksumsCompatible = !isMd5SumIncompatible(database);

            if (!databaseChecksumsCompatible) {
                executor.comment("DatabaseChangeLog checksums are an incompatible version.  Setting them to null " +
                        "so they will be updated on next database update");
                statementsToExecute.add(new RawSqlStatement(
                        "UPDATE " + getDatabase().escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName()
                                , getDatabaseChangeLogTableName()) + " " +
                                "SET " + getDatabase().escapeObjectName("MD5SUM", Column.class) + " = NULL"));
            }

        } else {
            executor.comment("Create Database Change Log Table");
            SqlStatement createTableStatement = new CreateDatabaseChangeLogTableStatement(getChangeLogTableDefinition());
            if (!canCreateChangeLogTable()) {
                throw new DatabaseException("Cannot create " + getDatabase().escapeTableName(getLiquibaseCatalogName
                    (), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()) + " table for your getDatabase()" +
                    ".\n\n" +
                        "Please construct it manually using the following SQL as a base and re-run Liquibase:\n\n" +
                        createTableStatement);
            }
            // If there is no table in the database for recording change history create one.
            statementsToExecute.add(createTableStatement);
            LogService.getLog(getClass()).info(LogType.LOG, "Creating database history table with name: " +
                getDatabase().escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName()));
        }

        for (SqlStatement sql : statementsToExecute) {
            if (SqlGeneratorFactory.getInstance().supports(sql, database)) {
                executor.execute(sql);
                getDatabase().commit();
            } else {
                LogService.getLog(getClass()).info(LogType.LOG, "Cannot run " + sql.getClass().getSimpleName() + " on " + getDatabase().getShortName() + " when checking databasechangelog table");
            }
        }
        serviceInitialized = true;
    }

    protected ChangeLogTableDefinition getTableDefinition() {
        return new ChangeLogTableDefinition();
    }

    private boolean isMd5SumIncompatible(Database database) throws DatabaseException {
        List<Map<String, ?>> md5sumRS = ExecutorService.getInstance().getExecutor(database).queryForList(new
                SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByNotNullCheckSum(),
                new ColumnConfig().setName("MD5SUM")).setLimit(1));

        boolean isResultSetNotEmpty = !md5sumRS.isEmpty();
        boolean md5SumIncompatible = false;

        if (isResultSetNotEmpty) {
            String md5sum = md5sumRS.get(0).get("MD5SUM").toString();
            md5SumIncompatible = !md5sum.startsWith(CheckSum.getCurrentVersion() + ":");
        }
        return md5SumIncompatible;
    }

    private Table getChangelogTable(Database database) {
        Table changeLogTable;
        try {
            changeLogTable = SnapshotGeneratorFactory.getInstance().getDatabaseChangeLogTable(new SnapshotControl(database, false, Table.class, Column.class), database);
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
        return changeLogTable;
    }

    @Override
    public void upgradeChecksums(final DatabaseChangeLog databaseChangeLog, final Contexts contexts, LabelExpression
            labels) throws DatabaseException {
        super.upgradeChecksums(databaseChangeLog, contexts, labels);
        getDatabase().commit();
    }

    /**
     * Returns the ChangeSets that have been run against the current getDatabase().
     */
    public List<RanChangeSet> getRanChangeSets() throws DatabaseException {
        if (this.ranChangeSetList == null) {
            this.ranChangeSetList = new ArrayList<>();
            this.ranChangeSetList.addAll(getRanChangeSetService().prepareRanChangeSets(getDatabase()));
        }
        return Collections.unmodifiableList(ranChangeSetList);
    }

    @Override
    protected void replaceChecksum(ChangeSet changeSet) throws DatabaseException {
        ExecutorService.getInstance().getExecutor(getDatabase()).execute(new UpdateChangeSetChecksumStatement
            (changeSet));

        getDatabase().commit();
        reset();
    }

    private boolean hasDatabaseChangeLogTable() {
        if (hasDatabaseChangeLogTable == null) {
            try {
                hasDatabaseChangeLogTable = SnapshotGeneratorFactory.getInstance().hasDatabaseChangeLogTable(getDatabase());
            } catch (LiquibaseException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return hasDatabaseChangeLogTable;
    }

    @Override
    public RanChangeSet getRanChangeSet(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        if (!hasDatabaseChangeLogTable()) {
            return null;
        }

        return super.getRanChangeSet(changeSet);
    }

    @Override
    public void setExecType(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException {
        Database database = getDatabase();

        ExecutorService.getInstance().getExecutor(database).execute(getMarkChangeSetRanStatement(changeSet, execType));
        getDatabase().commit();
        if (this.ranChangeSetList != null) {
            this.ranChangeSetList.add(getRanChangeSetService().create(changeSet, execType));
        }

    }

    protected MarkChangeSetRanStatement getMarkChangeSetRanStatement(ChangeSet changeSet, ChangeSet.ExecType execType) {
        return new MarkChangeSetRanStatement(changeSet, execType);
    }

    @Override
    public void removeFromHistory(final ChangeSet changeSet) throws DatabaseException {
        Database database = getDatabase();
        ExecutorService.getInstance().getExecutor(database).execute(new RemoveChangeSetRanStatusStatement(changeSet));
        getDatabase().commit();

        if (this.ranChangeSetList != null) {
            this.ranChangeSetList.remove(new RanChangeSet(changeSet));
        }
    }

    @Override
    public int getNextSequenceValue() throws LiquibaseException {
        if (lastChangeSetSequenceValue == null) {
            if (getDatabase().getConnection() == null) {
                lastChangeSetSequenceValue = 0;
            } else {
                lastChangeSetSequenceValue = ExecutorService.getInstance().getExecutor(getDatabase()).queryForInt(new
                    GetNextChangeSetSequenceValueStatement());
            }
        }

        return ++lastChangeSetSequenceValue;
    }

    /**
     * Tags the database changelog with the given string.
     */
    @Override
    public void tag(final String tagString) throws DatabaseException {
        Database database = getDatabase();
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        int totalRows = ExecutorService.getInstance().getExecutor(database).queryForInt(new
            SelectFromDatabaseChangeLogStatement(new ColumnConfig().setName("COUNT(*)", true)));
        if (totalRows == 0) {
            ChangeSet emptyChangeSet = new ChangeSet(String.valueOf(new Date().getTime()), "liquibase",
                false,false, "liquibase-internal", null, null,
                getDatabase().getObjectQuotingStrategy(), null);
            this.setExecType(emptyChangeSet, ChangeSet.ExecType.EXECUTED);
        }

        executor.execute(new TagDatabaseStatement(tagString));
        getDatabase().commit();

        if (this.ranChangeSetList != null) {
            ranChangeSetList.get(ranChangeSetList.size() - 1).setTag(tagString);
        }
    }

    @Override
    public boolean tagExists(final String tag) throws DatabaseException {
        int count = ExecutorService.getInstance().getExecutor(getDatabase()).queryForInt(new
            SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByTag(tag), new
            ColumnConfig().setName("COUNT(*)", true)));
        return count > 0;
    }

    @Override
    public void clearAllCheckSums() throws LiquibaseException {
        Database database = getDatabase();
        UpdateStatement updateStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database
            .getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());
        updateStatement.addNewColumnValue("MD5SUM", null);
        ExecutorService.getInstance().getExecutor(database).execute(updateStatement);
        database.commit();
    }

    @Override
    public void destroy() throws DatabaseException {
        Database database = getDatabase();
        try {
            if (SnapshotGeneratorFactory.getInstance().has(new Table().setName(database.getDatabaseChangeLogTableName
                ()).setSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()), database)) {
                ExecutorService.getInstance().getExecutor(database).execute(new DropTableStatement(database
                    .getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database
                    .getDatabaseChangeLogTableName(), false));
            }
            reset();
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public boolean isDatabaseChecksumsCompatible() {
        return databaseChecksumsCompatible;
    }
}
