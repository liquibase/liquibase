package liquibase.changelog;

import liquibase.ChecksumVersion;
import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.change.*;
import liquibase.change.core.EmptyChange;
import liquibase.change.core.RawSQLChange;
import liquibase.change.core.SQLFileChange;
import liquibase.change.visitor.ChangeVisitor;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.*;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.logging.Logger;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.customobjects.RollbackSqlFile;
import liquibase.parser.ChangeLogParserConfiguration;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.Conditional;
import liquibase.precondition.ErrorPrecondition;
import liquibase.precondition.FailedPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sql.visitor.SqlVisitorFactory;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.util.SqlUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Encapsulates a changeSet and all its associated changes.
 */
public class ChangeSet implements Conditional, ChangeLogChild {

    protected CheckSum checkSum;
    /**
     * storedChecksum is used to make the checksum of a changeset that has already been run
     * on a database available to liquibase extensions. This value might differ from the checkSum value that
     * is calculated at run time when ValidatorVisitor is being called
     */
    private CheckSum storedCheckSum;

    private static final String AND = " AND ";
    private static final String COMMA = ",";
    private static final String WHITESPACE = " ";
    private static final String OPEN_BRACKET = "(";
    private static final String CLOSE_BRACKET = ")";

    public enum RunStatus {
        NOT_RAN, ALREADY_RAN, RUN_AGAIN, MARK_RAN, INVALID_MD5SUM
    }

    public enum ExecType {
        EXECUTED("EXECUTED", false, true),
        FAILED("FAILED", false, false),
        SKIPPED("SKIPPED", false, false),
        RERAN("RERAN", true, true),
        MARK_RAN("MARK_RAN", false, true);

        ExecType(String value, boolean ranBefore, boolean ran) {
            this.value = value;
            this.ranBefore = ranBefore;
            this.ran = ran;
        }

        public final String value;
        public final boolean ranBefore;
        public final boolean ran;
    }

    public enum ValidationFailOption {
        HALT("HALT"),
        MARK_RAN("MARK_RAN");

        final String key;

