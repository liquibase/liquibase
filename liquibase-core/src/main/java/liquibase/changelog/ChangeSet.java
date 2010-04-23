package liquibase.changelog;

import liquibase.change.Change;
import liquibase.change.CheckSum;
import liquibase.change.core.EmptyChange;
import liquibase.change.core.RawSQLChange;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.precondition.Conditional;
import liquibase.precondition.core.ErrorPrecondition;
import liquibase.precondition.core.FailedPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.util.*;

/**
 * Encapsulates a changeSet and all its associated changes.
 */
public class ChangeSet implements Conditional {

    public enum RunStatus {
        NOT_RAN, ALREADY_RAN, RUN_AGAIN, MARK_RAN, INVALID_MD5SUM
    }

    public enum ExecType {
        EXECUTED("EXECUTED", false),
        FAILED("EXECUTED", false),
        SKIPPED("SKIPPED", false),
        RERAN("RERAN", true),
        MARK_RAN("MARK_RAN", false);

        ExecType(String value, boolean ranBefore) {
            this.value = value;
            this.ranBefore = ranBefore;
        }

        public String value;
        public boolean ranBefore;
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

    private List<Change> changes;
    private String id;
    private String author;
    private String filePath = "UNKNOWN CHANGE LOG";
    private String physicalFilePath;
    private Logger log;
    private boolean alwaysRun;
    private boolean runOnChange;
    private Set<String> contexts;
    private Set<String> dbmsSet;
    private Boolean failOnError;
    private Set<CheckSum> validCheckSums = new HashSet<CheckSum>();
    private boolean runInTransaction;
    private ValidationFailOption onValidationFail = ValidationFailOption.HALT;
    private boolean validationFailed;

    private List<Change> rollBackChanges = new ArrayList<Change>();

    private String comments;

    private PreconditionContainer preconditions;

    private List<SqlVisitor> sqlVisitors = new ArrayList<SqlVisitor>();

    public boolean shouldAlwaysRun() {
        return alwaysRun;
    }

    public boolean shouldRunOnChange() {
        return runOnChange;
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, String filePath, String physicalFilePath, String contextList, String dbmsList) {
        this(id, author, alwaysRun, runOnChange, filePath, physicalFilePath, contextList, dbmsList, true);
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, String filePath, String physicalFilePath, String contextList, String dbmsList, boolean runInTransaction) {
        this.changes = new ArrayList<Change>();
        log = LogFactory.getLogger();
        this.id = id;
        this.author = author;
        this.filePath = filePath;
        this.physicalFilePath = physicalFilePath;
        this.alwaysRun = alwaysRun;
        this.runOnChange = runOnChange;
        this.runInTransaction = runInTransaction;
        if (StringUtils.trimToNull(contextList) != null) {
            String[] strings = contextList.toLowerCase().split(",");
            contexts = new HashSet<String>();
            for (String string : strings) {
                contexts.add(string.trim().toLowerCase());
            }
        }
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

    public String getPhysicalFilePath() {
        if (physicalFilePath == null) {
            return filePath;
        } else {
            return physicalFilePath;
        }
    }

    public CheckSum generateCheckSum() {
            StringBuffer stringToMD5 = new StringBuffer();
            for (Change change : getChanges()) {
                stringToMD5.append(change.generateCheckSum()).append(":");
            }

            return CheckSum.compute(stringToMD5.toString());
    }

    /**
     * This method will actually execute each of the changes in the list against the
     * specified database.
     *
     * @return should change set be marked as ran
     */
    public ExecType execute(DatabaseChangeLog databaseChangeLog, Database database) throws MigrationFailedException {
        if (validationFailed) {
            return ExecType.MARK_RAN;
        }

        long startTime = new Date().getTime();

        ExecType execType = null;

        boolean skipChange = false;

        Executor executor = ExecutorService.getInstance().getExecutor(database);
        try {
            database.setAutoCommit(!runInTransaction);


            executor.comment("Changeset " + toString());
            if (StringUtils.trimToNull(getComments()) != null) {
                String comments = getComments();
                String[] lines = comments.split("\n");
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
                } else if (preconditions.getOnFail().equals(PreconditionContainer.FailOption.MARK_RAN)) {
                    execType = ExecType.MARK_RAN;
                    skipChange = true;

                    log.info("Marking ChangeSet: " + toString() + " ran despite precondition failure: " + message);
                } else if (preconditions.getOnFail().equals(PreconditionContainer.FailOption.WARN)) {
                    execType = null; //already warned
                } else {
                    throw new UnexpectedLiquibaseException("Unexpected precondition onFail attribute: " + preconditions.getOnFail(), e);
                }
            } catch (PreconditionErrorException e) {
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
            }

            if (!skipChange) {
                for (Change change : changes) {
                    try {
                        change.init();
                    } catch (SetupException se) {
                        throw new MigrationFailedException(this, se);
                    }
                }

                log.debug("Reading ChangeSet: " + toString());
                for (Change change : getChanges()) {
                    database.executeStatements(change, databaseChangeLog, sqlVisitors);
                    log.debug(change.getConfirmationMessage());
                }

                if (runInTransaction) {
                    database.commit();
                }
                log.info("ChangeSet " + toString(false) + " ran successfully in "+(new Date().getTime()-startTime+"ms"));
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
                log.info("Change set " + toString(false) + " failed, but failOnError was false.  Error: "+e.getMessage());
                log.debug("Failure Stacktrace", e);
                execType = ExecType.FAILED;
            } else {
                if (e instanceof MigrationFailedException) {
                    throw ((MigrationFailedException) e);
                } else {
                    throw new MigrationFailedException(this, e);
                }
            }
        } finally {
            if (!runInTransaction && !database.getAutoCommitMode()) {
                try {
                    database.setAutoCommit(false);
                } catch (DatabaseException e) {
                    throw new MigrationFailedException(this, "Could not reset autocommit", e);
                }
            }
        }
        return execType;
    }

