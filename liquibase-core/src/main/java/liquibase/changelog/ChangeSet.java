package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Labels;
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
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.parser.NamespaceDetails;
import liquibase.parser.core.ParsedNode;
import liquibase.parser.core.ParsedNodeException;
import liquibase.precondition.Conditional;
import liquibase.precondition.ErrorPrecondition;
import liquibase.precondition.FailedPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sql.visitor.SqlVisitorFactory;
import liquibase.statement.SqlStatement;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.util.*;

/**
 * Encapsulates a changeSet and all its associated changes.
 */
public class ChangeSet implements Conditional, LiquibaseSerializable {

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
    private Set<CheckSum> validCheckSums = new HashSet<CheckSum>();

    /**
     * If true, the changeSet will run in a database transaction.  Defaults to true
     */
    private boolean runInTransaction;

    /**
     * Behavior if the validation of any of the changeSet changes fails.  Does not include checksum validation
     */
    private ValidationFailOption onValidationFail = ValidationFailOption.HALT;

    /**
     * Stores if validation failed on this chhangeSet
     */
    private boolean validationFailed;

    /**
     * Changes defined to roll back this changeSet
     */
    private List<Change> rollBackChanges = new ArrayList<Change>();


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
    private List<SqlVisitor> sqlVisitors = new ArrayList<SqlVisitor>();

    private ObjectQuotingStrategy objectQuotingStrategy;

    private DatabaseChangeLog changeLog;

    public boolean shouldAlwaysRun() {
        return alwaysRun;
    }

    public boolean shouldRunOnChange() {
        return runOnChange;
    }