        ValidationFailOption(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    protected String key;

    private ChangeLogParameters changeLogParameters;

    /**
     * List of change objects defined in this changeset
     */
    private final List<Change> changes;

    /**
     * "id" specified in changeLog file.  Combination of id+author+filePath must be unique
     */
    private String id;

    /**
     * "author" defined in changeLog file.  Having each developer use a unique author tag allows duplicates of "id" attributes between developers.
     */
    private String author;

    /**
     * File changeSet is defined in.  May be a logical/non-physical string.  It is included in the unique identifier to allow duplicate id+author combinations in different files
     */
    private String filePath = "UNKNOWN CHANGE LOG";


    /**
     * A logicalFilePath if defined
     */
    private String logicalFilePath;

    /**
     * File path stored in the databasechangelog table. It should be the same as filePath, but not always.
     */
    private String storedFilePath;

    /**
     * If set to true, the changeSet will be executed on every update. Defaults to false
     */
    private boolean alwaysRun;

    /**
     * If set to true, the changeSet will be executed when the checksum changes.  Defaults to false.
     */
    private boolean runOnChange;

    /**
     * Runtime contexts in which the changeSet will be executed.  If null or empty, will execute regardless of contexts set
     */
    private ContextExpression contextFilter;

    /**
     * "Labels" associated with this changeSet.  If null or empty, will execute regardless of labels set
     */
    private Labels labels;

    /**
     * If set to true, the changeSet will be ignored (skipped)
     */
    private boolean ignore;

    /**
     * Databases for which this changeset should run.  The string values should match the value returned from Database.getShortName()
     */
    private Set<String> dbmsSet;

    /**
     * The original string used in the dbms attribute.
     */
    @Getter
    private String dbmsOriginalString;

    /**
     * If false, do not stop liquibase update execution if an error is thrown executing the changeSet.  Defaults to true
     */
    private Boolean failOnError;

    /**
     * List of checksums that are assumed to be valid besides the one stored in the database.  Can include the string "any"
     */
    private final Set<CheckSum> validCheckSums = new HashSet<>();

    /**
     * If true, the changeSet will run in a database transaction.  Defaults to true
     */
    private boolean runInTransaction;

    /**
     * Behavior if the validation of any of the changeSet changes fails.  Does not include checksum validation
     */
    private ValidationFailOption onValidationFail = ValidationFailOption.HALT;

    /**
     * Stores if validation failed on this ChangeSet
     */
    private boolean validationFailed;

    /**
     * Changes defined to roll back this changeSet
     */
    private final RollbackContainer rollback = new RollbackContainer();


    /**
     * ChangeSet comments defined in changeLog file
     */
    private String comments;

    /**
     * ChangeSet level preconditions defined for this changeSet
     */
    private PreconditionContainer preconditions;

    /**
     * ChangeSet level attribute to specify an Executor
     */
    private String runWith;

    /**
     * ChangeSet level attribute to specify a spool file name
     */
    private String runWithSpoolFile;

    /**
     * SqlVisitors defined for this changeset.
     * SqlVisitors will modify the SQL generated by the changes before sending it to the database.
     */
    private final List<SqlVisitor> sqlVisitors = new ArrayList<>();

    @Getter
    private ObjectQuotingStrategy objectQuotingStrategy;

    private final DatabaseChangeLog changeLog;

    @Getter
    @Setter
    private String created;

    /**
     * Allow changeSet to be run "first" or "last". Multiple changeSets with the same runOrder will preserve their order relative to each other.
     */
    @Getter
    private String runOrder;

    private final Map<String, Object> attributes = new HashMap<>();

    /**
     * Deployment ID stored in the databasechangelog table.
     */
    private String deploymentId;

    @Getter
    @Setter
    private List<String> generatedSql = new ArrayList<>();

    @Getter
    @Setter
    private ExecType execType;

    @Getter
    @Setter
    private String errorMsg;

    @Getter
    @Setter
    private ExecType rollbackExecType;

    @Getter
    @Setter
    private Date operationStartTime;

    @Getter
    @Setter
    private Date operationStopTime;

    public boolean shouldAlwaysRun() {
        return alwaysRun;
    }

    public boolean shouldRunOnChange() {
        return runOnChange;
    }

    public ChangeSet(DatabaseChangeLog databaseChangeLog) {
        this.changes = new ArrayList<>();
        this.changeLog = databaseChangeLog;
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, String filePath, String contextFilter, String dbmsList, DatabaseChangeLog databaseChangeLog) {
        this(id, author, alwaysRun, runOnChange, filePath, contextFilter, dbmsList, null, null, true, ObjectQuotingStrategy.LEGACY, databaseChangeLog);
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, String filePath, String contextFilter, String dbmsList, boolean runInTransaction, DatabaseChangeLog databaseChangeLog) {
        this(id, author, alwaysRun, runOnChange, filePath, contextFilter, dbmsList, null, null, runInTransaction, ObjectQuotingStrategy.LEGACY, databaseChangeLog);
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, String filePath, String contextFilter, String dbmsList, ObjectQuotingStrategy quotingStrategy, DatabaseChangeLog databaseChangeLog) {
        this(id, author, alwaysRun, runOnChange, filePath, contextFilter, dbmsList, null, null, true, quotingStrategy, databaseChangeLog);
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, String filePath, String contextFilter, String dbmsList,
                     boolean runInTransaction, ObjectQuotingStrategy quotingStrategy, DatabaseChangeLog databaseChangeLog) {
        this(id, author, alwaysRun, runOnChange, filePath, contextFilter, dbmsList, null, null, runInTransaction, quotingStrategy, databaseChangeLog);
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, String filePath, String contextFilter, String dbmsList,
                     String runWith, String runWithSpoolFile, boolean runInTransaction, ObjectQuotingStrategy quotingStrategy, DatabaseChangeLog databaseChangeLog) {
        this(databaseChangeLog);
        this.id = id;
        this.author = author;
        this.filePath = filePath;
        this.alwaysRun = alwaysRun;
        this.runOnChange = runOnChange;
        this.runInTransaction = runInTransaction;
        this.objectQuotingStrategy = quotingStrategy;
        this.contextFilter = new ContextExpression(contextFilter);
        setDbms(dbmsList);
        this.runWith = runWith;
        this.runWithSpoolFile = runWithSpoolFile;
    }

    protected void setDbms(String dbmsList) {
        this.dbmsSet = DatabaseList.toDbmsSet(dbmsList);
        this.dbmsOriginalString = dbmsList;
    }

    /**
     * @return either this object's logicalFilePath or the changelog's filepath (logical or physical) if not.
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Returns the logical file path defined directly on this node. Returns {@code null} if not set.
     *
     * @return the logical file path defined on this node, or {@code null} if not set
     */
    public String getLogicalFilePath() {
        return logicalFilePath;
    }

    public void setLogicalFilePath(String logicalFilePath) {
        this.logicalFilePath = logicalFilePath;
    }

    public String getStoredFilePath() {
        if (storedFilePath == null) {
            return getFilePath();
        }
        return storedFilePath;
    }

    public void setStoredFilePath(String storedFilePath) {
        this.storedFilePath = storedFilePath;
    }

    /**
     * @return the runWith value. If the runWith value is empty or not set this method will return null.
     */
    public String getRunWith() {
        return runWith == null || runWith.isEmpty() ? null : runWith;
    }

    public void setRunWith(String runWith) {
        this.runWith = runWith;
    }

    public String getRunWithSpoolFile() {
        return runWithSpoolFile;
    }

    public void setRunWithSpoolFile(String runWithSpoolFile) {
        this.runWithSpoolFile = runWithSpoolFile;
    }

    public void clearCheckSum() {
        this.checkSum = null;
    }

    public CheckSum generateCheckSum(ChecksumVersion version) {
        try {
            return Scope.child(Collections.singletonMap(Scope.Attr.checksumVersion.name(), version), () -> {
                if (checkSum == null) {
                    StringBuilder stringToMD5 = new StringBuilder();
                    for (Change change : this.getChanges()) {
                        // checksum v8 requires changes that are applied even to other databases to be calculated
                        // checksum v9 excludes them from calculation
                        if (!(change instanceof DbmsTargetedChange) ||
                                Scope.getCurrentScope().getChecksumVersion().lowerOrEqualThan(ChecksumVersion.V8) ||
                                DatabaseList.definitionMatches(((DbmsTargetedChange) change).getDbms(), Scope.getCurrentScope().getDatabase(), true)) {
                            stringToMD5.append(change.generateCheckSum()).append(":");
                        }
                    }

                    for (SqlVisitor visitor : this.getSqlVisitors()) {
                        stringToMD5.append(visitor.generateCheckSum()).append(";");
                    }
                    checkSum = CheckSum.compute(stringToMD5.toString());
                }

                return checkSum;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void load(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        this.id = node.getChildValue(null, "id", String.class);
        this.author = node.getChildValue(null, "author", String.class);
        this.alwaysRun = node.getChildValue(null, "runAlways", node.getChildValue(null, "alwaysRun", false));
        this.runOnChange = node.getChildValue(null, "runOnChange", false);
        this.runWith = node.getChildValue(null, "runWith", String.class);
        this.runWithSpoolFile = node.getChildValue(null, "runWithSpoolFile", String.class);
        this.contextFilter = new ContextExpression(node.getChildValue(null, "contextFilter", String.class));
        if (this.contextFilter.isEmpty()) {
            contextFilter = new ContextExpression(node.getChildValue(null, "context", String.class));
        }
        this.labels = new Labels(StringUtil.trimToNull(node.getChildValue(null, "labels", String.class)));
        setDbms(node.getChildValue(null, "dbms", String.class));
        this.runInTransaction = node.getChildValue(null, "runInTransaction", true);
        this.created = node.getChildValue(null, "created", String.class);
        this.runOrder = node.getChildValue(null, "runOrder", String.class);
        this.ignore = node.getChildValue(null, "ignore", false);
        this.comments = StringUtil.join(node.getChildren(null, "comment"), "\n", obj -> {
            if (((ParsedNode) obj).getValue() == null) {
                return "";
            } else {
                return ((ParsedNode) obj).getValue().toString();
            }
        });
        this.comments = StringUtil.trimToNull(this.comments);

        String objectQuotingStrategyString = StringUtil.trimToNull(node.getChildValue(null, "objectQuotingStrategy", String.class));
        if (changeLog != null) {
            this.objectQuotingStrategy = changeLog.getObjectQuotingStrategy();
        }
        if (objectQuotingStrategyString != null) {
            this.objectQuotingStrategy = ObjectQuotingStrategy.valueOf(objectQuotingStrategyString);
        }

        if (this.objectQuotingStrategy == null) {
            this.objectQuotingStrategy = ObjectQuotingStrategy.LEGACY;
        }

        this.logicalFilePath = StringUtil.trimToNull(node.getChildValue(null, "logicalFilePath", String.class));

        this.filePath = logicalFilePath;
        if (filePath == null) {
            if (changeLog != null) {
                filePath = changeLog.getFilePath();
            }
        } else {
            filePath = filePath.replaceAll("\\\\", "/")
                    .replaceFirst("^/", "");

        }

        this.setFailOnError(node.getChildValue(null, "failOnError", Boolean.class));
        String onValidationFailString = node.getChildValue(null, "onValidationFail", "HALT");
        this.setOnValidationFail(ValidationFailOption.valueOf(onValidationFailString));

        for (ParsedNode child : node.getChildren()) {
            handleChildNode(child, resourceAccessor);
        }
    }

    protected void handleChildNode(ParsedNode child, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        switch (child.getName()) {
            case "rollback":
                handleRollbackNode(child, resourceAccessor);
                break;
            case "validCheckSum":
            case "validCheckSums":
                if (child.getValue() == null) {
                    return;
                }

                if (child.getValue() instanceof Collection) {
                    for (Object checksum : (Collection) child.getValue()) {
                        addValidCheckSum((String) checksum);
                    }
                } else {
                    addValidCheckSum(child.getValue(String.class));
                }
                break;
            case "modifySql":
                String dbmsString = StringUtil.trimToNull(child.getChildValue(null, "dbms", String.class));
                String contextFilterString = StringUtil.trimToNull(child.getChildValue(null, "contextFilter", String.class));
                if (contextFilterString == null) {
                    contextFilterString = StringUtil.trimToNull(child.getChildValue(null, "context", String.class));
                }

                String labelsString = StringUtil.trimToNull(child.getChildValue(null, "labels", String.class));
                boolean applyToRollback = child.getChildValue(null, "applyToRollback", false);

                Set<String> dbms = new HashSet<>();
                if (dbmsString != null) {
                    dbms.addAll(StringUtil.splitAndTrim(dbmsString, ","));
                }
                ContextExpression contextFilter = null;
                if (contextFilterString != null) {
                    contextFilter = new ContextExpression(contextFilterString);
                }

                Labels labels = null;
                if (labelsString != null) {
                    labels = new Labels(labelsString);
                }


                List<ParsedNode> potentialVisitors = child.getChildren();
                for (ParsedNode node : potentialVisitors) {
                    SqlVisitor sqlVisitor = SqlVisitorFactory.getInstance().create(node.getName());
                    if (sqlVisitor != null) {
                        sqlVisitor.setApplyToRollback(applyToRollback);
                        if (!dbms.isEmpty()) {
                            sqlVisitor.setApplicableDbms(dbms);
                        }
                        sqlVisitor.setContextFilter(contextFilter);
                        sqlVisitor.setLabels(labels);
                        sqlVisitor.load(node, resourceAccessor);

                        addSqlVisitor(sqlVisitor);
                    }
                }


                break;
            case "preConditions":
                this.preconditions = new PreconditionContainer();
                this.preconditions.load(child, resourceAccessor);
                break;
            case "changes":
                for (ParsedNode changeNode : child.getChildren()) {
                    handleChildNode(changeNode, resourceAccessor);
                }
                break;
            default:
                Change change = toChange(child, resourceAccessor);
                if ((change == null) && (child.getValue() instanceof String)) {
                    this.setAttribute(child.getName(), child.getValue());
                } else {
                    addChange(change);
                }
                break;
        }
    }

    protected void handleRollbackNode(ParsedNode rollbackNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        String changeSetId = rollbackNode.getChildValue(null, "changeSetId", String.class);
        if (changeSetId != null) {
            String changeSetAuthor = rollbackNode.getChildValue(null, "changeSetAuthor", String.class);
            String changeSetPath = rollbackNode.getChildValue(null, "changeSetPath", getFilePath());

            DatabaseChangeLog changeLog = this.getChangeLog();
	        List<ChangeSet> changeSets = changeLog.getChangeSets(changeSetPath, changeSetAuthor, changeSetId);
	        while (changeSets.isEmpty() && (changeLog != null)) {
		        changeLog = changeLog.getParentChangeLog();
		        if (changeLog != null) {
			        changeSets = changeLog.getChangeSets(changeSetPath, changeSetAuthor, changeSetId);
		        }
	        }
            if (changeSets.isEmpty()) {
                throw new ParsedNodeException("Change set " + new ChangeSet(changeSetId, changeSetAuthor, false, false, changeSetPath, null, null, null).toString(false) + " does not exist");
            }
	        for (ChangeSet changeSet : changeSets) {
		        for (Change change : changeSet.getChanges()) {
			        rollback.getChanges().add(change);
		        }
	        }
            return;
        }

        boolean foundValue = false;
        for (ParsedNode childNode : rollbackNode.getChildren()) {
            Change rollbackChange = toChange(childNode, resourceAccessor);
            if (rollbackChange != null) {
                addRollbackChange(rollbackChange);
                foundValue = true;
            }
        }

        Object value = rollbackNode.getValue();
        if (value != null) {
            if (value instanceof String) {
                String finalValue = StringUtil.trimToNull((String) value);
                if (finalValue != null) {
                    String[] strings = StringUtil.processMultiLineSQL(finalValue, true, true, ";", this);
                    for (String string : strings) {
                        addRollbackChange(new RawSQLChange(string));
                        foundValue = true;
                    }
                }
            } else {
                throw new ParsedNodeException("Unexpected object: " + value.getClass().getName() + " '" + value + "'");
            }
        }
        if (!foundValue) {
            addRollbackChange(new EmptyChange());
        }
    }

    protected Change toChange(ParsedNode value, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        Change change = Scope.getCurrentScope().getSingleton(ChangeFactory.class).create(value.getName());
        if (change == null) {
            if (value.getChildren().size() > 0 && ChangeLogParserConfiguration.CHANGELOG_PARSE_MODE.getCurrentValue().equals(ChangeLogParserConfiguration.ChangelogParseMode.STRICT)) {
                String message = "";
                if (this.getChangeLog() != null && this.getChangeLog().getPhysicalFilePath() != null) {
                    message = "Error parsing " + this.getChangeLog().getPhysicalFilePath() + ": ";
                }
                message += "Unknown change type '" + value.getName() + "'. Check for spelling or capitalization errors and missing extensions such as liquibase-commercial.";
                throw new ParsedNodeException(message);
            }
            return null;
        } else {
            change.load(value, resourceAccessor);
            for(ChangeVisitor changeVisitor : getChangeVisitors()){
                change.modify(changeVisitor);
            }
            return change;
        }
    }

    @Override
    public ParsedNode serialize() {
        throw new RuntimeException("TODO");
    }


    public ExecType execute(DatabaseChangeLog databaseChangeLog, Database database) throws MigrationFailedException {
        return execute(databaseChangeLog, null, database);
    }

    /**
     * This method will actually execute each of the changes in the list against the
     * specified database.
     *
     * @return should changeset be marked as ran
     */
    public ExecType execute(DatabaseChangeLog databaseChangeLog, ChangeExecListener listener, Database database)
            throws MigrationFailedException {
        Logger log = Scope.getCurrentScope().getLog(getClass());
        addChangeSetMdcProperties();
        Boolean failOnError = getFailOnError();
        if (failOnError != null) {
            Scope.getCurrentScope().addMdcValue(MdcKey.FAIL_ON_ERROR, String.valueOf(failOnError));
        }
        if (validationFailed) {
            return ExecType.MARK_RAN;
        }

        operationStartTime = new Date();
        long startTime = operationStartTime.getTime();
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OPERATION_START_TIME, Instant.ofEpochMilli(startTime).toString());

        boolean skipChange = false;

        Executor originalExecutor = setupCustomExecutorIfNecessary(database);
        try {
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            // set object quoting strategy
            database.setObjectQuotingStrategy(objectQuotingStrategy);

            if (database.supportsDDLInTransaction()) {
                database.setAutoCommit(!runInTransaction);
            }

            executor.modifyChangeSet(this);

            executor.comment("Changeset " + toString(false));
            if (StringUtil.trimToNull(getComments()) != null) {
                String comments = getComments();
                String[] lines = comments.split("\\n");
                for (int i = 0; i < lines.length; i++) {
                    if (i > 0) {
                        lines[i] = database.getLineComment() + " " + lines[i];
                    }
                }
                executor.comment(StringUtil.join(Arrays.asList(lines), "\n"));
            }

            try {
                if (preconditions != null) {
                    preconditions.check(database, databaseChangeLog, this, listener);
                }
            } catch (PreconditionFailedException e) {
                if (listener != null) {
                    listener.preconditionFailed(e, preconditions.getOnFail());
                }
                StringBuilder message = new StringBuilder();
                message.append(StreamUtil.getLineSeparator());
                for (FailedPrecondition invalid : e.getFailedPreconditions()) {
                    message.append("          ").append(invalid.toString());
                    message.append(StreamUtil.getLineSeparator());
                }

                if (preconditions.getOnFail().equals(PreconditionContainer.FailOption.HALT)) {
                    throw new MigrationFailedException(this, message.toString(), e);
                } else if (preconditions.getOnFail().equals(PreconditionContainer.FailOption.CONTINUE)) {
                    skipChange = true;
                    execType = ExecType.SKIPPED;

                    Scope.getCurrentScope().getLog(getClass()).info("Continuing past: " + this + " despite precondition failure due to onFail='CONTINUE': " + message);
                } else if (preconditions.getOnFail().equals(PreconditionContainer.FailOption.MARK_RAN)) {
                    execType = ExecType.MARK_RAN;
                    skipChange = true;

                    log.info("Marking ChangeSet: \"" + this + "\" as ran despite precondition failure due to onFail='MARK_RAN': " + message);
                } else if (preconditions.getOnFail().equals(PreconditionContainer.FailOption.WARN)) {
                    execType = null; //already warned
                } else {
                    throw new UnexpectedLiquibaseException("Unexpected precondition onFail attribute: " + preconditions.getOnFail(), e);
                }
            } catch (PreconditionErrorException e) {
                if (listener != null) {
                    listener.preconditionErrored(e, preconditions.getOnError());
                }

                StringBuilder message = new StringBuilder();
                message.append(StreamUtil.getLineSeparator());
                for (ErrorPrecondition invalid : e.getErrorPreconditions()) {
                    message.append("          ").append(invalid.toString());
                    message.append(StreamUtil.getLineSeparator());
                }

                if (preconditions.getOnError().equals(PreconditionContainer.ErrorOption.HALT)) {
                    throw new MigrationFailedException(this, message.toString(), e);
                } else if (preconditions.getOnError().equals(PreconditionContainer.ErrorOption.CONTINUE)) {
                    skipChange = true;
                    execType = ExecType.SKIPPED;

                } else if (preconditions.getOnError().equals(PreconditionContainer.ErrorOption.MARK_RAN)) {
                    execType = ExecType.MARK_RAN;
                    skipChange = true;

                    log.info("Marking ChangeSet: " + this + " ran despite precondition error: " + message);
                } else if (preconditions.getOnError().equals(PreconditionContainer.ErrorOption.WARN)) {
                    execType = null; //already logged
                } else {
                    throw new UnexpectedLiquibaseException("Unexpected precondition onError attribute: " + preconditions.getOnError(), e);
                }

                database.rollback();
            } finally {
                database.rollback();
            }

            if (!skipChange) {
                for (Change change : changes) {
                    try {
                        change.finishInitialization();
                    } catch (SetupException se) {
                        throw new MigrationFailedException(this, se);
                    }
                }

                log.fine("Reading ChangeSet: " + this);
                for (Change change : getChanges()) {
                    if ((!(change instanceof DbmsTargetedChange)) || DatabaseList.definitionMatches(((DbmsTargetedChange) change).getDbms(), database, true)) {
                        if (listener != null) {
                            listener.willRun(change, this, changeLog, database);
                        }
                        if (change.generateStatementsVolatile(database)) {
                            executor.comment("WARNING The following SQL may change each run and therefore is possibly incorrect and/or invalid:");
                        }

                        String sql = addSqlMdc(change, database, false);
                        this.getGeneratedSql().add(sql);

                        database.executeStatements(change, databaseChangeLog, sqlVisitors);
                        log.info(change.getConfirmationMessage());
                        if (listener != null) {
                            listener.ran(change, this, changeLog, database);
                        }
                    } else {
                        log.fine("Change " + change.getSerializedObjectName() + " not included for database " + database.getShortName());
                    }
                }

                if (runInTransaction) {
                    database.commit();
                }
                if (execType == null) {
                    execType = ExecType.EXECUTED;
                }
                operationStopTime = new Date();
                long stopTime = operationStopTime.getTime();
                Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OPERATION_STOP_TIME, Instant.ofEpochMilli(stopTime).toString());
                Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OUTCOME, execType.value.toLowerCase());
                log.info("ChangeSet " + toString(false) + " ran successfully in " + (stopTime - startTime) + "ms");
            } else {
                log.fine("Skipping ChangeSet: " + this);
            }

        } catch (Exception e) {
            operationStopTime = new Date();
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OPERATION_STOP_TIME, Instant.ofEpochMilli(operationStopTime.getTime()).toString());
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OUTCOME, ExecType.FAILED.value.toLowerCase());
            log.severe(String.format("ChangeSet %s encountered an exception.", toString(false)), e);
            setErrorMsg(e.getMessage());
            try {
                database.rollback();
            } catch (Exception e1) {
                throw new MigrationFailedException(this, e);
            }
            if ((failOnError != null) && !failOnError) {
                log.info("Changeset " + toString(false) + " failed, but failOnError was false.  Error: " + e.getMessage());
                log.fine("Failure Stacktrace", e);
                execType = ExecType.FAILED;
            } else {
                if (e instanceof MigrationFailedException) {
                    throw ((MigrationFailedException) e);
                } else {
                    throw new MigrationFailedException(this, e);
                }
            }
        } finally {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, originalExecutor);
            // restore auto-commit to false if this ChangeSet was not run in a transaction,
            // but only if the database supports DDL in transactions
            if (!runInTransaction && database.supportsDDLInTransaction()) {
                try {
                    database.setAutoCommit(false);
                } catch (DatabaseException e) {
                    Scope.getCurrentScope().getLog(getClass()).warning("Could not resetInternalState autocommit", e);
                }
            }
        }
        return execType;
    }

