package liquibase.changelog;

import liquibase.Contexts;
import liquibase.change.CheckSum;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TableBasedChangeLogHistoryService implements ChangeLogHistoryService {

    private Database database;

    private List<RanChangeSet> ranChangeSetList;

    private Integer lastChangeSetSequenceValue;

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(Database database) {
        return true;
    }

    @Override
    public void reset() {

    }

    public Database getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }

    public String getDatabaseChangeLogTableName() {
        return getDatabase().getDatabaseChangeLogTableName();
    }

    public String getLiquibaseSchemaName() {
        return database.getLiquibaseSchemaName();
    }

    public String getLiquibaseCatalogName() {
        return database.getLiquibaseCatalogName();
    }

    public boolean canCreateChangeLogTable() throws DatabaseException {
        return true;
    }

    public void resetRanChangeSetList() {
        ranChangeSetList = null;
    }

    public boolean hasDatabaseChangeLogTable() throws DatabaseException {
        try {
            return SnapshotGeneratorFactory.getInstance().hasDatabaseChangeLogTable(database);
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public void init() throws DatabaseException {
        Database database = getDatabase();
        Executor executor = ExecutorService.getInstance().getExecutor(database);

        Table changeLogTable = null;
        try {
            changeLogTable = SnapshotGeneratorFactory.getInstance().getDatabaseChangeLogTable(new SnapshotControl(database, Table.class, Column.class), database);
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        List<SqlStatement> statementsToExecute = new ArrayList<SqlStatement>();

        boolean changeLogCreateAttempted = false;
        if (changeLogTable != null) {
            boolean hasDescription = changeLogTable.getColumn("DESCRIPTION") != null;
            boolean hasComments = changeLogTable.getColumn("COMMENTS") != null;
            boolean hasTag = changeLogTable.getColumn("TAG") != null;
            boolean hasLiquibase = changeLogTable.getColumn("LIQUIBASE") != null;
            boolean liquibaseColumnNotRightSize = false;
            if (!this.database.getConnection().getDatabaseProductName().equals("SQLite")) {
                Integer columnSize = changeLogTable.getColumn("LIQUIBASE").getType().getColumnSize();
                liquibaseColumnNotRightSize = columnSize != null && columnSize != 20;
            }
            boolean hasOrderExecuted = changeLogTable.getColumn("ORDEREXECUTED") != null;
            boolean checksumNotRightSize = false;
            if (!this.database.getConnection().getDatabaseProductName().equals("SQLite")) {
                Integer columnSize = changeLogTable.getColumn("MD5SUM").getType().getColumnSize();
                checksumNotRightSize = columnSize != null && columnSize != 35;
            }
            boolean hasExecTypeColumn = changeLogTable.getColumn("EXECTYPE") != null;

            if (!hasDescription) {
                executor.comment("Adding missing databasechangelog.description column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "DESCRIPTION", "VARCHAR(255)", null));
            }
            if (!hasTag) {
                executor.comment("Adding missing databasechangelog.tag column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "TAG", "VARCHAR(255)", null));
            }
            if (!hasComments) {
                executor.comment("Adding missing databasechangelog.comments column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "COMMENTS", "VARCHAR(255)", null));
            }
            if (!hasLiquibase) {
                executor.comment("Adding missing databasechangelog.liquibase column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "LIQUIBASE", "VARCHAR(255)", null));
            }
            if (!hasOrderExecuted) {
                executor.comment("Adding missing databasechangelog.orderexecuted column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "ORDEREXECUTED", "INT", null));
                statementsToExecute.add(new UpdateStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()).addNewColumnValue("ORDEREXECUTED", -1));
                statementsToExecute.add(new SetNullableStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "ORDEREXECUTED", "INT", false));
            }
            if (checksumNotRightSize) {
                executor.comment("Modifying size of databasechangelog.md5sum column");

                statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "MD5SUM", "VARCHAR(35)"));
            }
            if (liquibaseColumnNotRightSize) {
                executor.comment("Modifying size of databasechangelog.liquibase column");

                statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "LIQUIBASE", "VARCHAR(20)"));
            }
            if (!hasExecTypeColumn) {
                executor.comment("Adding missing databasechangelog.exectype column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "EXECTYPE", "VARCHAR(10)", null));
                statementsToExecute.add(new UpdateStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()).addNewColumnValue("EXECTYPE", "EXECUTED"));
                statementsToExecute.add(new SetNullableStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "EXECTYPE", "VARCHAR(10)", false));
            }

            List<Map<String, ?>> md5sumRS = ExecutorService.getInstance().getExecutor(database).queryForList(new SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByNotNullCheckSum(), "MD5SUM"));
            if (md5sumRS.size() > 0) {
                String md5sum = md5sumRS.get(0).get("MD5SUM").toString();
                if (!md5sum.startsWith(CheckSum.getCurrentVersion() + ":")) {
                    executor.comment("DatabaseChangeLog checksums are an incompatible version.  Setting them to null so they will be updated on next database update");
                    statementsToExecute.add(new RawSqlStatement("UPDATE " + database.escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()) + " SET MD5SUM=null"));
                }
            }


        } else if (!changeLogCreateAttempted) {
            executor.comment("Create Database Change Log Table");
            SqlStatement createTableStatement = new CreateDatabaseChangeLogTableStatement();
            if (!canCreateChangeLogTable()) {
                throw new DatabaseException("Cannot create " + database.escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()) + " table for your database.\n\n" +
                        "Please construct it manually using the following SQL as a base and re-run Liquibase:\n\n" +
                        createTableStatement);
            }
            // If there is no table in the database for recording change history create one.
            statementsToExecute.add(createTableStatement);
            LogFactory.getLogger().info("Creating database history table with name: " + database.escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()));