    public void rolback(Database database) throws RollbackFailedException {
        try {
            Executor executor = ExecutorService.getInstance().getExecutor(database);
            executor.comment("Rolling Back ChangeSet: " + toString());
            if (rollBackChanges != null && rollBackChanges.size() > 0) {
                for (Change rollback : rollBackChanges) {
                    for (SqlStatement statement : rollback.generateStatements(database)) {
                        try {
                            executor.execute(statement, sqlVisitors);
                        } catch (DatabaseException e) {
                            throw new RollbackFailedException("Error executing custom SQL [" + statement + "]", e);
                        }
                    }
                }

            } else {
                List<Change> changes = getChanges();
                for (int i = changes.size() - 1; i >= 0; i--) {
                    Change change = changes.get(i);
                    database.executeRollbackStatements(change, sqlVisitors);
                    log.debug(change.getConfirmationMessage());
                }
            }

            database.commit();
            log.debug("ChangeSet " + toString() + " has been successfully rolled back.");
        } catch (Exception e) {
            try {
                database.rollback();
            } catch (DatabaseException e1) {
                //ok
            }
            throw new RollbackFailedException(e);
        }

    }

    /**
     * Returns an unmodifiable list of changes.  To add one, use the addRefactoing method.
     */
    public List<Change> getChanges() {
        return Collections.unmodifiableList(changes);
    }

    public void addChange(Change change) {
        changes.add(change);
        change.setChangeSet(this);
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public Set<String> getContexts() {
        return contexts;
    }

    public Set<String> getDbmsSet() {
        return dbmsSet;
    }

    public String toString(boolean includeMD5Sum) {
        return filePath + "::" + getId() + "::" + getAuthor() + (includeMD5Sum ? ("::(Checksum: " + generateCheckSum() + ")") : "");
    }

    @Override
    public String toString() {
        return toString(true);
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
            rollBackChanges.add(new EmptyChange());
            return;
        }

        for (String statment : StringUtils.splitSQL(sql)) {
            rollBackChanges.add(new RawSQLChange(statment.trim()));
        }
    }

    public void addRollbackChange(Change change) throws UnsupportedChangeException {
        rollBackChanges.add(change);
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
                returnString.append(change.getChangeMetaData().getDescription());
                changeCount = 1;
            } else {
                returnString.append(", ").append(change.getChangeMetaData().getDescription());
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

    public boolean isCheckSumValid(CheckSum storedCheckSum) {
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

    public PreconditionContainer getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(PreconditionContainer preconditionContainer) {
        this.preconditions = preconditionContainer;
    }

    public void addSqlVisitor(SqlVisitor sqlVisitor) {
        sqlVisitors.add(sqlVisitor);
    }

    public List<SqlVisitor> getSqlVisitors() {
        return sqlVisitors;
    }        
}
