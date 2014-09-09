package liquibase.changelog;

import liquibase.Contexts;
import liquibase.ExecutionEnvironment;
import liquibase.exception.*;
import liquibase.snapshot.SnapshotFactory;
import liquibase.statement.Statement;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.LabelExpression;
import liquibase.change.CheckSum;
import liquibase.database.Database;
import liquibase.database.core.SQLiteDatabase;
import liquibase.executor.*;
import liquibase.logging.LogFactory;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.statement.core.*;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StandardChangeLogHistoryService extends AbstractChangeLogHistoryService {

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

    public String getDatabaseChangeLogTableName() {
        return getDatabase().getDatabaseChangeLogTableName();
    }

    public String getLiquibaseSchemaName() {
        return getDatabase().getLiquibaseSchemaName();
    }

    public String getLiquibaseCatalogName() {
        return getDatabase().getLiquibaseCatalogName();
    }

    public boolean canCreateChangeLogTable() throws DatabaseException {
        return true;
    }

    public void reset() {
        this.ranChangeSetList = null;
    }

    public boolean hasDatabaseChangeLogTable() throws DatabaseException {
        if (ranChangeSetList != null) {
            return true;
        }
        try {
            return SnapshotFactory.getInstance().hasDatabaseChangeLogTable(getDatabase());
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public void init() throws DatabaseException {
        Database database = getDatabase();
        Executor executor = ExecutorService.getInstance().getExecutor(database);

        Table changeLogTable = null;
        try {
            changeLogTable = SnapshotFactory.getInstance().getDatabaseChangeLogTable(new SnapshotControl(database, Table.class, Column.class), database);
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        List<Statement> statementsToExecute = new ArrayList<Statement>();

        try {
            boolean changeLogCreateAttempted = false;
            if (changeLogTable != null) {
                boolean hasDescription = changeLogTable.getColumn("DESCRIPTION") != null;
                boolean hasComments = changeLogTable.getColumn("COMMENTS") != null;
                boolean hasTag = changeLogTable.getColumn("TAG") != null;
                boolean hasLiquibase = changeLogTable.getColumn("LIQUIBASE") != null;
                boolean liquibaseColumnNotRightSize = false;
                if (!(this.getDatabase() instanceof SQLiteDatabase)) {
                    Integer columnSize = changeLogTable.getColumn("LIQUIBASE").getType().getColumnSize();
                    liquibaseColumnNotRightSize = columnSize != null && columnSize != 20;
                }
                boolean hasOrderExecuted = changeLogTable.getColumn("ORDEREXECUTED") != null;
                boolean checksumNotRightSize = false;
                if (!this.getDatabase().getConnection().getDatabaseProductName().equals("SQLite")) {
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
                    statementsToExecute.add(new UpdateDataStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()).addNewColumnValue("ORDEREXECUTED", -1));
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
                    statementsToExecute.add(new UpdateDataStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()).addNewColumnValue("EXECTYPE", "EXECUTED"));
                    statementsToExecute.add(new SetNullableStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "EXECTYPE", "VARCHAR(10)", false));
                }

                List<Row> md5sumRS = ExecutorService.getInstance().getExecutor(database).query(new SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByNotNullCheckSum(), "MD5SUM")).toList();
                if (md5sumRS.size() > 0) {
                    String md5sum = md5sumRS.get(0).get("MD5SUM", String.class);
                    if (!md5sum.startsWith(CheckSum.getCurrentVersion() + ":")) {
                        executor.comment("DatabaseChangeLog checksums are an incompatible version.  Setting them to null so they will be updated on next database update");
                        statementsToExecute.add(new RawSqlStatement("UPDATE " + getDatabase().escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()) + " SET MD5SUM=null"));
                    }
                }


            } else if (!changeLogCreateAttempted) {
                executor.comment("Create Database Change Log Table");
                Statement createTableStatement = new CreateDatabaseChangeLogTableStatement();
                if (!canCreateChangeLogTable()) {
                    throw new DatabaseException("Cannot create " + getDatabase().escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()) + " table for your getDatabase().\n\n" +
                            "Please construct it manually using the following SQL as a base and re-run Liquibase:\n\n" +
                            createTableStatement);
                }
                // If there is no table in the database for recording change history create one.
                statementsToExecute.add(createTableStatement);
                LogFactory.getLogger().info("Creating database history table with name: " + getDatabase().escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName()));
    //                }
            }

            for (Statement sql : statementsToExecute) {
                if (StatementLogicFactory.getInstance().supports(sql, new ExecutionEnvironment(database))) {
                    executor.execute(sql);
                    getDatabase().commit();
                } else {
                    LogFactory.getLogger().info("Cannot run "+sql.getClass().getSimpleName()+" on "+getDatabase().getShortName()+" when checking databasechangelog table");
                }
            }
        } catch (UnsupportedException e) {
            throw new DatabaseException(e);
        }

    }

    public void upgradeChecksums(final DatabaseChangeLog databaseChangeLog, final Contexts contexts, LabelExpression labels) throws DatabaseException {
        super.upgradeChecksums(databaseChangeLog, contexts, labels);
        getDatabase().commit();
    }

    /**
     * Returns the ChangeSets that have been run against the current getDatabase().
     */
    public List<RanChangeSet> getRanChangeSets() throws DatabaseException {
        if (this.ranChangeSetList == null) {
            Database database = getDatabase();
            String databaseChangeLogTableName = getDatabase().escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(), getDatabaseChangeLogTableName());
            List<RanChangeSet> ranChangeSetList = new ArrayList<RanChangeSet>();
            if (hasDatabaseChangeLogTable()) {
                LogFactory.getLogger().info("Reading from " + databaseChangeLogTableName);
                List<Row> results = queryDatabaseChangeLogTable(database);
                for (Row rs : results) {
                    String fileName = rs.get("FILENAME", String.class);
                    String author = rs.get("AUTHOR", String.class);
                    String id = rs.get("ID", String.class);
                    String md5sum = rs.get("MD5SUM", String.class) == null ? null : rs.get("MD5SUM", String.class);
                    String description = rs.get("DESCRIPTION", String.class) == null ? null : rs.get("DESCRIPTION", String.class);
                    String comments = rs.get("COMMENTS", String.class) == null ? null : rs.get("COMMENTS", String.class);
                    Object tmpDateExecuted = rs.get("DATEEXECUTED", String.class);
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
                    String tag = rs.get("TAG", String.class) == null ? null : rs.get("TAG", String.class);
                    String execType = rs.get("EXECTYPE", String.class) == null ? null : rs.get("EXECTYPE", String.class);
                    try {
                        RanChangeSet ranChangeSet = new RanChangeSet(fileName, id, author, CheckSum.parse(md5sum), dateExecuted, tag, ChangeSet.ExecType.valueOf(execType), description, comments);
                        ranChangeSetList.add(ranChangeSet);
                    } catch (IllegalArgumentException e) {
                        LogFactory.getLogger().severe("Unknown EXECTYPE from database: " + execType);
                        throw e;
                    }
                }
            }

            this.ranChangeSetList = ranChangeSetList;
        }
        return Collections.unmodifiableList(ranChangeSetList);
    }

    public List<Row> queryDatabaseChangeLogTable(Database database) throws DatabaseException {
        SelectFromDatabaseChangeLogStatement select = new SelectFromDatabaseChangeLogStatement("FILENAME", "AUTHOR", "ID", "MD5SUM", "DATEEXECUTED", "ORDEREXECUTED", "EXECTYPE", "DESCRIPTION", "COMMENTS", "TAG", "LIQUIBASE").setOrderBy("DATEEXECUTED ASC", "ORDEREXECUTED ASC");
        try {
            return ExecutorService.getInstance().getExecutor(database).query(select).toList();
        } catch (UnsupportedException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    protected void replaceChecksum(ChangeSet changeSet) throws DatabaseException {
        try {
            ExecutorService.getInstance().getExecutor(getDatabase()).execute(new UpdateChangeSetChecksumStatement(changeSet));
        } catch (liquibase.exception.UnsupportedException e) {
            throw new DatabaseException(e);
        }

        getDatabase().commit();
        reset();
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

        try {
            ExecutorService.getInstance().getExecutor(database).execute(new MarkChangeSetRanStatement(changeSet, execType));
        } catch (liquibase.exception.UnsupportedException e) {
            throw new DatabaseException(e);
        }
        getDatabase().commit();
        if (this.ranChangeSetList != null) {
            this.ranChangeSetList.add(new RanChangeSet(changeSet, execType));
        }

    }

    @Override
    public void removeFromHistory(final ChangeSet changeSet) throws DatabaseException {
        Database database = getDatabase();
        try {
            ExecutorService.getInstance().getExecutor(database).execute(new RemoveChangeSetRanStatusStatement(changeSet));
        } catch (liquibase.exception.UnsupportedException e) {
            throw new DatabaseException(e);
        }
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
                lastChangeSetSequenceValue = ExecutorService.getInstance().getExecutor(getDatabase()).query(new GetNextChangeSetSequenceValueStatement()).toObject(0);
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
            int totalRows = ExecutorService.getInstance().getExecutor(database).query(new SelectFromDatabaseChangeLogStatement("COUNT(*)")).toObject(0);
            if (totalRows == 0) {
                ChangeSet emptyChangeSet = new ChangeSet(String.valueOf(new Date().getTime()), "liquibase", false, false, "liquibase-internal", null, null, getDatabase().getObjectQuotingStrategy(), null);
                this.setExecType(emptyChangeSet, ChangeSet.ExecType.EXECUTED);
            }

//            Timestamp lastExecutedDate = (Timestamp) this.getExecutor().queryForObject(createChangeToTagSQL(), Timestamp.class);
            executor.execute(new TagDatabaseStatement(tagString));
            getDatabase().commit();

            if (this.ranChangeSetList != null) {
                ranChangeSetList.get(ranChangeSetList.size() - 1).setTag(tagString);
            }
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean tagExists(final String tag) throws DatabaseException {
        try {
            int count = ExecutorService.getInstance().getExecutor(getDatabase()).query(new SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByTag(tag), "COUNT(*)")).toObject(0);
            return count > 0;
        } catch (liquibase.exception.UnsupportedException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void clearAllCheckSums() throws LiquibaseException {
        Database database = getDatabase();
        UpdateDataStatement updateDataStatement = new UpdateDataStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());
        updateDataStatement.addNewColumnValue("MD5SUM", null);
        ExecutorService.getInstance().getExecutor(database).execute(updateDataStatement);
        database.commit();
    }

    @Override
    public void destroy() throws DatabaseException {
        Database database = getDatabase();
        try {
            if (SnapshotFactory.getInstance().has(new Table().setName(database.getDatabaseChangeLogTableName()).setSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName()), database)) {
                ExecutorService.getInstance().getExecutor(database).execute(new DropTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName(), false));
            }
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        } catch (liquibase.exception.UnsupportedException e) {
            throw new DatabaseException(e);
        }
    }
}
