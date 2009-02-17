package liquibase;

import liquibase.change.Change;
import liquibase.change.RawSQLChange;
import liquibase.change.EmptyChange;
import liquibase.database.Database;
import liquibase.database.sql.SqlStatement;
import liquibase.database.sql.visitor.SqlVisitor;
import liquibase.exception.*;
import liquibase.log.LogFactory;
import liquibase.util.MD5Util;
import liquibase.util.StringUtils;
import liquibase.util.StreamUtil;
import liquibase.preconditions.FailedPrecondition;
import liquibase.preconditions.Preconditions;
import liquibase.preconditions.ErrorPrecondition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Encapsulates a changeSet and all its associated changes.
 */
public class ChangeSet {

    public enum RunStatus {
        NOT_RAN, ALREADY_RAN, RUN_AGAIN, INVALID_MD5SUM
    }

    private List<Change> changes;
    private String id;
    private String author;
    private String filePath = "UNKNOWN CHANGE LOG";
    private String physicalFilePath;
    private Logger log;
    private String md5sum;
    private boolean alwaysRun;
    private boolean runOnChange;
    private Set<String> contexts;
    private Set<String> dbmsSet;
    private Boolean failOnError;
    private Set<String> validCheckSums = new HashSet<String>();
    private boolean runInTransaction;

    private List<Change> rollBackChanges = new ArrayList<Change>();

    private String comments;

    private Preconditions rootPrecondition;

    private List<SqlVisitor> sqlVisitors = new ArrayList<SqlVisitor>();

    public boolean shouldAlwaysRun() {
        return alwaysRun;
    }

