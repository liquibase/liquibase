package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Labels;
import liquibase.change.CheckSum;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SQLiteDatabase;
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
import liquibase.structure.core.DataType;
import liquibase.structure.core.Table;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StandardChangeLogHistoryService extends AbstractChangeLogHistoryService {

    private List<RanChangeSet> ranChangeSetList;
    private boolean serviceInitialized;
    private Boolean hasDatabaseChangeLogTable;
    private boolean databaseChecksumsCompatible = true;
    private Integer lastChangeSetSequenceValue;

    protected static final String LABELS_SIZE = "255";
    protected static final String CONTEXTS_SIZE = "255";

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

    @Override
    public void reset() {
        this.ranChangeSetList = null;
        this.serviceInitialized = false;
        this.hasDatabaseChangeLogTable = null;
    }

    public boolean hasDatabaseChangeLogTable() {
        if (hasDatabaseChangeLogTable == null) {
            try {
                hasDatabaseChangeLogTable = SnapshotGeneratorFactory.getInstance().hasDatabaseChangeLogTable
                    (getDatabase());
            } catch (LiquibaseException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }
        return hasDatabaseChangeLogTable;
    }

    protected String getCharTypeName() {
        if ((getDatabase() instanceof MSSQLDatabase) && ((MSSQLDatabase) getDatabase())
            .sendsStringParametersAsUnicode()) {
            return "nvarchar";
        }
        return "varchar";
    }

    public void init() throws DatabaseException {
        if (serviceInitialized) {
            return;
        }
        Database database = getDatabase();

        Table changeLogTable = null;
        try {
            changeLogTable = SnapshotGeneratorFactory.getInstance().getDatabaseChangeLogTable(new SnapshotControl
                (database, false, Table.class, Column.class), database);
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }

        List<SqlStatement> statementsToExecute = new ArrayList<>();

        boolean changeLogCreateAttempted = false;
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        if (changeLogTable != null) {
            boolean hasDescription = changeLogTable.getColumn("DESCRIPTION") != null;
            boolean hasComments = changeLogTable.getColumn("COMMENTS") != null;
            boolean hasTag = changeLogTable.getColumn("TAG") != null;
            boolean hasLiquibase = changeLogTable.getColumn("LIQUIBASE") != null;
            boolean hasContexts = changeLogTable.getColumn("CONTEXTS") != null;
            boolean hasLabels = changeLogTable.getColumn("LABELS") != null;
            boolean liquibaseColumnNotRightSize = false;
            if (!(this.getDatabase() instanceof SQLiteDatabase)) {
                DataType type = changeLogTable.getColumn("LIQUIBASE").getType();
                if (type.getTypeName().toLowerCase().startsWith("varchar")) {
                    Integer columnSize = type.getColumnSize();
                    liquibaseColumnNotRightSize = (columnSize != null) && (columnSize < 20);
                } else {
                    liquibaseColumnNotRightSize = false;
                }
            }
            boolean hasOrderExecuted = changeLogTable.getColumn("ORDEREXECUTED") != null;
            boolean checksumNotRightSize = false;
            if (!(this.getDatabase() instanceof SQLiteDatabase)) {
                DataType type = changeLogTable.getColumn("MD5SUM").getType();
                if (type.getTypeName().toLowerCase().startsWith("varchar")) {
                    Integer columnSize = type.getColumnSize();
                    checksumNotRightSize = (columnSize != null) && (columnSize < 35);
                } else {
                    liquibaseColumnNotRightSize = false;
                }
            }
            boolean hasExecTypeColumn = changeLogTable.getColumn("EXECTYPE") != null;
            String charTypeName = getCharTypeName();
            boolean hasDeploymentIdColumn = changeLogTable.getColumn("DEPLOYMENT_ID") != null;

            if (!hasDescription) {
                executor.comment("Adding missing databasechangelog.description column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "DESCRIPTION", charTypeName + "(255)", null));
            }
            if (!hasTag) {
                executor.comment("Adding missing databasechangelog.tag column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "TAG", charTypeName + "(255)", null));
            }
            if (!hasComments) {
                executor.comment("Adding missing databasechangelog.comments column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "COMMENTS", charTypeName + "(255)", null));
            }
            if (!hasLiquibase) {
                executor.comment("Adding missing databasechangelog.liquibase column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "LIQUIBASE", charTypeName + "(20)",
                    null));
            }
            if (!hasOrderExecuted) {
                executor.comment("Adding missing databasechangelog.orderexecuted column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "ORDEREXECUTED", "int", null));
                statementsToExecute.add(new UpdateStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName()).addNewColumnValue("ORDEREXECUTED", -1));
                statementsToExecute.add(new SetNullableStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "ORDEREXECUTED", "int", false));
            }
            if (checksumNotRightSize) {
                executor.comment("Modifying size of databasechangelog.md5sum column");

                statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "MD5SUM", charTypeName + "(35)"));
            }
            if (liquibaseColumnNotRightSize) {
                executor.comment("Modifying size of databasechangelog.liquibase column");

                statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "LIQUIBASE", charTypeName + "(20)"));
            }
            if (!hasExecTypeColumn) {
                executor.comment("Adding missing databasechangelog.exectype column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "EXECTYPE", charTypeName + "(10)",
                    null));
                statementsToExecute.add(new UpdateStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName()).addNewColumnValue("EXECTYPE", "EXECUTED"));
                statementsToExecute.add(new SetNullableStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "EXECTYPE", charTypeName + "(10)",
                    false));
            }

            if (hasContexts) {
                Integer columnSize = changeLogTable.getColumn("CONTEXTS").getType().getColumnSize();
                if ((columnSize != null) && (columnSize < Integer.parseInt(CONTEXTS_SIZE))) {
                    executor.comment("Modifying size of databasechangelog.contexts column");
                    statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseCatalogName(),
                        getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "CONTEXTS",
                        charTypeName + "("+ CONTEXTS_SIZE+")"));
                }
            } else {
                executor.comment("Adding missing databasechangelog.contexts column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "CONTEXTS", charTypeName + "("
                    + CONTEXTS_SIZE+")", null));
            }

            if (hasLabels) {
                Integer columnSize = changeLogTable.getColumn("LABELS").getType().getColumnSize();
                if ((columnSize != null) && (columnSize < Integer.parseInt(LABELS_SIZE))) {
                    executor.comment("Modifying size of databasechangelog.labels column");
                    statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseCatalogName(),
                        getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "LABELS",
                        charTypeName + "(" + LABELS_SIZE + ")"));
                }
            } else {
                executor.comment("Adding missing databasechangelog.labels column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "LABELS", charTypeName + "(" +
                    LABELS_SIZE + ")", null));
            }

            if (!hasDeploymentIdColumn) {
                executor.comment("Adding missing databasechangelog.deployment_id column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "DEPLOYMENT_ID", "VARCHAR(10)", null));
                if (database instanceof DB2Database) {
                    statementsToExecute.add(new ReorganizeTableStatement(getLiquibaseCatalogName(),
                        getLiquibaseSchemaName(), getDatabaseChangeLogTableName()));
                }
            }

            List<Map<String, ?>> md5sumRS = ExecutorService.getInstance().getExecutor(database).queryForList(new
                SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByNotNullCheckSum(),
                new ColumnConfig().setName("MD5SUM")).setLimit(1));
            if (!md5sumRS.isEmpty()) {
                String md5sum = md5sumRS.get(0).get("MD5SUM").toString();
                if (!md5sum.startsWith(CheckSum.getCurrentVersion() + ":")) {
                    executor.comment("DatabaseChangeLog checksums are an incompatible version.  Setting them to null " +
                        "so they will be updated on next database update");
                    databaseChecksumsCompatible = false;
                    statementsToExecute.add(new RawSqlStatement(
                        "UPDATE " + getDatabase().escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName()
                            , getDatabaseChangeLogTableName()) + " " +
                            "SET " +  getDatabase().escapeObjectName("MD5SUM", Column.class) + " = NULL"));
                }
            }


        } else if (!changeLogCreateAttempted) {
            executor.comment("Create Database Change Log Table");
            SqlStatement createTableStatement = new CreateDatabaseChangeLogTableStatement();
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
                LogService.getLog(getClass()).info(LogType.LOG, "Cannot run " + sql.getClass().getSimpleName() + " on" +
                    " " + getDatabase().getShortName() + " when checking databasechangelog table");
            }
        }
        serviceInitialized = true;
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
            Database database = getDatabase();
            String databaseChangeLogTableName = getDatabase().escapeTableName(getLiquibaseCatalogName(),
                getLiquibaseSchemaName(), getDatabaseChangeLogTableName());
            List<RanChangeSet> ranChangeSets = new ArrayList<>();
            if (hasDatabaseChangeLogTable()) {
                LogService.getLog(getClass()).info(LogType.LOG, "Reading from " + databaseChangeLogTableName);
                List<Map<String, ?>> results = queryDatabaseChangeLogTable(database);
                for (Map rs : results) {
                    String fileName = rs.get("FILENAME").toString();
                    String author = rs.get("AUTHOR").toString();
                    String id = rs.get("ID").toString();
                    String md5sum = ((rs.get("MD5SUM") == null) || !databaseChecksumsCompatible) ? null : rs.get
                        ("MD5SUM").toString();
                    String description = (rs.get("DESCRIPTION") == null) ? null : rs.get("DESCRIPTION").toString();
                    String comments = (rs.get("COMMENTS") == null) ? null : rs.get("COMMENTS").toString();
                    Object tmpDateExecuted = rs.get("DATEEXECUTED");
                    Date dateExecuted = null;
                    if (tmpDateExecuted instanceof Date) {
                        dateExecuted = (Date) tmpDateExecuted;
                    } else {
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            dateExecuted = df.parse((String) tmpDateExecuted);
                        } catch (ParseException e) {
                            // Ignore ParseException and assume dateExecuted == null instead of aborting.
                        }
                    }
                    String tmpOrderExecuted = rs.get("ORDEREXECUTED").toString();
                    Integer orderExecuted = ((tmpOrderExecuted == null) ? null : Integer.valueOf(tmpOrderExecuted));
                    String tag = (rs.get("TAG") == null) ? null : rs.get("TAG").toString();
                    String execType = (rs.get("EXECTYPE") == null) ? null : rs.get("EXECTYPE").toString();
                    ContextExpression contexts = new ContextExpression((String) rs.get("CONTEXTS"));
                    Labels labels = new Labels((String) rs.get("LABELS"));
                    String deploymentId = (String) rs.get("DEPLOYMENT_ID");

                    try {
                        RanChangeSet ranChangeSet = new RanChangeSet(fileName, id, author, CheckSum.parse(md5sum),
                            dateExecuted, tag, ChangeSet.ExecType.valueOf(execType), description, comments, contexts,
                            labels, deploymentId);
                        ranChangeSet.setOrderExecuted(orderExecuted);
                        ranChangeSets.add(ranChangeSet);
                    } catch (IllegalArgumentException e) {
                        LogService.getLog(getClass()).severe(LogType.LOG, "Unknown EXECTYPE from database: " +
                            execType);
                        throw e;
                    }
                }
            }

            this.ranChangeSetList = ranChangeSets;
        }
        return Collections.unmodifiableList(ranChangeSetList);
    }

    public List<Map<String, ?>> queryDatabaseChangeLogTable(Database database) throws DatabaseException {
        SelectFromDatabaseChangeLogStatement select = new SelectFromDatabaseChangeLogStatement(new ColumnConfig()
            .setName("*").setComputed(true)).setOrderBy("DATEEXECUTED ASC", "ORDEREXECUTED ASC");
        return ExecutorService.getInstance().getExecutor(database).queryForList(select);
    }

    @Override
    protected void replaceChecksum(ChangeSet changeSet) throws DatabaseException {
        ExecutorService.getInstance().getExecutor(getDatabase()).execute(new UpdateChangeSetChecksumStatement
            (changeSet));

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

        ExecutorService.getInstance().getExecutor(database).execute(new MarkChangeSetRanStatement(changeSet, execType));
        getDatabase().commit();
        if (this.ranChangeSetList != null) {
            this.ranChangeSetList.add(new RanChangeSet(changeSet, execType, null, null));
        }

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
}
