package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.change.CheckSum;
import liquibase.change.DbmsTargetedChange;
import liquibase.change.core.EmptyChange;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.database.DatabaseList;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.*;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.Conditional;
import liquibase.precondition.ErrorPrecondition;
import liquibase.precondition.FailedPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sql.visitor.SqlVisitorFactory;
import liquibase.statement.SqlStatement;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtil;

import java.util.*;

/**
 * Encapsulates a changeSet and all its associated changes.
 */
public class ChangeSet implements Conditional, ChangeLogChild {

    protected CheckSum checkSum;

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

        String key;

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
    private List<Change> changes;

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

    private Logger log;

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
    private ContextExpression contexts;

    /**
     * "Labels" associated with this changeSet.  If null or empty, will execute regardless of contexts set
     */
    private Labels labels;

    /**
     *
     * If set to true, the changeSet will be ignored (skipped)
     *
     */
    private boolean ignore;

    /**
     * Databases for which this changeset should run.  The string values should match the value returned from Database.getShortName()
     */
    private Set<String> dbmsSet;

    /**
     * If false, do not stop liquibase update execution if an error is thrown executing the changeSet.  Defaults to true
     */
    private Boolean failOnError;

    /**
     * List of checksums that are assumed to be valid besides the one stored in the database.  Can include the string "any"
     */
    private Set<CheckSum> validCheckSums = new HashSet<>();

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
    private RollbackContainer rollback = new RollbackContainer();


    /**
     * ChangeSet comments defined in changeLog file
     */
    private String comments;

    /**
     * ChangeSet level precondtions defined for this changeSet
     */
    private PreconditionContainer preconditions;

    /**
     * SqlVisitors defined for this changeset.
     * SqlVisitors will modify the SQL generated by the changes before sending it to the database.
     */
    private List<SqlVisitor> sqlVisitors = new ArrayList<>();

    private ObjectQuotingStrategy objectQuotingStrategy;

    private DatabaseChangeLog changeLog;

    private String created;

    /**
     * Allow changeSet to be ran "first" or "last". Multiple changeSets with the same runOrder will preserve their order relative to each other.
     */
    private String runOrder;

    private Map<String, Object> attributes = new HashMap<>();

    public boolean shouldAlwaysRun() {
        return alwaysRun;
    }

    public boolean shouldRunOnChange() {
        return runOnChange;
    }

    public ChangeSet(DatabaseChangeLog databaseChangeLog) {
        this.changes = new ArrayList<>();
        log = LogService.getLog(getClass());
        this.changeLog = databaseChangeLog;
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, String filePath, String contextList, String dbmsList, DatabaseChangeLog databaseChangeLog) {
        this(id, author, alwaysRun, runOnChange, filePath, contextList, dbmsList, true, ObjectQuotingStrategy.LEGACY, databaseChangeLog);
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, String filePath, String contextList, String dbmsList, boolean runInTransaction, DatabaseChangeLog databaseChangeLog) {
        this(id, author, alwaysRun, runOnChange, filePath, contextList, dbmsList, runInTransaction, ObjectQuotingStrategy.LEGACY, databaseChangeLog);
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, String filePath, String contextList, String dbmsList, ObjectQuotingStrategy quotingStrategy, DatabaseChangeLog databaseChangeLog) {
        this(id, author, alwaysRun, runOnChange, filePath, contextList, dbmsList, true, quotingStrategy, databaseChangeLog);
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, String filePath, String contextList, String dbmsList,
                     boolean runInTransaction, ObjectQuotingStrategy quotingStrategy, DatabaseChangeLog databaseChangeLog) {
        this(databaseChangeLog);
        this.id = id;
        this.author = author;
        this.filePath = filePath;
        this.alwaysRun = alwaysRun;
        this.runOnChange = runOnChange;
        this.runInTransaction = runInTransaction;
        this.objectQuotingStrategy = quotingStrategy;
        this.contexts = new ContextExpression(contextList);
        setDbms(dbmsList);
    }

    protected void setDbms(String dbmsList) {
        this.dbmsSet = DatabaseList.toDbmsSet(dbmsList);
    }

    public String getFilePath() {
        return filePath;
    }

    public void clearCheckSum() {
        this.checkSum = null;
    }

    public CheckSum generateCheckSum() {
        if (checkSum == null) {
            StringBuffer stringToMD5 = new StringBuffer();
            for (Change change : getChanges()) {
                stringToMD5.append(change.generateCheckSum()).append(":");
            }

            for (SqlVisitor visitor : this.getSqlVisitors()) {
                stringToMD5.append(visitor.generateCheckSum()).append(";");
            }


            checkSum = CheckSum.compute(stringToMD5.toString());
        }

        return checkSum;
    }