    //
    // Get the custom Executor ready if necessary
    // We do not do anything if we have a LoggingExecutor.
    //
    private Executor setupCustomExecutorIfNecessary(Database database) {
        Executor originalExecutor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        if (getRunWith() == null || originalExecutor instanceof LoggingExecutor) {
            return originalExecutor;
        }
        Scope.getCurrentScope().addMdcValue(MdcKey.RUN_WITH, getRunWith());
        String executorName = ChangeSet.lookupExecutor(getRunWith());
        Executor customExecutor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(executorName, database);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, customExecutor);
        List<Change> changes = getChanges();
        for (Change change : changes) {
            if (!(change instanceof AbstractChange)) {
                continue;
            }
            final ResourceAccessor resourceAccessor = ((AbstractChange) change).getResourceAccessor();
            if (resourceAccessor != null) {
                customExecutor.setResourceAccessor(resourceAccessor);
                break;
            }
        }
        return originalExecutor;
    }

    /**
     * Look for a configuration property that matches liquibase.<executor name>.executor
     * and if found, return its value as the executor name
     *
     * @param executorName The value from the input changeset runWith attribute
     * @return String                           The mapped value
     */
    public static String lookupExecutor(String executorName) {
        if (StringUtil.isEmpty(executorName)) {
            return null;
        }
        String key = "liquibase." + executorName.toLowerCase() + ".executor";
        if (executorName.equalsIgnoreCase("psql") || executorName.equalsIgnoreCase("sqlcmd") || executorName.equalsIgnoreCase("sqlplus")) {
            return executorName;
        }
        String replacementExecutorName = (String) Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)
                .getCurrentConfiguredValue(null, null, key)
                .getValue();
        if (replacementExecutorName != null) {
            Scope.getCurrentScope().getLog(ChangeSet.class).info("Mapped '" + executorName + "' to executor '" + replacementExecutorName + "'");
            return replacementExecutorName;
        } else if (executorName.equalsIgnoreCase("native")) {
            String message = "Unable to locate an executor for 'runWith=" + executorName + "'.  You must specify a valid executor name.";
            Scope.getCurrentScope().getLog(ChangeSet.class).warning(message);
            Scope.getCurrentScope().getUI().sendErrorMessage("WARNING: " + message);
        }
        return executorName;
    }

    public void rollback(Database database) throws RollbackFailedException {
        rollback(database, null);
    }

    public void rollback(Database database, ChangeExecListener listener) throws RollbackFailedException {
        operationStartTime = new Date();
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OPERATION_START_TIME, Instant.ofEpochMilli(operationStartTime.getTime()).toString());
        addChangeSetMdcProperties();
        Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_ID, getDeploymentId());
        Executor originalExecutor = setupCustomExecutorIfNecessary(database);
        try {
            Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
            executor.comment("Rolling Back ChangeSet: " + this);

            database.setObjectQuotingStrategy(objectQuotingStrategy);

            // set auto-commit based on runInTransaction if database supports DDL in transactions
            if (database.supportsDDLInTransaction()) {
                database.setAutoCommit(!runInTransaction);
            }

            executor.modifyChangeSet(this);

            if (hasCustomRollbackChanges()) {
                final List<SqlStatement> statements = new LinkedList<>();
                for (Change change : rollback.getChanges()) {
                    if (this.ignoreSpecificChangeTypes(change, database)) {
                        continue;
                    }
                    if (listener != null) {
                        listener.willRun(change, this, changeLog, database);
                    }
                    ValidationErrors errors = change.validate(database);
                    if (errors.hasErrors()) {
                        throw new RollbackFailedException("Rollback statement failed validation: " + errors);
                    }
                    //
                    SqlStatement[] changeStatements = change.generateStatements(database);
                    String sql = addSqlMdc(change, database, false);
                    this.getGeneratedSql().add(sql);
                    if (change instanceof SQLFileChange) {
                        addSqlFileMdc((SQLFileChange) change);
                    }
                    if (changeStatements != null) {
                        statements.addAll(Arrays.asList(changeStatements));
                    }
                    if (listener != null) {
                        listener.ran(change, this, changeLog, database);
                    }
                }
                if (!statements.isEmpty()) {
                    database.executeRollbackStatements(statements.toArray(SqlStatement.EMPTY_SQL_STATEMENT), sqlVisitors);
                }

            } else {
                List<Change> changes = getChanges();
                for (int i = changes.size() - 1; i >= 0; i--) {
                    Change change = changes.get(i);
                    if (change instanceof RawSQLChange && this.getFilePath().toLowerCase().endsWith(".sql")) {
                        throw new RollbackFailedException("Liquibase does not support automatic rollback generation for raw " +
                            "sql changes (did you mean to specify keyword \"empty\" to ignore rolling back this change?)");
                    }
                    String sql = addSqlMdc(change, database, true);
                    this.getGeneratedSql().add(sql);
                    database.executeRollbackStatements(change, sqlVisitors);
                }
            }

            if (runInTransaction) {
                database.commit();
            }
            rollbackExecType = ExecType.EXECUTED;
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OUTCOME, ExecType.EXECUTED.value.toLowerCase());
            operationStopTime = new Date();
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OPERATION_STOP_TIME, Instant.ofEpochMilli(operationStopTime.getTime()).toString());
            Scope.getCurrentScope().getLog(getClass()).fine("ChangeSet " + toString() + " has been successfully rolled back.");
        } catch (Exception e) {
            setErrorMsg(e.getMessage());
            operationStopTime = new Date();
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OPERATION_STOP_TIME, Instant.ofEpochMilli(operationStopTime.getTime()).toString());
            try {
                rollbackExecType = ExecType.FAILED;
                Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_OUTCOME, ExecType.FAILED.value.toLowerCase());
                Scope.getCurrentScope().getLog(getClass()).fine("ChangeSet " + this + " rollback failed.");
                database.rollback();
            } catch (DatabaseException e1) {
                //ok
            }
            throw new RollbackFailedException(e);
        } finally {
            // restore auto-commit to false if this ChangeSet was not run in a transaction,
            // but only if the database supports DDL in transactions
            Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, originalExecutor);
            if (!runInTransaction && database.supportsDDLInTransaction()) {
                try {
                    database.setAutoCommit(false);
                } catch (DatabaseException e) {
                    Scope.getCurrentScope().getLog(getClass()).warning("Could not resetInternalState autocommit", e);
                }
            }
        }

    }

    private void addSqlFileMdc(SQLFileChange change) {
        RollbackSqlFile rollbackSqlFile = new RollbackSqlFile(change);
        Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_SQL_FILE, rollbackSqlFile);
    }

    private boolean ignoreSpecificChangeTypes(Change change, Database database) {
        return ((change instanceof DbmsTargetedChange) && !DatabaseList.definitionMatches(((DbmsTargetedChange) change).getDbms(), database, true))
             || ((change instanceof RawSQLChange) && "empty".equalsIgnoreCase(((RawSQLChange)change).getSql()));
    }

    /**
     * Returns whether custom rollback steps are specified for this changeSet, or whether auto-generated ones should be used
     */
    public boolean hasCustomRollbackChanges() {
        return (rollback.getChanges() != null) && !rollback.getChanges().isEmpty();
    }

    /**
     * Returns an unmodifiable list of changes.  To add one, use the addRefactoring method.
     */
    public List<Change> getChanges() {
        return Collections.unmodifiableList(changes);
    }

    /**
     * Method created to remove changes from a changeset
     * @param collection
     */
    public void removeAllChanges(Collection<?> collection) {
        this.changes.removeAll(collection);
    }

    public void addChange(Change change) {
        if (change == null) {
            return;
        }
        changes.add(change);
        change.setChangeSet(this);
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    /**
     * @deprecated use {@link #getContextFilter()}
     */
    @Deprecated
    public ContextExpression getContexts() {
        return getContextFilter();
    }

    /**
     * @deprecated use {@link #setContextFilter(ContextExpression)}
     */
    @Deprecated
    public ChangeSet setContexts(ContextExpression contexts) {
        return setContextFilter(contexts);
    }

    public ContextExpression getContextFilter() {
        return contextFilter;
    }

    public ChangeSet setContextFilter(ContextExpression contextFilter) {
        this.contextFilter = contextFilter;
        return this;
    }

    public Labels getLabels() {
        return labels;
    }

    public void setLabels(Labels labels) {
        this.labels = labels;
    }

    public Set<String> getDbmsSet() {
        return dbmsSet;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public boolean isInheritableIgnore() {
        DatabaseChangeLog changeLog = getChangeLog();
        if (changeLog == null) {
            return false;
        }

        return changeLog.isIncludeIgnore();
    }

    public Collection<ContextExpression> getInheritableContextFilter() {
        Collection<ContextExpression> expressions = new ArrayList<>();
        DatabaseChangeLog changeLog = getChangeLog();
        while (changeLog != null) {
            ContextExpression expression = changeLog.getContextFilter();
            if ((expression != null) && !expression.isEmpty()) {
                expressions.add(expression);
            }
            ContextExpression includeExpression = changeLog.getIncludeContextFilter();
            if ((includeExpression != null) && !includeExpression.isEmpty()) {
                expressions.add(includeExpression);
            }
            changeLog = changeLog.getParentChangeLog();
        }
        return Collections.unmodifiableCollection(expressions);
    }

    public Collection<Labels> getInheritableLabels() {
        Collection<Labels> labels = new ArrayList<>();
        DatabaseChangeLog changeLog = getChangeLog();
        while (changeLog != null) {
            Labels includeLabels = changeLog.getIncludeLabels();
            if (includeLabels != null && !includeLabels.isEmpty()) {
                labels.add(includeLabels);
            }
            changeLog = changeLog.getParentChangeLog();
        }
        return Collections.unmodifiableCollection(labels);
    }

    /**
     * Build and return a string which contains both the changeset and inherited context
     *
     * @return String
     */
    public String buildFullContext() {
        StringBuilder contextExpression = new StringBuilder();
        boolean notFirstContext = false;
        for (ContextExpression inheritableContext : getInheritableContextFilter()) {
            appendContext(contextExpression, inheritableContext.toString(), notFirstContext);
            notFirstContext = true;
        }
        ContextExpression changeSetContext = getContexts();
        if ((changeSetContext != null) && !changeSetContext.isEmpty()) {
            appendContext(contextExpression, changeSetContext.toString(), notFirstContext);
        }
        return StringUtil.trimToNull(contextExpression.toString());
    }

    /**
     * Build and return a string which contains both the changeset and inherited labels
     *
     * @return String
     */
    public String buildFullLabels() {
        StringBuilder labels = new StringBuilder();
        boolean notFirstLabel = false;
        for (Labels inheritableLabel : getInheritableLabels()) {
            appendLabels(labels, inheritableLabel.toString(), notFirstLabel);
            notFirstLabel = true;
        }
        Labels changeSetLabels = getLabels();
        if ((changeSetLabels != null) && !changeSetLabels.isEmpty()) {
            appendLabels(labels, changeSetLabels.toString(), notFirstLabel);
        }
        return StringUtil.trimToNull(labels.toString());
    }

    private void appendLabels(StringBuilder existingLabels, String labelToAppend, boolean notFirstContext) {
        if (notFirstContext) {
            existingLabels.append(COMMA);
        }
        existingLabels.append(labelToAppend);
    }

    private void appendContext(StringBuilder contextExpression, String contextToAppend, boolean notFirstContext) {
        boolean complexExpression = contextToAppend.contains(COMMA) || contextToAppend.contains(WHITESPACE);
        if (notFirstContext) {
            contextExpression.append(AND);
        }
        if (complexExpression) {
            contextExpression.append(OPEN_BRACKET);
        }
        contextExpression.append(contextToAppend);
        if (complexExpression) {
            contextExpression.append(CLOSE_BRACKET);
        }
    }

    public DatabaseChangeLog getChangeLog() {
        return changeLog;
    }

    public String toString(boolean includeMD5Sum) {
        ChecksumVersion checksumVersion = ChecksumVersion.enumFromChecksumVersion(this.checkSum != null ? this.checkSum.getVersion() : ChecksumVersion.latest().getVersion());
        return filePath + "::" + getId() + "::" + getAuthor() +
                (includeMD5Sum ? ("::(Checksum: " + generateCheckSum(checksumVersion) + ")") : "");
    }

    public String toNormalizedString() {
        return DatabaseChangeLog.normalizePath(filePath) + "::" + getId() + "::" + getAuthor();
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isAlwaysRun() {
        return alwaysRun;
    }

    public boolean isRunOnChange() {
        return runOnChange;
    }

    public boolean isRunInTransaction() {
        return runInTransaction;
    }

    public RollbackContainer getRollback() {
        return rollback;
    }

    public void addRollBackSQL(String sql) {
        if (StringUtil.trimToNull(sql) == null) {
            if (rollback.getChanges().isEmpty()) {
                rollback.getChanges().add(new EmptyChange());
            }
            return;
        }

        for (String statement : StringUtil.splitSQL(sql, null, this)) {
            rollback.getChanges().add(new RawSQLChange(statement.trim()));
        }
    }

    public void addRollbackChange(Change change) {
        if (change == null) {
            return;
        }
        rollback.getChanges().add(change);
        change.setChangeSet(this);
    }


    public boolean supportsRollback(Database database) {
        if ((rollback.getChanges() != null) && !rollback.getChanges().isEmpty()) {
            return true;
        }

        for (Change change : getChanges()) {
            if (!change.supportsRollback(database)) {
                return false;
            }
        }
        return true;
    }

    public String getDescription() {
        List<Change> changes = getChanges();
        if (changes.isEmpty()) {
            return "empty";
        }

        List<String> messages = new ArrayList<>();
        for (Change change : changes) {
            messages.add(change.getDescription());
        }

        return StringUtil.limitSize(StringUtil.join(messages, "; "), 255);
    }

    public Boolean getFailOnError() {
        return failOnError;
    }

    public void setFailOnError(Boolean failOnError) {
        this.failOnError = failOnError;
    }

    public ValidationFailOption getOnValidationFail() {
        return onValidationFail;
    }

    public void setOnValidationFail(ValidationFailOption onValidationFail) {
        this.onValidationFail = onValidationFail;
    }

    public void setValidationFailed(boolean validationFailed) {
        this.validationFailed = validationFailed;
    }

    public void addValidCheckSum(String text) {
        validCheckSums.add(CheckSum.parse(text));
    }

    public Set<CheckSum> getValidCheckSums() {
        return Collections.unmodifiableSet(validCheckSums);
    }

    public boolean isCheckSumValid(CheckSum storedCheckSum) {
        // no need to generate the checksum if any has been set as the valid checksum
        for (CheckSum validCheckSum : validCheckSums) {
            if ("1:any".equalsIgnoreCase(validCheckSum.toString())
                    || "1:all".equalsIgnoreCase(validCheckSum.toString())
                    || "1:*".equalsIgnoreCase(validCheckSum.toString())) {
                return true;
            }
        }
        CheckSum currentMd5Sum = storedCheckSum != null ? generateCheckSum(ChecksumVersion.enumFromChecksumVersion(storedCheckSum.getVersion())) : null;
        if (currentMd5Sum == null) {
            return true;
        }
        if (storedCheckSum == null) {
            return true;
        }
        if (currentMd5Sum.equals(storedCheckSum)) {
            return true;
        }

        for (CheckSum validCheckSum : validCheckSums) {
            if (currentMd5Sum.equals(validCheckSum) || storedCheckSum.equals(validCheckSum)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public PreconditionContainer getPreconditions() {
        return preconditions;
    }

    @Override
    public void setPreconditions(PreconditionContainer preconditionContainer) {
        this.preconditions = preconditionContainer;
    }

    public void addSqlVisitor(SqlVisitor sqlVisitor) {
        sqlVisitors.add(sqlVisitor);
    }

    public List<SqlVisitor> getSqlVisitors() {
        return sqlVisitors;
    }

    public ChangeLogParameters getChangeLogParameters() {
        return changeLogParameters;
    }

    /**
     * Called by the changelog parsing process to pass the {@link ChangeLogParameters}.
     */
    public void setChangeLogParameters(ChangeLogParameters changeLogParameters) {
        this.changeLogParameters = changeLogParameters;
    }

    /**
     * Called to update file path from database entry when rolling back and ignoreClasspathPrefix is true.
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setRunOrder(String runOrder) {
        if (runOrder != null) {
            runOrder = runOrder.toLowerCase();
            if (!"first".equals(runOrder) && !"last".equals(runOrder)) {
                throw new UnexpectedLiquibaseException("runOrder must be 'first' or 'last'");
            }
        }
        this.runOrder = runOrder;
    }

    @Override
    public String getSerializedObjectName() {
        return "changeSet";
    }

    @Override
    public Set<String> getSerializableFields() {
        return new LinkedHashSet<>(
                Arrays.asList(
                        "id", "author", "runAlways", "runOnChange", "failOnError", "contextFilter", "labels", "dbms",
                        "objectQuotingStrategy", "comment", "preconditions", "changes", "rollback", "labels",
                        "logicalFilePath", "created", "runInTransaction", "runOrder", "ignore"
                )
        );
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        if ("id".equals(field)) {
            return this.getId();
        }
        if ("author".equals(field)) {
            return this.getAuthor();
        }

        if ("runAlways".equals(field)) {
            if (this.isAlwaysRun()) {
                return true;
            } else {
                return null;
            }
        }

        if ("runOnChange".equals(field)) {
            if (this.isRunOnChange()) {
                return true;
            } else {
                return null;
            }
        }

        if ("failOnError".equals(field)) {
            return this.getFailOnError();
        }

        if ("contextFilter".equals(field) || "context".equals(field)) {
            if (!this.getContextFilter().isEmpty()) {
                return this.getContextFilter().toString().replaceFirst("^\\(", "").replaceFirst("\\)$", "");
            } else {
                return null;
            }
        }

        if ("labels".equals(field)) {
            if ((this.getLabels() != null) && !this.getLabels().isEmpty()) {
                return StringUtil.join(this.getLabels().getLabels(), ", ");
            } else {
                return null;
            }
        }

        if ("dbms".equals(field)) {
            if ((this.getDbmsSet() != null) && !this.getDbmsSet().isEmpty()) {
                return StringUtil.join(getDbmsSet(), ",");
            } else {
                return null;
            }
        }

        if ("comment".equals(field)) {
            return StringUtil.trimToNull(this.getComments());
        }

        if ("objectQuotingStrategy".equals(field)) {
            if (this.getObjectQuotingStrategy() == null) {
                return null;
            }
            return this.getObjectQuotingStrategy().toString();
        }

        if ("preconditions".equals(field)) {
            if ((this.getPreconditions() != null) && !this.getPreconditions().getNestedPreconditions().isEmpty()) {
                return this.getPreconditions();
            } else {
                return null;
            }
        }

        if ("changes".equals(field)) {
            return getChanges();
        }

        if ("created".equals(field)) {
            return getCreated();
        }

        if ("logicalFilePath".equals(field)) {
            return getLogicalFilePath();
        }

        if ("rollback".equals(field)) {
            if ((rollback.getChanges() != null) && !rollback.getChanges().isEmpty()) {
               return rollback;
            } else {
                return null;
            }
        }

        if ("runInTransaction".equals(field)) {
            if (!this.isRunInTransaction()) {
                return false;
            } else {
                return null;
            }
        }

        if ("runOrder".equals(field)) {
            return getRunOrder();
        }

        if ("ignore".equals(field)) {
            if (this.isIgnore()) {
                return true;
            } else {
                return null;
            }
        }

        throw new UnexpectedLiquibaseException("Unexpected field request on changeSet: " + field);
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        if ("comment".equals(field) || "preconditions".equals(field) || "changes".equals(field) || "rollback".equals
                (field)) {
            return SerializationType.NESTED_OBJECT;
        } else {
            return SerializationType.NAMED_FIELD;
        }
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public String getSerializableFieldNamespace(String field) {
        return getSerializedObjectNamespace();
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChangeSet)) {
            return false;
        }
        return this.toString(false).equals(((ChangeSet) obj).toString(false));
    }

    @Override
    public int hashCode() {
        return toString(false).hashCode();
    }

    public Object getAttribute(String attribute) {
        return attributes.get(attribute);
    }

    public ChangeSet setAttribute(String attribute, Object value) {
        this.attributes.put(attribute, value);

        return this;
    }

    /**
     * Gets storedCheckSum
     *
     * @return storedCheckSum if it was executed otherwise null
     */
    public CheckSum getStoredCheckSum() {
        return storedCheckSum;
    }

    /**
     * Sets the stored checksum in the ValidatingVisitor in case the changeset was executed.
     *
     * @param storedCheckSum the checksum to set
     */
    public void setStoredCheckSum(CheckSum storedCheckSum) {
        this.storedCheckSum = storedCheckSum;
    }

    /**
     * @return Deployment ID stored in the databasechangelog table.
     */
    public String getDeploymentId() {
        return deploymentId;
    }

    /**
     * @param deploymentId Deployment ID stored in the databasechangelog table.
     */
    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public void addChangeSetMdcProperties() {
        String commentMdc = comments != null ? comments : "";
        String labelMdc = labels != null ? labels.toString() : "";
        String contextsMdc = contextFilter != null && contextFilter.getOriginalString() != null ? contextFilter.getOriginalString() : "";

        String changelogPath = (getChangeLog() != null ? getChangeLog().getLogicalFilePath() : null);
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, changelogPath);
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_COMMENT, commentMdc);
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_LABEL, labelMdc);
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_CONTEXT, contextsMdc);
    }

    /**
     *
     * Adds changeset sql to mdc if the change is supported by the database
     * @param change the change to read sql from
     * @param database the database to generate change sql against
     * @param generateRollbackStatements controls generation of rollback sql statements or standard statements sql
     * @throws RollbackImpossibleException if you cannot generate rollback statements
     *
     */
    private String addSqlMdc(Change change, Database database, boolean generateRollbackStatements) throws Exception {
        //
        // If the change is for this Database
        // add a Boolean flag to Scope to indicate that the Change should not be executed when adding MDC context
        //
        if (! change.supports(database)) {
            return null;
        }
        AtomicReference<SqlStatement[]> statementsReference = new AtomicReference<>();
        Map<String, Object> scopeValues = new HashMap<>();
        scopeValues.put(Change.SHOULD_EXECUTE, Boolean.FALSE);
        Scope.child(scopeValues, () -> statementsReference.set(generateRollbackStatements ?
               change.generateRollbackStatements(database) : change.generateStatements(database)));
        String sqlStatementsMdc = Arrays.stream(statementsReference.get())
                    .map(statement -> SqlUtil.getSqlString(statement, SqlGeneratorFactory.getInstance(), database))
                    .collect(Collectors.joining("\n"));
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_SQL, sqlStatementsMdc);
        return sqlStatementsMdc;
    }

    private List<ChangeVisitor> getChangeVisitors(){
       return getChangeLog().getChangeVisitors();
    }
}
