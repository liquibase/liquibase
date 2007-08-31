package liquibase.migrator;

import liquibase.database.DatabaseConnection;
import liquibase.migrator.change.Change;
import liquibase.migrator.exception.MigrationFailedException;
import liquibase.migrator.exception.RollbackFailedException;
import liquibase.migrator.exception.SetupException;
import liquibase.util.MD5Util;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Logger;

/**
 * Encapsulates a changeSet and all its associated changes.
 */
public class ChangeSet {

    public enum RunStatus { NOT_RAN, ALREADY_RAN, RUN_AGAIN, INVALID_MD5SUM }

    private List<Change> changes;
    private String id;
    private String author;
    private DatabaseChangeLog databaseChangeLog;
    private Logger log;
    private String md5sum;
    private boolean alwaysRun;
    private boolean runOnChange;
    private String context;
    private Set<String> dbmsSet;

    private String[] rollBackStatements;

    private String comments;

    public boolean shouldAlwaysRun() {
        return alwaysRun;
    }

    public boolean shouldRunOnChange() {
        return runOnChange;
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, DatabaseChangeLog databaseChangeLog, String context, String dbmsList) {
        this.changes = new ArrayList<Change>();
        log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);
        this.id = id;
        this.author = author;
        this.databaseChangeLog = databaseChangeLog;
        this.alwaysRun = alwaysRun;
        this.runOnChange = runOnChange;
        if (context != null) {
            this.context = context.trim().toLowerCase();
        }
        if (StringUtils.trimToNull(dbmsList) != null) {
            String[] strings = dbmsList.split(",");
            for (String string : strings) {
                if (dbmsSet == null) {
                    dbmsSet = new HashSet<String>();
                }
                dbmsSet.add(string.trim().toLowerCase());
            }
        }
    }

    public DatabaseChangeLog getDatabaseChangeLog() {
        return databaseChangeLog;
    }

    public void setDatabaseChangeLog(DatabaseChangeLog databaseChangeLog) {
        this.databaseChangeLog = databaseChangeLog;
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
    public void execute() throws MigrationFailedException {
        
        for(Change change : changes) {
            try {
                change.setUp();
            } catch(SetupException se) {
                throw new MigrationFailedException(this, se);
            }
        }
        
        Migrator migrator = getDatabaseChangeLog().getMigrator();
        DatabaseConnection connection = migrator.getDatabase().getConnection();
        try {
            Writer outputSQLWriter = getDatabaseChangeLog().getMigrator().getOutputSQLWriter();
            if (migrator.getMode().equals(Migrator.Mode.EXECUTE_MODE)) {
                log.finest("Reading ChangeSet: " + toString());
                for (Change change : getChanges()) {
                    change.executeStatements(migrator.getDatabase());
                    log.finest(change.getConfirmationMessage());
                }

                connection.commit();
                log.finest("ChangeSet " + toString() + " has been successfully ran.");
            } else if (migrator.getMode().equals(Migrator.Mode.OUTPUT_SQL_MODE)) {
                outputSQLWriter.write("-- Changeset " + toString() + StreamUtil.getLineSeparator());
                writeComments(outputSQLWriter);
                for (Change change : getChanges()) {
                    change.saveStatements(getDatabaseChangeLog().getMigrator().getDatabase(), outputSQLWriter);
                }
//                outputSQLWriter.write(getDatabaseChangeLog().getMigrator().getDatabase().getCommitSQL()+";"+StreamUtil.getLineSeparator()+StreamUtil.getLineSeparator());
            } else if (migrator.getMode().equals(Migrator.Mode.EXECUTE_ROLLBACK_MODE)) {
                log.finest("Rolling Back ChangeSet: " + toString());
                if (rollBackStatements != null && rollBackStatements.length > 0) {
                    Statement statement = connection.createStatement();
                    for (String rollback : rollBackStatements) {
                        try {
                            statement.execute(rollback);
                        } catch (SQLException e) {
                            throw new RollbackFailedException("Error executing custom SQL [" + rollback + "]");
                        }
                    }
                    statement.close();

                } else {
                    List<Change> changes = getChanges();
                    for (int i = changes.size() - 1; i >= 0; i--) {
                        Change change = changes.get(i);
                        change.executeRollbackStatements(migrator.getDatabase());
                        log.finest(change.getConfirmationMessage());
                    }
                }

                connection.commit();
                log.finest("ChangeSet " + toString() + " has been successfully rolled back.");
            } else
            if (migrator.getMode().equals(Migrator.Mode.OUTPUT_ROLLBACK_SQL_MODE) || migrator.getMode().equals(Migrator.Mode.OUTPUT_FUTURE_ROLLBACK_SQL_MODE))
            {
                outputSQLWriter.write("-- Changeset " + toString() + StreamUtil.getLineSeparator());
                writeComments(outputSQLWriter);
                if (rollBackStatements != null && rollBackStatements.length > 0) {
                    for (String statement : rollBackStatements) {
                        outputSQLWriter.append(statement).append(";").append(StreamUtil.getLineSeparator()).append(StreamUtil.getLineSeparator());
                    }
                } else {
                    for (int i = changes.size() - 1; i >= 0; i--) {
                        Change change = changes.get(i);
                        change.saveRollbackStatement(getDatabaseChangeLog().getMigrator().getDatabase(), outputSQLWriter);
                    }
                }
            } else if (migrator.getMode().equals(Migrator.Mode.OUTPUT_CHANGELOG_ONLY_SQL_MODE)) {
                //don't need to do anything
            } else {
                throw new MigrationFailedException(this, "Unexpected mode: " + migrator.getMode());
            }
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                throw new MigrationFailedException(this, e);
            }
            throw new MigrationFailedException(this, e);
        }
    }

    private void writeComments(Writer writer) throws IOException {
        if (StringUtils.trimToNull(comments) != null) {
            String[] commentLines = comments.split("\n");
            for (String line : commentLines) {
                writer.append("-- ").append(line.trim()).append(StreamUtil.getLineSeparator());
            }
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

    public String getContext() {
        return context;
    }

    public Set<String> getDbmsSet() {
        return dbmsSet;
    }

    public String toString(boolean includeMD5Sum) {
        return getDatabaseChangeLog().getFilePath() + " :: " + getId() + " :: " + getAuthor() + (includeMD5Sum?(" :: (MD5Sum: " + getMd5sum() + ")"):"");
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

        if (StringUtils.trimToNull(getContext()) != null) {
            node.setAttribute("context", StringUtils.trimToEmpty(getContext()));
        }

        if (getDbmsSet() != null && getDbmsSet().size() > 0) {
            StringBuffer dbmsString = new StringBuffer();
            for (String dbms : getDbmsSet()) {
                dbmsString.append(dbms).append(",");
            }
            node.setAttribute("dbms", dbmsString.toString().replaceFirst(",$",""));
        }

        if (StringUtils.trimToNull(getComments()) != null) {
            Element commentsElement = currentChangeLogDOM.createElement("comments");
            Text commentsText = currentChangeLogDOM.createTextNode(getComments());
            commentsElement.appendChild(commentsText);
            node.appendChild(commentsElement);
        }


        for (Change change : getChanges()) {
            node.appendChild(change.createNode(currentChangeLogDOM));
        }
        return node;
    }

    public void setRollBackSQL(String sql) {
        if (sql == null) {
            return;
        }
        this.rollBackStatements = sql.split(";");
        for (int i = 0; i < rollBackStatements.length; i++) {
            rollBackStatements[i] = rollBackStatements[i].trim();
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