    @Override
    public void load(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        this.id = node.getChildValue(null, "id", String.class);
        this.author = node.getChildValue(null, "author", String.class);
        this.alwaysRun  = node.getChildValue(null, "runAlways", node.getChildValue(null, "alwaysRun", false));
        this.runOnChange  = node.getChildValue(null, "runOnChange", false);
        this.contexts = new ContextExpression(node.getChildValue(null, "context", String.class));
        this.labels = new Labels(StringUtil.trimToNull(node.getChildValue(null, "labels", String.class)));
        setDbms(node.getChildValue(null, "dbms", String.class));
        this.runInTransaction  = node.getChildValue(null, "runInTransaction", true);
        this.created = node.getChildValue(null, "created", String.class);
        this.runOrder = node.getChildValue(null, "runOrder", String.class);
        this.ignore = node.getChildValue(null, "ignore", false);
        this.comments = StringUtil.join(node.getChildren(null, "comment"), "\n", new StringUtil.StringUtilFormatter() {
            @Override
            public String toString(Object obj) {
                if (((ParsedNode) obj).getValue() == null) {
                    return "";
                } else {
                    return ((ParsedNode) obj).getValue().toString();
                }
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

        this.filePath = StringUtil.trimToNull(node.getChildValue(null, "logicalFilePath", String.class));
        if (filePath == null) {
            filePath = changeLog.getFilePath();
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
                String contextString = StringUtil.trimToNull(child.getChildValue(null, "context", String.class));
                String labelsString = StringUtil.trimToNull(child.getChildValue(null, "labels", String.class));
                boolean applyToRollback = child.getChildValue(null, "applyToRollback", false);

                Set<String> dbms = new HashSet<>();
                if (dbmsString != null) {
                    dbms.addAll(StringUtil.splitAndTrim(dbmsString, ","));
                }
                ContextExpression context = null;
                if (contextString != null) {
                    context = new ContextExpression(contextString);
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
                        sqlVisitor.setContexts(context);
                        sqlVisitor.setLabels(labels);
                        sqlVisitor.load(node, resourceAccessor);

                        addSqlVisitor(sqlVisitor);
                    }
                }


                break;
            case "preConditions":
                this.preconditions = new PreconditionContainer();
                try {
                    this.preconditions.load(child, resourceAccessor);
                } catch (ParsedNodeException e) {
                    e.printStackTrace();
                }
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
            ChangeSet changeSet = changeLog.getChangeSet(changeSetPath, changeSetAuthor, changeSetId);
            while ((changeSet == null) && (changeLog != null)) {
                changeLog = changeLog.getParentChangeLog();
                if (changeLog != null) {
                    changeSet = changeLog.getChangeSet(changeSetPath, changeSetAuthor, changeSetId);
                }
            }
            if (changeSet == null) {
                throw new ParsedNodeException("Change set " + new ChangeSet(changeSetId, changeSetAuthor, false, false, changeSetPath, null, null, null).toString(false) + " does not exist");
            }
            for (Change change : changeSet.getChanges()) {
                rollback.getChanges().add(change);
            }
            return;
        }

        boolean foundValue = false;
        for (ParsedNode childNode : rollbackNode.getChildren()) {
            Change rollbackChange = toChange(childNode, resourceAccessor);
            if (rollbackChange != null) {
                addRollbackChange(rollbackChange);
                foundValue =  true;
            }
        }

        Object value = rollbackNode.getValue();
        if (value != null) {
            if (value instanceof String) {
                String finalValue = StringUtil.trimToNull((String) value);
                if (finalValue != null) {
                    String[] strings = StringUtil.processMutliLineSQL(finalValue, true, true, ";");
                    for (String string : strings) {
                        addRollbackChange(new RawSQLChange(string));
                        foundValue = true;
                    }
                }
            } else {
                throw new ParsedNodeException("Unexpected object: "+value.getClass().getName()+" '"+value.toString()+"'");
            }
        }
        if (!foundValue) {
            addRollbackChange(new EmptyChange());
        }
    }

    protected Change toChange(ParsedNode value, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        Change change = Scope.getCurrentScope().getSingleton(ChangeFactory.class).create(value.getName());
        if (change == null) {
            return null;
        } else {
            change.load(value, resourceAccessor);

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
     * @return should change set be marked as ran
     */
    public ExecType execute(DatabaseChangeLog databaseChangeLog, ChangeExecListener listener, Database database)
            throws MigrationFailedException {
        if (validationFailed) {
            return ExecType.MARK_RAN;
        }

        long startTime = new Date().getTime();

        ExecType execType = null;

        boolean skipChange = false;

        Executor executor = ExecutorService.getInstance().getExecutor(database);
        try {
            // set object quoting strategy
            database.setObjectQuotingStrategy(objectQuotingStrategy);

            if (database.supportsDDLInTransaction()) {
                database.setAutoCommit(!runInTransaction);
            }

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
                StringBuffer message = new StringBuffer();
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

                    LogService.getLog(getClass()).info(LogType.LOG, "Continuing past: " + toString() + " despite precondition failure due to onFail='CONTINUE': " + message);
                } else if (preconditions.getOnFail().equals(PreconditionContainer.FailOption.MARK_RAN)) {
                    execType = ExecType.MARK_RAN;
                    skipChange = true;

                    log.info(LogType.LOG, "Marking ChangeSet: " + toString() + " ran despite precondition failure due to onFail='MARK_RAN': " + message);
                } else if (preconditions.getOnFail().equals(PreconditionContainer.FailOption.WARN)) {
                    execType = null; //already warned
                } else {
                    throw new UnexpectedLiquibaseException("Unexpected precondition onFail attribute: " + preconditions.getOnFail(), e);
                }
            } catch (PreconditionErrorException e) {
                if (listener != null) {
                    listener.preconditionErrored(e, preconditions.getOnError());
                }

                StringBuffer message = new StringBuffer();
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

                    log.info(LogType.LOG, "Marking ChangeSet: " + toString() + " ran despite precondition error: " + message);
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

                log.debug(LogType.LOG, "Reading ChangeSet: " + toString());
                for (Change change : getChanges()) {
                    if ((!(change instanceof DbmsTargetedChange)) || DatabaseList.definitionMatches(((DbmsTargetedChange) change).getDbms(), database, true)) {
                        if (listener != null) {
                            listener.willRun(change, this, changeLog, database);
                        }
                        if (change.generateStatementsVolatile(database)) {
                            executor.comment("WARNING The following SQL may change each run and therefore is possibly incorrect and/or invalid:");
                        }


                        database.executeStatements(change, databaseChangeLog, sqlVisitors);
                        log.info(LogType.LOG, change.getConfirmationMessage());
                        if (listener != null) {
                            listener.ran(change, this, changeLog, database);
                        }
                    } else {
                        log.debug(LogType.LOG, "Change " + change.getSerializedObjectName() + " not included for database " + database.getShortName());
                    }
                }

                if (runInTransaction) {
                    database.commit();
                }
                log.info(LogType.LOG, "ChangeSet " + toString(false) + " ran successfully in " + (new Date().getTime() - startTime + "ms"));
                if (execType == null) {
                    execType = ExecType.EXECUTED;
                }
            } else {
                log.debug(LogType.LOG, "Skipping ChangeSet: " + toString());
            }

        } catch (Exception e) {
            try {
                database.rollback();
            } catch (Exception e1) {
                throw new MigrationFailedException(this, e);
            }
            if ((getFailOnError() != null) && !getFailOnError()) {
                log.info(LogType.LOG, "Change set " + toString(false) + " failed, but failOnError was false.  Error: " + e.getMessage());
                log.debug(LogType.LOG, "Failure Stacktrace", e);
                execType = ExecType.FAILED;
            } else {
                // just log the message, dont log the stacktrace by appending exception. Its logged anyway to stdout
                log.severe(LogType.LOG, "Change Set " + toString(false) + " failed.  Error: " + e.getMessage());
                if (e instanceof MigrationFailedException) {
                    throw ((MigrationFailedException) e);
                } else {
                    throw new MigrationFailedException(this, e);
                }
            }
        } finally {
            // restore auto-commit to false if this ChangeSet was not run in a transaction,
            // but only if the database supports DDL in transactions
            if (!runInTransaction && database.supportsDDLInTransaction()) {
                try {
                    database.setAutoCommit(false);
                } catch (DatabaseException e) {
                    throw new MigrationFailedException(this, "Could not resetInternalState autocommit", e);
                }
            }
        }
        return execType;
    }

    public void rollback(Database database) throws RollbackFailedException {
      rollback(database, null);
    }

    public void rollback(Database database, ChangeExecListener listener) throws RollbackFailedException {
        try {
            Executor executor = ExecutorService.getInstance().getExecutor(database);
            executor.comment("Rolling Back ChangeSet: " + toString());

            database.setObjectQuotingStrategy(objectQuotingStrategy);

            // set auto-commit based on runInTransaction if database supports DDL in transactions
            if (database.supportsDDLInTransaction()) {
                database.setAutoCommit(!runInTransaction);
            }

            RanChangeSet ranChangeSet = database.getRanChangeSet(this);
            if (hasCustomRollbackChanges()) {
                
                final List<SqlStatement> statements = new LinkedList<>();
                for (Change change : rollback.getChanges()) {
                    if (((change instanceof DbmsTargetedChange)) && !DatabaseList.definitionMatches(((DbmsTargetedChange) change).getDbms(), database, true)) {
                        continue;
                    }
                    if (listener != null) {
                        listener.willRun(change, this, changeLog, database);
                    }
                    ValidationErrors errors = change.validate(database);
                    if (errors.hasErrors()) {
                        throw new RollbackFailedException("Rollback statement failed validation: "+errors.toString());
                    }
                    //
                    SqlStatement[] changeStatements = change.generateStatements(database);
                    if (changeStatements != null) {
                        statements.addAll(Arrays.asList(changeStatements));
                    }
                    if (listener != null) {
                        listener.ran(change, this, changeLog, database);
                    }
                }
                if (!statements.isEmpty()) {
                    database.executeRollbackStatements(statements.toArray(new SqlStatement[]{}), sqlVisitors);
                }

            } else {
                List<Change> changes = getChanges();
                for (int i = changes.size() - 1; i >= 0; i--) {
                    Change change = changes.get(i);
                    database.executeRollbackStatements(change, sqlVisitors);
                }
            }

            if (runInTransaction) {
                database.commit();
            }
            log.debug(LogType.LOG, "ChangeSet " + toString() + " has been successfully rolled back.");
        } catch (Exception e) {
            try {
                database.rollback();
            } catch (DatabaseException e1) {
                //ok
            }
            throw new RollbackFailedException(e);
        } finally {
            // restore auto-commit to false if this ChangeSet was not run in a transaction,
            // but only if the database supports DDL in transactions
            if (!runInTransaction && database.supportsDDLInTransaction()) {
                try {
                    database.setAutoCommit(false);
                } catch (DatabaseException e) {
                    throw new RollbackFailedException("Could not resetInternalState autocommit", e);
                }
            }
        }

    }

    /**
     * Returns whether custom rollback steps are specified for this changeSet, or whether auto-generated ones should be used
     */
    protected boolean hasCustomRollbackChanges() {
        return (rollback != null) && (rollback.getChanges() != null) && !rollback.getChanges().isEmpty();
    }

    /**
     * Returns an unmodifiable list of changes.  To add one, use the addRefactoing method.
     */
    public List<Change> getChanges() {
        return Collections.unmodifiableList(changes);
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

    public ContextExpression getContexts() {
        return contexts;
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

    public Collection<ContextExpression> getInheritableContexts() {
        Collection<ContextExpression> expressions = new ArrayList<>();
        DatabaseChangeLog changeLog = getChangeLog();
        while (changeLog != null) {
            ContextExpression expression = changeLog.getContexts();
            if ((expression != null) && !expression.isEmpty()) {
                expressions.add(expression);
            }
            ContextExpression includeExpression = changeLog.getIncludeContexts();
            if ((includeExpression != null) && !includeExpression.isEmpty()) {
                expressions.add(includeExpression);
            }
            changeLog = changeLog.getParentChangeLog();
        }
        return Collections.unmodifiableCollection(expressions);
    }

    public DatabaseChangeLog getChangeLog() {
        return changeLog;
    }

    public String toString(boolean includeMD5Sum) {
        return filePath + "::" + getId() + "::" + getAuthor() + (includeMD5Sum ? ("::(Checksum: " + generateCheckSum() + ")") : "");
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

        for (String statment : StringUtil.splitSQL(sql, null)) {
            rollback.getChanges().add(new RawSQLChange(statment.trim()));
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
        if ((rollback != null) && (rollback.getChanges() != null) && !rollback.getChanges().isEmpty()) {
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
        CheckSum currentMd5Sum = generateCheckSum();
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

    public ObjectQuotingStrategy getObjectQuotingStrategy() {
        return objectQuotingStrategy;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getRunOrder() {
        return runOrder;
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
                "id", "author", "runAlways", "runOnChange", "failOnError", "context", "labels", "dbms",
                "objectQuotingStrategy", "comment", "preconditions", "changes", "rollback", "labels",
                "objectQuotingStrategy", "created"
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

        if ("context".equals(field)) {
            if (!this.getContexts().isEmpty()) {
                return this.getContexts().toString().replaceFirst("^\\(", "").replaceFirst("\\)$", "");
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
                StringBuffer dbmsString = new StringBuffer();
                for (String dbms : this.getDbmsSet()) {
                    dbmsString.append(dbms).append(",");
                }
                return dbmsString.toString().replaceFirst(",$", "");
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

        if ("rollback".equals(field)) {
            if ((rollback != null) && (rollback.getChanges() != null) && !rollback.getChanges().isEmpty()) {
                return rollback;
            } else {
                return null;
            }
        }

        throw new UnexpectedLiquibaseException("Unexpected field request on changeSet: "+field);
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
}
