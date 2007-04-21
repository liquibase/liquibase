package liquibase.migrator;

import liquibase.database.AbstractDatabase;
import liquibase.migrator.change.AbstractChange;
import liquibase.migrator.preconditions.PreconditionFailedException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.Writer;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class will serve the purpose of keeping track of the statements.
 * When statements element is encountered in the XML file, this class will
 * be invoked and the child elements of the statements tag will be added to
 * the arraylist as Statement objects.
 */
public class ChangeSet {

    private enum RunStatus {
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
        this.context = context.toLowerCase();
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
     * specified database. Before executing it will check the database change log table to find out
     * if the change set has already been executed or not, if it is already executed then it will not execute
     * it again.
     */
    public void execute() throws DatabaseHistoryException, MigrationFailedException, PreconditionFailedException {
        Migrator migrator = getDatabaseChangeLog().getMigrator();
        try {
            //System.out.println("sdf");
            //String vendor=  getDatabaseChangeLog().getPreconditions().getDbms().getVendor().toLowerCase();
            //System.out.println("dbproduct" +dbproduct);
            //System.out.println("vendor"+getDatabaseChangeLog().getPreconditions().getDbms().getVendor());
            //String dbproduct = migrator.getDatabase().getConnection().getMetaData().getDatabaseProductName();
            //if(getDatabaseChangeLog().getPreconditions() != null && getDatabaseChangeLog().getPreconditions().getDbms().checkVendor(dbproduct)) {
            RunStatus isChangeSetRan = isChangeSetRan();
            if (shouldAlwaysRun() || !isChangeSetRan.equals(RunStatus.ALREADY_RAN)) {
                if (migrator.getMode().equals(Migrator.EXECUTE_MODE)) {
                    log.finest("Reading ChangeSet: " + toString());
                    for (AbstractChange change : getRefactorings()) {
                        change.executeStatement(migrator.getDatabase());
                        log.finest(change.getConfirmationMessage());
                    }

                    migrator.getDatabase().getConnection().commit();
                    log.finest("ChangeSet " + toString() + " has been successfully ran.");
                } else if (migrator.getMode().equals(Migrator.OUTPUT_SQL_MODE)) {
                    getDatabaseChangeLog().getMigrator().getOutputSQLWriter().write("-- Changeset " + toString() + "\n");
                    for (AbstractChange change : getRefactorings()) {
                        change.saveStatement(getDatabaseChangeLog().getMigrator().getDatabase(), getDatabaseChangeLog().getMigrator().getOutputSQLWriter());
                    }
                }
                if (isChangeSetRan.equals(RunStatus.NOT_RAN)) {
                    markChangeSetAsRan();
                } else {
                    markChangeSetAsReRan();
                }
            }
            migrator.getDatabase().getConnection().commit();
        } catch (Exception e) {
            try {
                migrator.getDatabase().getConnection().rollback();
            } catch (SQLException e1) {
                throw new MigrationFailedException("Unable to process change set: " + toString() + ": " + e.getMessage(), e);
            }
            throw new MigrationFailedException("Unable to process change set: " + toString() + ": " + e.getMessage(), e);
        }
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


    /**
     * After the change set has been ran against the database this method will update the change log table
     * with the information.
     *
     * @throws SQLException
     */
    public void markChangeSetAsRan() throws SQLException, IOException {
        String dateValue = dateValue = getDatabaseChangeLog().getMigrator().getDatabase().getCurrentDateTimeFunction();
        String sql = "INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM) VALUES ('?', '?', '?', " + dateValue + ", '?')";
        sql = sql.replaceFirst("\\?", getId().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", getAuthor().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", getDatabaseChangeLog().getMigrator().getMigrationFile().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", getMd5sum().replaceAll("'", "''"));

        Writer sqlOutputWriter = getDatabaseChangeLog().getMigrator().getOutputSQLWriter();
        if (sqlOutputWriter == null) {
            Statement statement = getDatabaseChangeLog().getMigrator().getDatabase().getConnection().createStatement();
            statement.executeUpdate(sql);
            statement.close();
        } else {
            sqlOutputWriter.write(sql + ";\n\n");
        }
    }

    public void markChangeSetAsReRan() throws SQLException, IOException {
        String dateValue = getDatabaseChangeLog().getMigrator().getDatabase().getCurrentDateTimeFunction();
        String sql = "UPDATE DATABASECHANGELOG SET DATEEXECUTED=" + dateValue + ", MD5SUM='?' WHERE ID='?' AND AUTHOR='?' AND FILENAME='?'";
        sql = sql.replaceFirst("\\?", getMd5sum().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", getId().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", getAuthor().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", getDatabaseChangeLog().getMigrator().getMigrationFile().replaceAll("'", "''"));

        Writer sqlOutputWriter = getDatabaseChangeLog().getMigrator().getOutputSQLWriter();
        if (sqlOutputWriter == null) {
            Statement statement = getDatabaseChangeLog().getMigrator().getDatabase().getConnection().createStatement();
            statement.executeUpdate(sql);
            statement.close();
        } else {
            sqlOutputWriter.write(sql + ";\n\n");
        }
    }

    /**
     * This mehod is responisble to check if the change set has already been ran or not.
     * If a change set is marked as "runOnChange", this will return 'true' if the change set has changed.
     * If the change set ran more than once and it is not marked 'runOnChange' it will throw an SQLException.
     *
     * @return
     * @throws DatabaseHistoryException
     */
    public RunStatus isChangeSetRan() throws DatabaseHistoryException, SQLException {
        AbstractDatabase database = getDatabaseChangeLog().getMigrator().getDatabase();
        //System.out.println(database.g)
        if (!database.doesChangeLogTableExist()) {
            return RunStatus.NOT_RAN;
        }
        PreparedStatement pstmt;
        ResultSet rs;
        Connection connection = database.getConnection();
        pstmt = connection.prepareStatement("select md5sum from DatabaseChangeLog where id=? AND author=? AND filename=?".toUpperCase());
        pstmt.setString(1, getId());
        pstmt.setString(2, getAuthor());
        pstmt.setString(3, databaseChangeLog.getMigrator().getMigrationFile());
        rs = pstmt.executeQuery();
        if (rs.next()) {
            log.finest("ChangeSet: " + getId() + ":" + databaseChangeLog.getMigrator().getMigrationFile() + " was already run");
            String md5sum = rs.getString("md5sum");
            if (md5sum == null) {
                log.info("Updating NULL md5sum for " + this.toString());
                PreparedStatement updatePstmt = connection.prepareStatement("update DatabaseChangeLog set md5sum=? where id=? AND author=? AND filename=?".toUpperCase());
                updatePstmt.setString(1, getMd5sum());
                updatePstmt.setString(2, getId());
                updatePstmt.setString(3, getAuthor());
                updatePstmt.setString(4, databaseChangeLog.getMigrator().getMigrationFile());

                updatePstmt.executeUpdate();
                updatePstmt.close();
                connection.commit();
            } else if (!md5sum.equals(getMd5sum())) {
                if (shouldRunOnChange()) {
                    return RunStatus.RUN_AGAIN;
                } else {
                    throw new DatabaseHistoryException("MD5 Check for " + toString() + " failed");
                }
            }
            return RunStatus.ALREADY_RAN;
        }
        return RunStatus.NOT_RAN;
    }

    public String toString() {
        return getDatabaseChangeLog().getMigrator().getMigrationFile() + " :: " + getId() + " :: " + getAuthor() + " :: (MD5Sum: " + getMd5sum() + ")";
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
}
