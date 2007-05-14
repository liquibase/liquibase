package liquibase.migrator;

import liquibase.migrator.change.*;
import liquibase.migrator.preconditions.*;
import liquibase.util.StringUtils;
import liquibase.database.AbstractDatabase;
import liquibase.StreamUtil;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.logging.Logger;
import java.sql.*;
import java.io.IOException;
import java.io.Writer;

public class UpdateDatabaseChangeLogHandler extends BaseChangeLogHandler {

    public UpdateDatabaseChangeLogHandler(Migrator migrator, String physicalFilePath) {
        super(migrator, physicalFilePath);
    }

    /**
     * After the change set has been ran against the database this method will update the change log table
     * with the information.
     *
     * @throws SQLException
     */
    public void markChangeSetAsRan(ChangeSet changeSet) throws SQLException, IOException {
        Migrator migrator = changeSet.getDatabaseChangeLog().getMigrator();
        String dateValue = migrator.getDatabase().getCurrentDateTimeFunction();
        String sql = "INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, LIQUIBASE) VALUES ('?', '?', '?', " + dateValue + ", '?', '?', '?', '?')";
        sql = sql.replaceFirst("\\?", changeSet.getId().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", changeSet.getAuthor().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", changeSet.getDatabaseChangeLog().getFilePath().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", changeSet.getMd5sum().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", limitSize(changeSet.getDescription().replaceAll("'", "''")));
        sql = sql.replaceFirst("\\?", limitSize(StringUtils.trimToEmpty(changeSet.getComments()).replaceAll("'", "''")));
        sql = sql.replaceFirst("\\?", changeSet.getDatabaseChangeLog().getMigrator().getBuildVersion().replaceAll("'", "''"));

        Writer sqlOutputWriter = migrator.getOutputSQLWriter();
        if (sqlOutputWriter == null) {
            Connection connection = migrator.getDatabase().getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
            connection.commit();
        } else {
            sqlOutputWriter.write(sql + ";"+ StreamUtil.getLineSeparator()+StreamUtil.getLineSeparator());
        }

        migrator.getRanChangeSetList().add(new RanChangeSet(changeSet));
    }

    private String limitSize(String string) {
        int maxLength = 255;
        if (string.length() > maxLength) {
            return string.substring(0, maxLength-3)+"...";
        }
        return string;
    }

    public void markChangeSetAsReRan(ChangeSet changeSet) throws SQLException, IOException {
        String dateValue = changeSet.getDatabaseChangeLog().getMigrator().getDatabase().getCurrentDateTimeFunction();
        String sql = "UPDATE DATABASECHANGELOG SET DATEEXECUTED=" + dateValue + ", MD5SUM='?' WHERE ID='?' AND AUTHOR='?' AND FILENAME='?'";
        sql = sql.replaceFirst("\\?", changeSet.getMd5sum().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", changeSet.getId().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", changeSet.getAuthor().replaceAll("'", "''"));
        sql = sql.replaceFirst("\\?", changeSet.getDatabaseChangeLog().getFilePath().replaceAll("'", "''"));

        Writer sqlOutputWriter = changeSet.getDatabaseChangeLog().getMigrator().getOutputSQLWriter();
        if (sqlOutputWriter == null) {
            Connection connection = changeSet.getDatabaseChangeLog().getMigrator().getDatabase().getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(sql);
            statement.close();
            connection.commit();
        } else {
            sqlOutputWriter.write(sql + ";"+StreamUtil.getLineSeparator()+StreamUtil.getLineSeparator());
        }
    }

    private boolean shouldRun(ChangeSet changeSet) throws SQLException, DatabaseHistoryException {
        ChangeSet.RunStatus isChangeSetRan = migrator.getRunStatus(changeSet);
        if (changeSet.shouldAlwaysRun() || isChangeSetRan.equals(ChangeSet.RunStatus.NOT_RAN)  || isChangeSetRan.equals(ChangeSet.RunStatus.RUN_AGAIN)) {
            Set<String> requiredContexts = changeSet.getDatabaseChangeLog().getMigrator().getContexts();
            String changeSetContext = changeSet.getContext();
            return changeSetContext == null || requiredContexts.size() == 0 || requiredContexts.contains(changeSetContext);
        } else {
            return false;
        }
    }

    protected void handleChangeSet(ChangeSet changeSet) throws SQLException, DatabaseHistoryException, MigrationFailedException, IOException {
        if (shouldRun(changeSet)) {
            changeSet.execute();
            if (migrator.getRunStatus(changeSet).equals(ChangeSet.RunStatus.NOT_RAN)) {
                markChangeSetAsRan(changeSet);
            } else {
                markChangeSetAsReRan(changeSet);
            }
            migrator.getDatabase().getConnection().commit();
        }

    }
}
