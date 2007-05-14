package liquibase.migrator;

import liquibase.migrator.change.AbstractChange;
import liquibase.util.StringUtils;
import liquibase.StreamUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.io.Writer;
import java.io.IOException;

/**
 * This class will serve the purpose of keeping track of the statements.
 * When statements element is encountered in the XML file, this class will
 * be invoked and the child elements of the statements tag will be added to
 * the arraylist as Statement objects.
 */
public class ChangeSet {

    public enum RunStatus {
        NOT_RAN, ALREADY_RAN, RUN_AGAIN }

    private List<AbstractChange> refactorings;
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
        this.refactorings = new ArrayList<AbstractChange>();
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
            for (AbstractChange change : getRefactorings()) {
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
            if (migrator.getMode().equals(Migrator.EXECUTE_MODE)) {
                log.finest("Reading ChangeSet: " + toString());
                for (AbstractChange change : getRefactorings()) {
                    change.executeStatements(migrator.getDatabase());
                    log.finest(change.getConfirmationMessage());
                }

                connection.commit();
                log.finest("ChangeSet " + toString() + " has been successfully ran.");
            } else if (migrator.getMode().equals(Migrator.OUTPUT_SQL_MODE)) {
                outputSQLWriter.write("-- Changeset " + toString() + StreamUtil.getLineSeparator());
                writeComments(outputSQLWriter);
                for (AbstractChange change : getRefactorings()) {
                    change.saveStatement(getDatabaseChangeLog().getMigrator().getDatabase(), outputSQLWriter);
                }
//                outputSQLWriter.write(getDatabaseChangeLog().getMigrator().getDatabase().getCommitSQL()+";"+StreamUtil.getLineSeparator()+StreamUtil.getLineSeparator());
            } else if (migrator.getMode().equals(Migrator.EXECUTE_ROLLBACK_MODE)) {
                log.finest("Rolling Back ChangeSet: " + toString());
                if (rollBackStatements != null && rollBackStatements.length > 0) {
                    Statement statement = connection.createStatement();
                    for (String rollback : rollBackStatements) {
                        try {
                            statement.execute(rollback);
                        } catch (SQLException e) {
                            throw new RollbackFailedException("Error executing custom SQL ["+rollback+"]");
                        }
                    }
                    statement.close();

                } else {
                    List<AbstractChange> refactorings = getRefactorings();
                    for (int i=refactorings.size()-1; i>=0; i--) {
                        AbstractChange change = refactorings.get(i);
                        change.executeRollbackStatements(migrator.getDatabase());
                        log.finest(change.getConfirmationMessage());
                    }
                }

                connection.commit();
                log.finest("ChangeSet " + toString() + " has been successfully rolled back.");
            } else if (migrator.getMode().equals(Migrator.OUTPUT_ROLLBACK_SQL_MODE) || migrator.getMode().equals(Migrator.OUTPUT_FUTURE_ROLLBACK_SQL_MODE)) {
                outputSQLWriter.write("-- Changeset " + toString() + StreamUtil.getLineSeparator());
                writeComments(outputSQLWriter);
                if (rollBackStatements != null && rollBackStatements.length > 0) {
                    for (String statement : rollBackStatements) {
                        outputSQLWriter.append(statement + ";"+StreamUtil.getLineSeparator()+StreamUtil.getLineSeparator());
                    }
                } else {
                    for (int i=refactorings.size()-1; i>=0; i--) {
                        AbstractChange change = refactorings.get(i);
                        change.saveRollbackStatement(getDatabaseChangeLog().getMigrator().getDatabase(), outputSQLWriter);
                    }
                }
            } else if (migrator.getMode().equals(Migrator.OUTPUT_CHANGELOG_ONLY_SQL_MODE)) {
                //don't need to do anything
            } else {
                throw new MigrationFailedException("Unexpected mode: "+migrator.getMode());
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
                writer.append("-- "+line.trim()+StreamUtil.getLineSeparator());
            }
        }
    }

    public void rollback() {
    }


    /**
     * Returns an unmodifiable list of refactorings.  To add one, use the addRefactoing method.
     */
    public List<AbstractChange> getRefactorings() {
        return Collections.unmodifiableList(refactorings);
    }

    public void addRefactoring(AbstractChange change) {
        refactorings.add(change);
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

    public Node createNode(Document currentMigrationFileDOM) {
        Element node = currentMigrationFileDOM.createElement("changeSet");
        node.setAttribute("id", getId());
        node.setAttribute("author", getAuthor());
        for (AbstractChange change : getRefactorings()) {
            node.appendChild(change.createNode(currentMigrationFileDOM));
        }
        return node;
    }

    public void setRollBackSQL(String sql) {
        if (sql == null) {
            return;
        }
        this.rollBackStatements = sql.split(";");
        for (int i=0; i<rollBackStatements.length; i++) {
            rollBackStatements[i] = rollBackStatements[i].trim();
        }
    }

    public boolean canRollBack() {
        if (rollBackStatements != null && rollBackStatements.length > 0) {
            return true;
        }

        for (AbstractChange change : getRefactorings()) {
            if (!change.canRollBack()) {
                 return false;
            }
        }
        return true;
    }

    public String getDescription() {
        List<AbstractChange> refactorings = getRefactorings();
        if (refactorings.size() == 0) {
            return "Empty";
        }

        StringBuffer returnString = new StringBuffer();
        Class lastRefactoringClass = null;
        int refactoringCount = 0;
        for (AbstractChange change : refactorings) {
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