    public boolean shouldRunOnChange() {
        return runOnChange;
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, String filePath, String physicalFilePath, String contextList, String dbmsList) {
        this(id, author,  alwaysRun, runOnChange, filePath, physicalFilePath, contextList,  dbmsList, false);
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

    public String getMd5sum() {
        if (md5sum == null) {
            StringBuffer stringToMD5 = new StringBuffer();
            for (Change change : getChanges()) {
                stringToMD5.append(change.getMD5Sum()).append(":");
            }

            md5sum = MD5Util.computeMD5(stringToMD5.toString());
        }
        return md5sum;
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

        try {
            database.setAutoCommit(!runInTransaction);

            database.getJdbcTemplate().comment("Changeset " + toString());
            if (StringUtils.trimToNull(getComments()) != null) {
                String comments = getComments();
                String[] lines = comments.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    if (i > 0) {
                        lines[i] = database.getLineComment() + " " + lines[i];
                    }
                }
                database.getJdbcTemplate().comment(StringUtils.join(Arrays.asList(lines), "\n"));
            }

            if (database.getJdbcTemplate().executesStatements() && rootPrecondition != null) {
                try {
                    rootPrecondition.check(database, null);
                } catch (PreconditionFailedException e) {
                    StringBuffer message = new StringBuffer();
                    message.append(StreamUtil.getLineSeparator());
                    for (FailedPrecondition invalid : e.getFailedPreconditions()) {
                        message.append("          ").append(invalid.toString());
                        message.append(StreamUtil.getLineSeparator());
                    }

                    if (rootPrecondition.getOnFail().equals(Preconditions.FailOption.HALT)) {
                        e.printStackTrace();
                        throw new MigrationFailedException(this, message.toString());
                    } else if (rootPrecondition.getOnFail().equals(Preconditions.FailOption.CONTINUE)) {
                        skipChange = true;
                        markRan = false;

                        log.log(Level.INFO, "Continuing past ChangeSet: " + toString() +" due to precondition failure: " + message);
                    } else if (rootPrecondition.getOnFail().equals(Preconditions.FailOption.MARK_RAN)) {
                        skipChange = true;

                        log.log(Level.INFO, "Marking ChangeSet: " + toString() +" ran due to precondition failure: " + message);
                    } else if (rootPrecondition.getOnFail().equals(Preconditions.FailOption.WARN)) {
                        log.log(Level.WARNING, "Running change set despite failed precondition.  ChangeSet: " + toString() + ": " + message);
                    } else {
                        throw new MigrationFailedException(this, "Unexpected precondition onFail attribute: "+rootPrecondition.getOnFail());
                    }
                } catch (PreconditionErrorException e) {
                    StringBuffer message = new StringBuffer();
                    message.append(StreamUtil.getLineSeparator());
                    for (ErrorPrecondition invalid : e.getErrorPreconditions()) {
                        message.append("          ").append(invalid.toString());
                        message.append(StreamUtil.getLineSeparator());
                    }

                    if (rootPrecondition.getOnError().equals(Preconditions.ErrorOption.HALT)) {
                        throw new MigrationFailedException(this, message.toString());
                    } else if (rootPrecondition.getOnError().equals(Preconditions.ErrorOption.CONTINUE)) {
                        skipChange = true;
                        markRan = false;

                        log.log(Level.INFO, "Continuing past ChangeSet: " + toString() +" due to precondition error: " + message);
                    } else if (rootPrecondition.getOnError().equals(Preconditions.ErrorOption.MARK_RAN)) {
                        skipChange = true;
                        markRan = true;

                        log.log(Level.INFO, "Marking ChangeSet: " + toString() +" due ran to precondition error: " + message);
                    } else if (rootPrecondition.getOnError().equals(Preconditions.ErrorOption.WARN)) {
                        log.log(Level.WARNING, "Running change set despite errored precondition.  ChangeSet: " + toString() + ": " + message);
                    } else {
                        throw new MigrationFailedException(this, "Unexpected precondition onError attribute: "+rootPrecondition.getOnError());
                    }

                    database.rollback();
                }
            }

            if (!skipChange) {
                for (Change change : changes) {
                    try {
                        change.setUp();
                    } catch (SetupException se) {
                        throw new MigrationFailedException(this, se);
                    }
                }

                log.finest("Reading ChangeSet: " + toString());
                for (Change change : getChanges()) {
                    change.executeStatements(database, sqlVisitors);
                    log.finest(change.getConfirmationMessage());
                }

                if (runInTransaction) {
                    database.commit();
                }
                log.finest("ChangeSet " + toString() + " has been successfully run.");
            } else {
                log.finest("Skipping ChangeSet: " + toString());
            }

        } catch (Exception e) {
            try {
                database.rollback();
            } catch (Exception e1) {
                throw new MigrationFailedException(this, e);
            }
            if (getFailOnError() != null && !getFailOnError()) {
                log.log(Level.INFO, "Change set " + toString(false) + " failed, but failOnError was false", e);
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
                } catch (JDBCException e) {
                    throw new MigrationFailedException(this, "Could not reset autocommit");
                }
            }
        }
        return markRan;
    }

    public void rolback(Database database) throws RollbackFailedException {
        try {
            database.getJdbcTemplate().comment("Rolling Back ChangeSet: " + toString());
            if (rollBackChanges != null && rollBackChanges.size() > 0) {
                for (Change rollback : rollBackChanges) {
                    for (SqlStatement statement : rollback.generateStatements(database)) {
                        try {
                            database.getJdbcTemplate().execute(statement, sqlVisitors);
                        } catch (JDBCException e) {
                            throw new RollbackFailedException("Error executing custom SQL [" + statement + "]", e);
                        }
                    }
                }

            } else {
                List<Change> changes = getChanges();
                for (int i = changes.size() - 1; i >= 0; i--) {
                    Change change = changes.get(i);
                    change.executeRollbackStatements(database, sqlVisitors);
                    log.finest(change.getConfirmationMessage());
                }
            }

            database.commit();
            log.finest("ChangeSet " + toString() + " has been successfully rolled back.");
        } catch (Exception e) {
            try {
                database.rollback();
            } catch (JDBCException e1) {
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
        return filePath + "::" + getId() + "::" + getAuthor() + (includeMD5Sum ? ("::(MD5Sum: " + getMd5sum() + ")") : "");
    }

    public String toString() {
        return toString(true);
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Element createNode(Document currentChangeLogDOM) {
        Element node = currentChangeLogDOM.createElement("changeSet");
        node.setAttribute("id", getId());
        node.setAttribute("author", getAuthor());

        if (alwaysRun) {
            node.setAttribute("alwaysRun", "true");
        }

        if (runOnChange) {
            node.setAttribute("runOnChange", "true");
        }

        if (failOnError != null) {
            node.setAttribute("failOnError", failOnError.toString());
        }

        if (getContexts() != null && getContexts().size() > 0) {
            StringBuffer contextString = new StringBuffer();
            for (String context : getContexts()) {
                contextString.append(context).append(",");
            }
            node.setAttribute("context", contextString.toString().replaceFirst(",$", ""));
        }

        if (getDbmsSet() != null && getDbmsSet().size() > 0) {
            StringBuffer dbmsString = new StringBuffer();
            for (String dbms : getDbmsSet()) {
                dbmsString.append(dbms).append(",");
            }
            node.setAttribute("dbms", dbmsString.toString().replaceFirst(",$", ""));
        }

        if (StringUtils.trimToNull(getComments()) != null) {
            Element commentsElement = currentChangeLogDOM.createElement("comment");
            Text commentsText = currentChangeLogDOM.createTextNode(getComments());
            commentsElement.appendChild(commentsText);
            node.appendChild(commentsElement);
        }


        for (Change change : getChanges()) {
            node.appendChild(change.createNode(currentChangeLogDOM));
        }
        return node;
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


    public boolean canRollBack() {
        if (rollBackChanges != null && rollBackChanges.size() > 0) {
            return true;
        }

        for (Change change : getChanges()) {
            if (!change.canRollBack()) {
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
                returnString.append(change.getChangeName());
                changeCount = 1;
            } else {
                returnString.append(", ").append(change.getChangeName());
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
        validCheckSums.add(text);
    }

    public boolean isCheckSumValid(String storedCheckSum) {
        String currentMd5Sum = getMd5sum();
        if (currentMd5Sum == null) {
            return true;
        }
        if (storedCheckSum == null) {
            return true;
        }
        if (currentMd5Sum.equals(storedCheckSum)) {
            return true;
        }

        for (String validCheckSum : validCheckSums) {
            if (currentMd5Sum.equals(validCheckSum)) {
                return true;
            }
        }
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public Preconditions getPrecondition() {
        return rootPrecondition;
    }

    public void setPreconditions(Preconditions preconditions) {
        this.rootPrecondition = preconditions;
    }

    public void addSqlVisitor(SqlVisitor sqlVisitor) {
        sqlVisitors.add(sqlVisitor);
    }
}
