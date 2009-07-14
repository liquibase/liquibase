package liquibase.changelog;

import liquibase.change.Change;
import liquibase.change.CheckSum;
import liquibase.change.core.EmptyChange;
import liquibase.change.core.RawSQLChange;
import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.executor.ExecutorService;
import liquibase.executor.WriteExecutor;
import liquibase.precondition.core.ErrorPrecondition;
import liquibase.precondition.core.FailedPrecondition;
import liquibase.precondition.core.PreconditionContainer;
import liquibase.precondition.Conditional;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.statement.SqlStatement;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;
import liquibase.logging.LogFactory;

import java.util.*;
import java.util.logging.Level;
import liquibase.logging.Logger;

/**
 * Encapsulates a changeSet and all its associated changes.
 */
public class ChangeSet implements Conditional {

    public enum RunStatus {
        NOT_RAN, ALREADY_RAN, RUN_AGAIN, INVALID_MD5SUM
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
        this(id, author, alwaysRun, runOnChange, filePath, physicalFilePath, contextList, dbmsList, false);
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
    public boolean execute(Database database) throws MigrationFailedException {

        boolean skipChange = false;
        boolean markRan = true;

        WriteExecutor writeExecutor = ExecutorService.getInstance().getWriteExecutor(database);
        try {
            database.setAutoCommit(!runInTransaction);


            writeExecutor.comment("Changeset " + toString());
            if (StringUtils.trimToNull(getComments()) != null) {
                String comments = getComments();
                String[] lines = comments.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    if (i > 0) {
                        lines[i] = database.getLineComment() + " " + lines[i];
                    }
                }
                writeExecutor.comment(StringUtils.join(Arrays.asList(lines), "\n"));
            }

            if (writeExecutor.executesStatements() && preconditions != null) {
                try {
                    preconditions.check(database, null);
                } catch (PreconditionFailedException e) {
                    StringBuffer message = new StringBuffer();
                    message.append(StreamUtil.getLineSeparator());
                    for (FailedPrecondition invalid : e.getFailedPreconditions()) {
                        message.append("          ").append(invalid.toString());
                        message.append(StreamUtil.getLineSeparator());
                    }

                    if (preconditions.getOnFail().equals(PreconditionContainer.FailOption.HALT)) {
                        e.printStackTrace();
                        throw new MigrationFailedException(this, message.toString());
                    } else if (preconditions.getOnFail().equals(PreconditionContainer.FailOption.CONTINUE)) {
                        skipChange = true;
                        markRan = false;

                        log.info("Continuing past ChangeSet: " + toString() + " due to precondition failure: " + message);
                    } else if (preconditions.getOnFail().equals(PreconditionContainer.FailOption.MARK_RAN)) {
                        skipChange = true;

                        log.info("Marking ChangeSet: " + toString() + " ran due to precondition failure: " + message);
                    } else if (preconditions.getOnFail().equals(PreconditionContainer.FailOption.WARN)) {
                        log.warning("Running change set despite failed precondition.  ChangeSet: " + toString() + ": " + message);
                    } else {
                        throw new MigrationFailedException(this, "Unexpected precondition onFail attribute: " + preconditions.getOnFail());
                    }
                } catch (PreconditionErrorException e) {
                    StringBuffer message = new StringBuffer();
                    message.append(StreamUtil.getLineSeparator());
                    for (ErrorPrecondition invalid : e.getErrorPreconditions()) {
                        message.append("          ").append(invalid.toString());
                        message.append(StreamUtil.getLineSeparator());
                    }

                    if (preconditions.getOnError().equals(PreconditionContainer.ErrorOption.HALT)) {
                        throw new MigrationFailedException(this, message.toString());
                    } else if (preconditions.getOnError().equals(PreconditionContainer.ErrorOption.CONTINUE)) {
                        skipChange = true;
                        markRan = false;

                        log.info("Continuing past ChangeSet: " + toString() + " due to precondition error: " + message);
                    } else if (preconditions.getOnError().equals(PreconditionContainer.ErrorOption.MARK_RAN)) {
                        skipChange = true;
                        markRan = true;

                        log.info("Marking ChangeSet: " + toString() + " due ran to precondition error: " + message);
                    } else if (preconditions.getOnError().equals(PreconditionContainer.ErrorOption.WARN)) {
                        log.warning("Running change set despite errored precondition.  ChangeSet: " + toString() + ": " + message);
                    } else {
                        throw new MigrationFailedException(this, "Unexpected precondition onError attribute: " + preconditions.getOnError());
                    }

                    database.rollback();
                }
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
                    database.executeStatements(change, sqlVisitors);
                    log.debug(change.getConfirmationMessage());
                }

                if (runInTransaction) {
                    database.commit();
                }
                log.debug("ChangeSet " + toString() + " has been successfully run.");
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
                log.info("Change set " + toString(false) + " failed, but failOnError was false", e);
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
                    throw new MigrationFailedException(this, "Could not reset autocommit");
                }
            }
        }
        return markRan;
    }

    public void rolback(Database database) throws RollbackFailedException {
        try {
            WriteExecutor writeExecutor = ExecutorService.getInstance().getWriteExecutor(database);
            writeExecutor.comment("Rolling Back ChangeSet: " + toString());
            if (rollBackChanges != null && rollBackChanges.size() > 0) {
                for (Change rollback : rollBackChanges) {
                    for (SqlStatement statement : rollback.generateStatements(database)) {
                        try {
                            writeExecutor.execute(statement, sqlVisitors);
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
                ;
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
        return filePath + "::" + getId() + "::" + getAuthor() + (includeMD5Sum ? ("::(MD5Sum: " + generateCheckSum() + ")") : "");
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
}