//                }
        }

        for (SqlStatement sql : statementsToExecute) {
            if (SqlGeneratorFactory.getInstance().supports(sql, database)) {
                executor.execute(sql);
                database.commit();
            } else {
                LogFactory.getLogger().info("Cannot run "+sql.getClass().getSimpleName()+" on "+database.getShortName()+" when checking databasechangelog table");
            }
        }

    }

    public void upgradeChecksums(final DatabaseChangeLog databaseChangeLog, final Contexts contexts) throws DatabaseException {
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        for (RanChangeSet ranChangeSet : database.getRanChangeSetList()) {
            if (ranChangeSet.getLastCheckSum() == null) {
                ChangeSet changeSet = databaseChangeLog.getChangeSet(ranChangeSet);
                if (changeSet != null && new ContextChangeSetFilter(contexts).accepts(changeSet) && new DbmsChangeSetFilter(database).accepts(changeSet)) {
                    LogFactory.getLogger().debug("Updating null or out of date checksum on changeSet " + changeSet + " to correct value");
                    executor.execute(new UpdateChangeSetChecksumStatement(changeSet));
                }
            }
        }
        database.commit();
        ranChangeSetList = null;
    }

    /**
     * Returns the ChangeSets that have been run against the current database.
     */
    public List<RanChangeSet> getRanChangeSets() throws DatabaseException {
        if (this.ranChangeSetList != null) {
            return this.ranChangeSetList;
        }

        Database database = getDatabase();
        String databaseChangeLogTableName = database.escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName());
        ranChangeSetList = new ArrayList<RanChangeSet>();
        if (hasDatabaseChangeLogTable()) {
            LogFactory.getLogger().info("Reading from " + databaseChangeLogTableName);
            SqlStatement select = new SelectFromDatabaseChangeLogStatement("FILENAME", "AUTHOR", "ID", "MD5SUM", "DATEEXECUTED", "ORDEREXECUTED", "TAG", "EXECTYPE", "DESCRIPTION", "COMMENTS").setOrderBy("DATEEXECUTED ASC", "ORDEREXECUTED ASC");
            List<Map<String, ?>> results = ExecutorService.getInstance().getExecutor(database).queryForList(select);
            for (Map rs : results) {
                String fileName = rs.get("FILENAME").toString();
                String author = rs.get("AUTHOR").toString();
                String id = rs.get("ID").toString();
                String md5sum = rs.get("MD5SUM") == null ? null : rs.get("MD5SUM").toString();
                String description = rs.get("DESCRIPTION") == null ? null : rs.get("DESCRIPTION").toString();
                String comments = rs.get("COMMENTS") == null ? null : rs.get("COMMENTS").toString();
                Object tmpDateExecuted = rs.get("DATEEXECUTED");
                Date dateExecuted = null;
                if (tmpDateExecuted instanceof Date) {
                    dateExecuted = (Date) tmpDateExecuted;
                } else {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        dateExecuted = df.parse((String) tmpDateExecuted);
                    } catch (ParseException e) {
                    }
                }
                String tag = rs.get("TAG") == null ? null : rs.get("TAG").toString();
                String execType = rs.get("EXECTYPE") == null ? null : rs.get("EXECTYPE").toString();
                try {
                    RanChangeSet ranChangeSet = new RanChangeSet(fileName, id, author, CheckSum.parse(md5sum), dateExecuted, tag, ChangeSet.ExecType.valueOf(execType), description, comments);
                    ranChangeSetList.add(ranChangeSet);
                } catch (IllegalArgumentException e) {
                    LogFactory.getLogger().severe("Unknown EXECTYPE from database: " + execType);
                    throw e;
                }
            }
        }
        return ranChangeSetList;
    }

    public ChangeSet.RunStatus getRunStatus(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        if (!hasDatabaseChangeLogTable()) {
            return ChangeSet.RunStatus.NOT_RAN;
        }

        RanChangeSet foundRan = getRanChangeSet(changeSet);

        if (foundRan == null) {
            return ChangeSet.RunStatus.NOT_RAN;
        } else {
            if (foundRan.getLastCheckSum() == null) {
                try {
                    LogFactory.getLogger().info("Updating NULL md5sum for " + changeSet.toString());
                    ExecutorService.getInstance().getExecutor(getDatabase()).execute(new RawSqlStatement("UPDATE " + getDatabase().escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()) + " SET MD5SUM='" + changeSet.generateCheckSum().toString() + "' WHERE ID='" + changeSet.getId() + "' AND AUTHOR='" + changeSet.getAuthor() + "' AND FILENAME='" + changeSet.getFilePath() + "'"));

                    getDatabase().commit();
                } catch (DatabaseException e) {
                    throw new DatabaseException(e);
                }

                return ChangeSet.RunStatus.ALREADY_RAN;
            } else {
                if (foundRan.getLastCheckSum().equals(changeSet.generateCheckSum())) {
                    return ChangeSet.RunStatus.ALREADY_RAN;
                } else {
                    if (changeSet.shouldRunOnChange()) {
                        return ChangeSet.RunStatus.RUN_AGAIN;
                    } else {
                        return ChangeSet.RunStatus.INVALID_MD5SUM;
//                        throw new DatabaseHistoryException("MD5 Check for " + changeSet.toString() + " failed");
                    }
                }
            }
        }
    }

    @Override
    public RanChangeSet getRanChangeSet(final ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        if (!hasDatabaseChangeLogTable()) {
            return null;
        }

        RanChangeSet foundRan = null;
        for (RanChangeSet ranChange : getRanChangeSets()) {
            if (ranChange.isSameAs(changeSet)) {
                foundRan = ranChange;
                break;
            }
        }
        return foundRan;
    }

    @Override
    public Date getRanDate(ChangeSet changeSet) throws DatabaseException, DatabaseHistoryException {
        RanChangeSet ranChange = getRanChangeSet(changeSet);
        if (ranChange == null) {
            return null;
        } else {
            return ranChange.getDateExecuted();
        }
    }

    @Override
    public void setExecType(ChangeSet changeSet, ChangeSet.ExecType execType) throws DatabaseException {
        Database database = getDatabase();

        ExecutorService.getInstance().getExecutor(database).execute(new MarkChangeSetRanStatement(changeSet, execType));
        database.commit();
        getRanChangeSets().add(new RanChangeSet(changeSet, execType));

    }

    @Override
    public void removeFromHistory(final ChangeSet changeSet) throws DatabaseException {
        Database database = getDatabase();
        ExecutorService.getInstance().getExecutor(database).execute(new RemoveChangeSetRanStatusStatement(changeSet));
        database.commit();

        getRanChangeSets().remove(new RanChangeSet(changeSet));
    }

    @Override
    public int getNextSequenceValue() throws LiquibaseException {
        if (lastChangeSetSequenceValue == null) {
            if (getDatabase().getConnection() == null) {
                lastChangeSetSequenceValue = 0;
            } else {
                lastChangeSetSequenceValue = ExecutorService.getInstance().getExecutor(getDatabase()).queryForInt(new GetNextChangeSetSequenceValueStatement());
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
        try {
            int totalRows = ExecutorService.getInstance().getExecutor(database).queryForInt(new SelectFromDatabaseChangeLogStatement("COUNT(*)"));
            if (totalRows == 0) {
                ChangeSet emptyChangeSet = new ChangeSet(String.valueOf(new Date().getTime()), "liquibase", false, false, "liquibase-internal", null, null, database.getObjectQuotingStrategy(), null);
                this.setExecType(emptyChangeSet, ChangeSet.ExecType.EXECUTED);
            }

//            Timestamp lastExecutedDate = (Timestamp) this.getExecutor().queryForObject(createChangeToTagSQL(), Timestamp.class);
            executor.execute(new TagDatabaseStatement(tagString));
            database.commit();

            getRanChangeSets().get(getRanChangeSets().size() - 1).setTag(tagString);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean tagExists(final String tag) throws DatabaseException {
        int count = ExecutorService.getInstance().getExecutor(getDatabase()).queryForInt(new SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByTag(tag), "COUNT(*)"));
        return count > 0;
    }


}