    public ChangeSet(DatabaseChangeLog databaseChangeLog) {
        this.changes = new ArrayList<Change>();
        log = LogFactory.getLogger();
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
        if (StringUtils.trimToNull(dbmsList) != null) {
            String[] strings = dbmsList.toLowerCase().split(",");
            dbmsSet = new HashSet<String>();
            for (String string : strings) {
                dbmsSet.add(string.trim().toLowerCase());
            }
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public CheckSum generateCheckSum() {
        StringBuffer stringToMD5 = new StringBuffer();
        for (Change change : getChanges()) {
            stringToMD5.append(change.generateCheckSum()).append(":");
        }

        for (SqlVisitor visitor : this.getSqlVisitors()) {
            stringToMD5.append(visitor.generateCheckSum()).append(";");
        }


        return CheckSum.compute(stringToMD5.toString());
    }

    @Override
    public void load(ParsedNode node, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        this.id = node.getChildValue(null, "id", String.class);
        this.author = node.getChildValue(null, "author", String.class);
        this.alwaysRun  = node.getChildValue(null, "runAlways", node.getChildValue(null, "alwaysRun", false));
        this.runOnChange  = node.getChildValue(null, "runOnChange", false);
        this.contexts = new ContextExpression(node.getChildValue(null, "context", String.class));
        this.labels = new Labels(StringUtils.trimToNull(node.getChildValue(null, "labels", String.class)));
        setDbms(node.getChildValue(null, "dbms", String.class));
        this.runInTransaction  = node.getChildValue(null, "runInTransaction", true);
        this.comments = StringUtils.join(node.getChildren(null, "comment"), "\n", new StringUtils.StringUtilsFormatter() {
            @Override
            public String toString(Object obj) {
                if (((ParsedNode) obj).getValue() == null) {
                    return "";
                } else {
                    return ((ParsedNode) obj).getValue().toString();
                }
            }
        });
        this.comments = StringUtils.trimToNull(this.comments);

        String objectQuotingStrategyString = StringUtils.trimToNull(node.getChildValue(null, "objectQuotingStrategy", String.class));
        if (changeLog != null) {
            this.objectQuotingStrategy = changeLog.getObjectQuotingStrategy();
        }
        if (objectQuotingStrategyString != null) {
            this.objectQuotingStrategy = ObjectQuotingStrategy.valueOf(objectQuotingStrategyString);
        }

        if (this.objectQuotingStrategy == null) {
            this.objectQuotingStrategy = ObjectQuotingStrategy.LEGACY;
        }

        this.filePath = StringUtils.trimToNull(node.getChildValue(null, "logicalFilePath", String.class));
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
        if (child.getName().equals("rollback")) {
            handleRollbackNode(child, resourceAccessor);
        } else if (child.getName().equals("validCheckSum") || child.getName().equals("validCheckSums")) {
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
        } else if (child.getName().equals("modifySql")) {
            String dbmsString = StringUtils.trimToNull(child.getChildValue(null, "dbms", String.class));
            String contextString = StringUtils.trimToNull(child.getChildValue(null, "context", String.class));
            String labelsString = StringUtils.trimToNull(child.getChildValue(null, "labels", String.class));
            boolean applyToRollback = child.getChildValue(null, "applyToRollback", false);

            Set<String> dbms = new HashSet<String>();
            if (dbmsString != null) {
                dbms.addAll(StringUtils.splitAndTrim(dbmsString, ","));
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
                    if (dbms.size() > 0) {
                        sqlVisitor.setApplicableDbms(dbms);
                    }
                    sqlVisitor.setContexts(context);
                    sqlVisitor.setLabels(labels);
                    sqlVisitor.load(node, resourceAccessor);

                    addSqlVisitor(sqlVisitor);
                }
            }


        } else if (child.getName().equals("preConditions")) {
            this.preconditions = new PreconditionContainer();
            try {
                this.preconditions.load(child, resourceAccessor);
            } catch (ParsedNodeException e) {
                e.printStackTrace();
            }
        } else if (child.getName().equals("changes")) {
            for (ParsedNode changeNode : child.getChildren()) {
                handleChildNode(changeNode, resourceAccessor);
            }
        } else {
            addChange(toChange(child, resourceAccessor));
        }
    }

    protected void handleRollbackNode(ParsedNode rollbackNode, ResourceAccessor resourceAccessor) throws ParsedNodeException {
        String changeSetId = rollbackNode.getChildValue(null, "changeSetId", String.class);
        if (changeSetId != null) {
            String changeSetAuthor = rollbackNode.getChildValue(null, "changeSetAuthor", String.class);
            String changeSetPath = rollbackNode.getChildValue(null, "changeSetPath", getFilePath());

            ChangeSet changeSet = this.getChangeLog().getChangeSet(changeSetPath, changeSetAuthor, changeSetId);
            if (changeSet == null) {
                throw new ParsedNodeException("Change set "+new ChangeSet(changeSetId, changeSetAuthor, false, false, changeSetPath, null, null, null).toString(false)+" does not exist");
            }
            for (Change change : changeSet.getChanges()) {
                addRollbackChange(change);
            }
            return;
        }

        boolean foundValue = false;
        for (ParsedNode childNode : rollbackNode.getChildren()) {
            addRollbackChange(toChange(childNode, resourceAccessor));
            foundValue =  true;
        }

        Object value = rollbackNode.getValue();
        if (value != null) {
            if (value instanceof String) {
                String finalValue = StringUtils.trimToNull((String) value);
                if (finalValue != null) {
                    String[] strings = StringUtils.processMutliLineSQL(finalValue, true, true, ";");
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
        Change change = ChangeFactory.getInstance().create(value.getName());
        if (change == null) {
            return null;
        } else {
            try {
                change.load(value, resourceAccessor);
            } catch (ParsedNodeException e) {
                e.printStackTrace();
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
     * @return should change set be marked as ran
     */
    public ExecType execute(DatabaseChangeLog databaseChangeLog, ChangeExecListener listener, Database database) throws MigrationFailedException {
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

            // set auto-commit based on runInTransaction if database supports DDL in transactions
            if (database.supportsDDLInTransaction()) {
                database.setAutoCommit(!runInTransaction);
            }

            executor.comment("Changeset " + toString(false));
            if (StringUtils.trimToNull(getComments()) != null) {
                String comments = getComments();
                String[] lines = comments.split("\\n");
                for (int i = 0; i < lines.length; i++) {
                    if (i > 0) {
                        lines[i] = database.getLineComment() + " " + lines[i];
                    }
                }
                executor.comment(StringUtils.join(Arrays.asList(lines), "\n"));
            }

            try {
                if (preconditions != null) {
                    preconditions.check(database, databaseChangeLog, this);
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

                    LogFactory.getLogger().info("Continuing past: " + toString() + " despite precondition failure due to onFail='CONTINUE': " + message);
                } else if (preconditions.getOnFail().equals(PreconditionContainer.FailOption.MARK_RAN)) {
                    execType = ExecType.MARK_RAN;
                    skipChange = true;

                    log.info("Marking ChangeSet: " + toString() + " ran despite precondition failure due to onFail='MARK_RAN': " + message);
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

                    log.info("Marking ChangeSet: " + toString() + " ran despite precondition error: " + message);
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

                log.debug("Reading ChangeSet: " + toString());
                for (Change change : getChanges()) {
                    if ((!(change instanceof DbmsTargetedChange)) || DatabaseList.definitionMatches(((DbmsTargetedChange) change).getDbms(), database, true)) {
                        if (listener != null) {
                            listener.willRun(change, this, changeLog, database);
                        }
                        database.executeStatements(change, databaseChangeLog, sqlVisitors);
                        log.info(change.getConfirmationMessage());
                        if (listener != null) {
                            listener.ran(change, this, changeLog, database);
                        }
                    } else {
                        log.debug("Change " + change.getSerializedObjectName() + " not included for database " + database.getShortName());
                    }
                }

                if (runInTransaction) {
                    database.commit();
                }
                log.info("ChangeSet " + toString(false) + " ran successfully in " + (new Date().getTime() - startTime + "ms"));
                if (execType == null) {
                    execType = ExecType.EXECUTED;
                }
            } else {
                log.debug("Skipping ChangeSet: " + toString());
            }

        } catch (Exception e) {
            try {
                database.rollback();
            } catch (Exception e1) {
                throw new MigrationFailedException(this, e);
            }
            if (getFailOnError() != null && !getFailOnError()) {
                log.info("Change set " + toString(false) + " failed, but failOnError was false.  Error: " + e.getMessage());
                log.debug("Failure Stacktrace", e);
                execType = ExecType.FAILED;
            } else {
                log.severe("Change Set " + toString(false) + " failed.  Error: " + e.getMessage(), e);
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
        try {
            Executor executor = ExecutorService.getInstance().getExecutor(database);
            executor.comment("Rolling Back ChangeSet: " + toString());
            
            // set auto-commit based on runInTransaction if database supports DDL in transactions
            if (database.supportsDDLInTransaction()) {
                database.setAutoCommit(!runInTransaction);
            }
            
            RanChangeSet ranChangeSet = database.getRanChangeSet(this);
            if (hasCustomRollbackChanges()) {
                
                final List<SqlStatement> statements = new LinkedList<SqlStatement>();
                for (Change rollback : rollBackChanges) {
                    if (((rollback instanceof DbmsTargetedChange)) && !DatabaseList.definitionMatches(((DbmsTargetedChange) rollback).getDbms(), database, true)) {
                        continue;
                    }
                    SqlStatement[] changeStatements = rollback.generateStatements(database);
                    if (changeStatements != null) {
                        statements.addAll(Arrays.asList(changeStatements));
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
            log.debug("ChangeSet " + toString() + " has been successfully rolled back.");
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
        return rollBackChanges != null && rollBackChanges.size() > 0;
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

    public Change[] getRollBackChanges() {
        return rollBackChanges.toArray(new Change[rollBackChanges.size()]);
    }

    public void addRollBackSQL(String sql) {
        if (StringUtils.trimToNull(sql) == null) {
            if (this.rollBackChanges.size() == 0) {
                rollBackChanges.add(new EmptyChange());
            }
            return;
        }

        for (String statment : StringUtils.splitSQL(sql, null)) {
            rollBackChanges.add(new RawSQLChange(statment.trim()));
        }
    }

    public void addRollbackChange(Change change) {
        if (change == null) {
            return;
        }
        rollBackChanges.add(change);
        change.setChangeSet(this);
    }


    public boolean supportsRollback(Database database) {
        if (rollBackChanges != null && rollBackChanges.size() > 0) {
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
        if (changes.size() == 0) {
            return "Empty";
        }

        StringBuffer returnString = new StringBuffer();
        Class<? extends Change> lastChangeClass = null;
        int changeCount = 0;
        for (Change change : changes) {
            if (change.getClass().equals(lastChangeClass)) {
                changeCount++;
            } else if (changeCount > 1) {
                returnString.append(" (x").append(changeCount).append(")");
                returnString.append(", ");
                returnString.append(ChangeFactory.getInstance().getChangeMetaData(change).getName());
                changeCount = 1;
            } else {
                returnString.append(", ").append(ChangeFactory.getInstance().getChangeMetaData(change).getName());
                changeCount = 1;
            }
            lastChangeClass = change.getClass();
        }

        if (changeCount > 1) {
            returnString.append(" (x").append(changeCount).append(")");
        }

        return returnString.toString().replaceFirst("^, ", "");
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
            if (validCheckSum.toString().equalsIgnoreCase("1:any")) {
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
            if (currentMd5Sum.equals(validCheckSum)) {
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
 
    @Override
    public String getSerializedObjectName() {
        return "changeSet";
    }

    @Override
    public Set<String> getSerializableFields() {
        return new HashSet<String>(Arrays.asList(
                "id",
                "author",
                "runAlways",
                "runOnChange",
                "failOnError",
                "context",
                "dbms",
                "comment",
                "changes",
                "rollback",
                "objectQuotingStrategy"));

    }

    @Override
    public Object getSerializableFieldValue(String field) {
        if (field.equals("id")) {
            return this.getId();
        }
        if (field.equals("author")) {
            return this.getAuthor();
        }

        if (field.equals("runAlways")) {
            if (this.isAlwaysRun()) {
                return true;
            } else {
                return null;
            }
        }

        if (field.equals("runOnChange")) {
            if (this.isRunOnChange()) {
                return true;
            } else {
                return null;
            }
        }

        if (field.equals("failOnError")) {
            return this.getFailOnError();
        }

        if (field.equals("context")) {
            if (!this.getContexts().isEmpty()) {
                return this.getContexts().toString();
            } else {
                return null;
            }
        }

        if (field.equals("labels")) {
            if (this.getLabels() != null && !this.getLabels().isEmpty()) {
                return StringUtils.join(this.getLabels().getLabels(), ", ");
            } else {
                return null;
            }
        }

        if (field.equals("dbms")) {
            if (this.getDbmsSet() != null && this.getDbmsSet().size() > 0) {
                StringBuffer dbmsString = new StringBuffer();
                for (String dbms : this.getDbmsSet()) {
                    dbmsString.append(dbms).append(",");
                }
                return dbmsString.toString().replaceFirst(",$", "");
            } else {
                return null;
            }
        }

        if (field.equals("comment")) {
            return StringUtils.trimToNull(this.getComments());
        }

        if (field.equals("objectQuotingStrategy")) {
            if (this.getObjectQuotingStrategy() == null) {
                return null;
            }
            return this.getObjectQuotingStrategy().toString();
        }

        if (field.equals("changes")) {
            return getChanges();
        }

        if (field.equals("rollback")) {
            if (this.getRollBackChanges() != null && this.getRollBackChanges().length > 0) {
                return this.getRollBackChanges();
            } else {
                return null;
            }
        }

        throw new UnexpectedLiquibaseException("Unexpected field request on changeSet: "+field);
    }

    @Override
    public SerializationType getSerializableFieldType(String field) {
        if (field.equals("comment") || field.equals("changes") || field.equals("rollback")) {
            return SerializationType.NESTED_OBJECT;
//        } else if (field.equals()) {
//            return SerializationType.DIRECT_VALUE;
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
}
