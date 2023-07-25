package liquibase.changelog;

import liquibase.*;
import liquibase.change.Change;
import liquibase.change.CheckSum;
import liquibase.change.ColumnConfig;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorFactory;
import liquibase.exception.DatabaseException;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.jvm.ChangelogJdbcMdcListener;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.*;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import liquibase.structure.core.Table;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Override
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
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor( "jdbc", getDatabase());
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
                if (type.getTypeName().toLowerCase().startsWith("varchar") || type.getTypeName().toLowerCase().startsWith("character varying")) {
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
                if ((columnSize != null) && (columnSize < Integer.parseInt(getContextsSize()))) {
                    executor.comment("Modifying size of databasechangelog.contexts column");
                    statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseCatalogName(),
                        getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "CONTEXTS",
                        charTypeName + "("+ getContextsSize()+")"));
                }
            } else {
                executor.comment("Adding missing databasechangelog.contexts column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "CONTEXTS", charTypeName + "("
                    + getContextsSize() + ")", null));
            }

            if (hasLabels) {
                Integer columnSize = changeLogTable.getColumn("LABELS").getType().getColumnSize();
                if ((columnSize != null) && (columnSize < Integer.parseInt(getLabelsSize()))) {
                    executor.comment("Modifying size of databasechangelog.labels column");
                    statementsToExecute.add(new ModifyDataTypeStatement(getLiquibaseCatalogName(),
                        getLiquibaseSchemaName(), getDatabaseChangeLogTableName(), "LABELS",
                        charTypeName + "(" + getLabelsSize() + ")"));
                }
            } else {
                executor.comment("Adding missing databasechangelog.labels column");
                statementsToExecute.add(new AddColumnStatement(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName(), "LABELS", charTypeName + "(" +
                    getLabelsSize() + ")", null));
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

            SqlStatement databaseChangeLogStatement = new SelectFromDatabaseChangeLogStatement(
                    new SelectFromDatabaseChangeLogStatement.ByNotNullCheckSum(),
                    new ColumnConfig().setName("MD5SUM"));
            List<Map<String, ?>> md5sumRS = ChangelogJdbcMdcListener.query(getDatabase(), ex -> ex.queryForList(databaseChangeLogStatement));

            if (!md5sumRS.isEmpty()) {
                //check if any checksum is not using the current version
                databaseChecksumsCompatible = md5sumRS.stream().allMatch(m -> m.get("MD5SUM").toString().startsWith(ChecksumVersion.latest().getVersion() + ":"));
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
            Scope.getCurrentScope().getLog(getClass()).info("Creating database history table with name: " +
                getDatabase().escapeTableName(getLiquibaseCatalogName(), getLiquibaseSchemaName(),
                    getDatabaseChangeLogTableName()));
        }

        for (SqlStatement sql : statementsToExecute) {
            if (SqlGeneratorFactory.getInstance().supports(sql, database)) {
                ChangelogJdbcMdcListener.execute(getDatabase(), ex -> ex.execute(sql));
                getDatabase().commit();
            } else {
                Scope.getCurrentScope().getLog(getClass()).info("Cannot run " + sql.getClass().getSimpleName() + " on" +
                    " " + getDatabase().getShortName() + " when checking databasechangelog table");
            }
        }

        if (!statementsToExecute.isEmpty()) {
            //reset the cache if there was a change to the table. Especially catches things like md5 changes which might have been updated but would still be wrong in the cache
            this.ranChangeSetList = null;
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
    @Override
    public List<RanChangeSet> getRanChangeSets() throws DatabaseException {
        if (this.ranChangeSetList == null) {
            Database database = getDatabase();
            String databaseChangeLogTableName = getDatabase().escapeTableName(getLiquibaseCatalogName(),
                getLiquibaseSchemaName(), getDatabaseChangeLogTableName());
            List<RanChangeSet> ranChangeSets = new ArrayList<>();
            if (hasDatabaseChangeLogTable()) {
                Scope.getCurrentScope().getLog(getClass()).info("Reading from " + databaseChangeLogTableName);
                List<Map<String, ?>> results = queryDatabaseChangeLogTable(database);
                for (Map rs : results) {
                    String storedFileName = rs.get("FILENAME").toString();
                    String fileName = DatabaseChangeLog.normalizePath(storedFileName);
                    String author = rs.get("AUTHOR").toString();
                    String id = rs.get("ID").toString();
                    String md5sum = ((rs.get("MD5SUM") == null)) ? null : rs.get("MD5SUM").toString();
                    String description = (rs.get("DESCRIPTION") == null) ? null : rs.get("DESCRIPTION").toString();
                    String comments = (rs.get("COMMENTS") == null) ? null : rs.get("COMMENTS").toString();
                    Object tmpDateExecuted = rs.get("DATEEXECUTED");
                    Date dateExecuted = null;
                    if (tmpDateExecuted instanceof Date) {
                        dateExecuted = (Date) tmpDateExecuted;
                    } else if (tmpDateExecuted instanceof LocalDateTime) {
                        dateExecuted = Date.from(((LocalDateTime) tmpDateExecuted).atZone(ZoneId.systemDefault()).toInstant());
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
                    String liquibaseVersion =  (rs.get("LIQUIBASE") == null) ? null : rs.get("LIQUIBASE").toString();

                    try {
                        RanChangeSet ranChangeSet = new RanChangeSet(fileName, id, author, CheckSum.parse(md5sum),
                            dateExecuted, tag, ChangeSet.ExecType.valueOf(execType), description, comments, contexts,
                            labels, deploymentId, storedFileName);
                        ranChangeSet.setOrderExecuted(orderExecuted);
                        ranChangeSet.setLiquibaseVersion(liquibaseVersion);
                        ranChangeSets.add(ranChangeSet);
                    } catch (IllegalArgumentException e) {
                        Scope.getCurrentScope().getLog(getClass()).severe("Unknown EXECTYPE from database: " +
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
        return ChangelogJdbcMdcListener.query(getDatabase(), executor -> executor.queryForList(select));
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
        SqlStatement markChangeSetRanStatement = new MarkChangeSetRanStatement(changeSet, execType);
        ChangelogJdbcMdcListener.execute(getDatabase(), executor -> executor.execute(markChangeSetRanStatement));
        getDatabase().commit();
        if (this.ranChangeSetList != null) {
            this.ranChangeSetList.add(new RanChangeSet(changeSet, execType, null, null));
        }

    }

    @Override
    public void removeFromHistory(final ChangeSet changeSet) throws DatabaseException {
        SqlStatement removeChangeSetRanStatusStatement = new RemoveChangeSetRanStatusStatement(changeSet);
        ChangelogJdbcMdcListener.execute(getDatabase(), executor -> executor.execute(removeChangeSetRanStatusStatement));
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
                SqlStatement nextChangeSetSequenceValueStatement = new GetNextChangeSetSequenceValueStatement();
                lastChangeSetSequenceValue = ChangelogJdbcMdcListener.query(getDatabase(), executor -> executor.queryForInt(nextChangeSetSequenceValueStatement));
            }
        }

        return ++lastChangeSetSequenceValue;
    }

    /**
     * Tags the database changelog with the given string.
     */
    @Override
    public void tag(final String tagString) throws DatabaseException {
        SqlStatement totalRowsStatement = new SelectFromDatabaseChangeLogStatement(new ColumnConfig().setName("COUNT(*)", true));
        int totalRows = ChangelogJdbcMdcListener.query(getDatabase(), executor -> executor.queryForInt(totalRowsStatement));
        if (totalRows == 0) {
            ChangeSet emptyChangeSet = new ChangeSet(String.valueOf(new Date().getTime()), "liquibase",
                false,false, "liquibase-internal", null, null,
                getDatabase().getObjectQuotingStrategy(), null);
            this.setExecType(emptyChangeSet, ChangeSet.ExecType.EXECUTED);
        }
        SqlStatement tagStatement = new TagDatabaseStatement(tagString);
        ChangelogJdbcMdcListener.execute(getDatabase(), executor -> executor.execute(tagStatement));
        getDatabase().commit();

        if (this.ranChangeSetList != null) {
            ranChangeSetList.get(ranChangeSetList.size() - 1).setTag(tagString);
        }
    }

    @Override
    public boolean tagExists(final String tag) throws DatabaseException {
        SqlStatement selectChangelogStatement = new SelectFromDatabaseChangeLogStatement(new SelectFromDatabaseChangeLogStatement.ByTag(tag),
                new ColumnConfig().setName("COUNT(*)", true));
        int count = ChangelogJdbcMdcListener.query(getDatabase(), executor -> executor.queryForInt(selectChangelogStatement));
        return count > 0;
    }

    @Override
    public void clearAllCheckSums() throws LiquibaseException {
        Database database = getDatabase();
        UpdateStatement updateStatement = new UpdateStatement(database.getLiquibaseCatalogName(), database
            .getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());
        updateStatement.addNewColumnValue("MD5SUM", null);
        ChangelogJdbcMdcListener.execute(getDatabase(), executor -> executor.execute(updateStatement));
        database.commit();
    }

    @Override
    public void destroy() throws DatabaseException {
        Database database = getDatabase();
        try {
            //
            // This code now uses the ChangeGeneratorFactory to
            // allow extension code to be called in order to
            // delete the changelog table.
            //
            // To implement the extension, you will need to override:
            // DropTableStatement
            // DropTableChange
            // DropTableGenerator
            //
            //
            DatabaseObject example =new Table().setName(database.getDatabaseChangeLogTableName
                ()).setSchema(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName());
            if (SnapshotGeneratorFactory.getInstance().has(example, database)) {
                DatabaseObject table = SnapshotGeneratorFactory.getInstance().createSnapshot(example, database);
                DiffOutputControl diffOutputControl = new DiffOutputControl(true, true, false, null);
                Change[] change = ChangeGeneratorFactory.getInstance().fixUnexpected(table, diffOutputControl,database
                    , database);
                SqlStatement[] sqlStatement = change[0].generateStatements(database);
                ChangelogJdbcMdcListener.execute(getDatabase(), executor -> executor.execute(sqlStatement[0]));
            }
            reset();
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    protected String getLabelsSize() {
        return LABELS_SIZE;
    }

    protected String getContextsSize() {
        return CONTEXTS_SIZE;
    }

    @Override
    public boolean isDatabaseChecksumsCompatible() {
        return this.databaseChecksumsCompatible;
    }
}
