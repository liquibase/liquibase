package liquibase;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.JDBCException;
import liquibase.exception.MigrationFailedException;
import liquibase.exception.RollbackFailedException;
import liquibase.exception.SetupException;
import liquibase.log.LogFactory;
import liquibase.util.MD5Util;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.logging.Logger;

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

    private SqlStatement[] rollBackStatements;

    private String comments;

    public boolean shouldAlwaysRun() {
        return alwaysRun;
    }

    public boolean shouldRunOnChange() {
        return runOnChange;
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, String filePath, String physicalFilePath, String contextList, String dbmsList) {
        this.changes = new ArrayList<Change>();
        log = LogFactory.getLogger();
        this.id = id;
        this.author = author;
        this.filePath = filePath;
        this.physicalFilePath = physicalFilePath;
        this.alwaysRun = alwaysRun;
        this.runOnChange = runOnChange;
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
     */

    public void execute(Database database) throws MigrationFailedException {

        try {
            database.getJdbcTemplate().comment("Changeset " + toString());
            if (StringUtils.trimToNull(getComments()) != null) {
                String comments = getComments();
                String[] lines = comments.split("\n");
                for (int i=0; i<lines.length; i++) {
                    if (i > 0) {
                        lines[i] = database.getLineComment()+" "+lines[i];
                    }
                }
                database.getJdbcTemplate().comment(StringUtils.join(Arrays.asList(lines), "\n"));
            }

            for (Change change : changes) {
                try {
                    change.setUp();
                } catch (SetupException se) {
                    throw new MigrationFailedException(this, se);
                }
            }

            log.finest("Reading ChangeSet: " + toString());
            for (Change change : getChanges()) {
                change.executeStatements(database);
                log.finest(change.getConfirmationMessage());
            }

            database.commit();
            log.finest("ChangeSet " + toString() + " has been successfully ran.");

            database.commit();
        } catch (Exception e) {
            try {
                database.rollback();
            } catch (Exception e1) {
                throw new MigrationFailedException(this, e);
            }
            throw new MigrationFailedException(this, e);
        }
    }

    public void rolback(Database database) throws RollbackFailedException {
        try {
            database.getJdbcTemplate().comment("Rolling Back ChangeSet: " + toString());
            if (rollBackStatements != null && rollBackStatements.length > 0) {
                for (SqlStatement rollback : rollBackStatements) {
                    try {
                        database.getJdbcTemplate().execute(rollback);
                    } catch (JDBCException e) {
                        throw new RollbackFailedException("Error executing custom SQL [" + rollback.getSqlStatement(database) + "]", e);
                    }
                }

            } else {
                List<Change> changes = getChanges();
                for (int i = changes.size() - 1; i >= 0; i--) {
                    Change change = changes.get(i);
                    change.executeRollbackStatements(database);
                    log.finest(change.getConfirmationMessage());
                }
            }

            database.commit();
            log.finest("ChangeSet " + toString() + " has been successfully rolled back.");
        } catch (Exception e) {
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
        return filePath + " :: " + getId() + " :: " + getAuthor() + (includeMD5Sum ? (" :: (MD5Sum: " + getMd5sum() + ")") : "");
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

    public SqlStatement[] getRollBackStatements() {
        return rollBackStatements;
    }

    public void setRollBackSQL(String sql) {
        if (sql == null) {
            return;
        }
        String[] sqlStatements = sql.split(";");
        this.rollBackStatements = new SqlStatement[sqlStatements.length];

        for (int i = 0; i < rollBackStatements.length; i++) {
            rollBackStatements[i] = new RawSqlStatement(sqlStatements[i].trim());
        }
    }

    public boolean canRollBack() {
        if (rollBackStatements != null && rollBackStatements.length > 0) {
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
}
