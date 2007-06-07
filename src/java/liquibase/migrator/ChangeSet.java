package liquibase.migrator;

import liquibase.migrator.change.Change;
import liquibase.util.MD5Util;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Encapsulates a changeSet and all its associated changes.
 */
public class ChangeSet {

    public enum RunStatus {
        NOT_RAN, ALREADY_RAN, RUN_AGAIN }

    private List<Change> changes;
    private String id;
    private String author;
    private DatabaseChangeLog databaseChangeLog;
    private Logger log;
    private String md5sum;
    private boolean alwaysRun;
    private boolean runOnChange;
    private String context;

    private String[] rollBackStatements;

    private String comments;

    public boolean shouldAlwaysRun() {
        return alwaysRun;
    }

    public boolean shouldRunOnChange() {
        return runOnChange;
    }

    public ChangeSet(String id, String author, boolean alwaysRun, boolean runOnChange, DatabaseChangeLog databaseChangeLog, String context) {
        this.changes = new ArrayList<Change>();
        log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);
        this.id = id;
        this.author = author;
        this.databaseChangeLog = databaseChangeLog;
        this.alwaysRun = alwaysRun;
        this.runOnChange = runOnChange;
        if (context != null) {
            this.context = context.toLowerCase();
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
     * This method will actually execute each of the refactoring in the list against the
     * specified database.
     */
    public void execute() throws DatabaseHistoryException, MigrationFailedException {
        Migrator migrator = getDatabaseChangeLog().getMigrator();
        Connection connection = migrator.getDatabase().getConnection();
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
                    List<Change> refactorings = getChanges();
                    for (int i = refactorings.size() - 1; i >= 0; i--) {
                        Change change = refactorings.get(i);
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
                        outputSQLWriter.append(statement + ";" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator());
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
                throw new MigrationFailedException("Unexpected mode: " + migrator.getMode());
            }
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                throw new MigrationFailedException("Unable to process change set: " + toString() + ": " + e.getMessage(), e);
            }
            throw new MigrationFailedException("Unable to process change set: " + toString() + ": " + e.getMessage(), e);
        }
    }

    private void writeComments(Writer writer) throws IOException {
        if (StringUtils.trimToNull(comments) != null) {
            String[] commentLines = comments.split("\n");
            for (String line : commentLines) {
                writer.append("-- " + line.trim() + StreamUtil.getLineSeparator());
            }
        }
    }

    /**
     * Returns an unmodifiable list of changes.  To add one, use the addRefactoing method.
     */
    public List<Change> getChanges() {
        return Collections.unmodifiableList(changes);
    }

    public void addRefactoring(Change change) {
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

    public String toString() {
        return getDatabaseChangeLog().getFilePath() + " :: " + getId() + " :: " + getAuthor() + " :: (MD5Sum: " + getMd5sum() + ")";
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Node createNode(Document currentChangeLogDOM) {
        Element node = currentChangeLogDOM.createElement("changeSet");
        node.setAttribute("id", getId());
        node.setAttribute("author", getAuthor());
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
        List<Change> refactorings = getChanges();
        if (refactorings.size() == 0) {
            return "Empty";
        }

        StringBuffer returnString = new StringBuffer();
        Class lastRefactoringClass = null;
        int refactoringCount = 0;
        for (Change change : refactorings) {
            if (change.getClass().equals(lastRefactoringClass)) {
                refactoringCount++;
            } else if (refactoringCount > 1) {
                returnString.append(" (x").append(refactoringCount).append(")");
                returnString.append(", ");
                returnString.append(change.getRefactoringName());
                refactoringCount = 1;
            } else {
                returnString.append(", ").append(change.getRefactoringName());
                refactoringCount = 1;
            }
            lastRefactoringClass = change.getClass();
        }

        if (refactoringCount > 1) {
            returnString.append(" (x").append(refactoringCount).append(")");
        }

        return returnString.toString().replaceFirst("^, ", "");
    }
}
