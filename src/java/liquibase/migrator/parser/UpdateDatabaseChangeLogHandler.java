package liquibase.migrator.parser;

import liquibase.migrator.ChangeSet;
import liquibase.migrator.Migrator;
import liquibase.migrator.RanChangeSet;
import liquibase.migrator.exception.DatabaseHistoryException;
import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.MigrationFailedException;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

/**
 * An implementation of BaseChangeLogHandler for generating statements to update a database.
 *
 * @see liquibase.migrator.parser.BaseChangeLogHandler
 */
public class UpdateDatabaseChangeLogHandler extends BaseChangeLogHandler {

    public UpdateDatabaseChangeLogHandler(Migrator migrator, String physicalFilePath) {
        super(migrator, physicalFilePath);
    }

    /**
     * After the change set has been ran against the database this method will update the change log table
     * with the information.
     */
    public void markChangeSetAsRan(ChangeSet changeSet) throws JDBCException, IOException {
        Migrator migrator = changeSet.getDatabaseChangeLog().getMigrator();
        String dateValue = migrator.getDatabase().getCurrentDateTimeFunction();
        String sql = "INSERT INTO DATABASECHANGELOG (ID, AUTHOR, FILENAME, DATEEXECUTED, MD5SUM, DESCRIPTION, COMMENTS, LIQUIBASE) VALUES ('?', '?', '?', " + dateValue + ", '?', '?', '?', '?')";
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(escapeStringForDatabase(changeSet.getId())));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getAuthor()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getDatabaseChangeLog().getFilePath()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getMd5sum()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(limitSize(changeSet.getDescription())));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(limitSize(StringUtils.trimToEmpty(changeSet.getComments()))));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getDatabaseChangeLog().getMigrator().getBuildVersion()));

        Writer sqlOutputWriter = migrator.getOutputSQLWriter();
        if (sqlOutputWriter == null) {
            Connection connection = migrator.getDatabase().getConnection();
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(sql);
                statement.close();
                connection.commit();
            } catch (SQLException e) {
                throw new JDBCException(e);
            }
        } else {
            sqlOutputWriter.write(sql + ";" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator());
        }

        migrator.getRanChangeSetList().add(new RanChangeSet(changeSet));
    }

    private String limitSize(String string) {
        int maxLength = 255;
        if (string.length() > maxLength) {
            return string.substring(0, maxLength - 3) + "...";
        }
        return string;
    }

    public void markChangeSetAsReRan(ChangeSet changeSet) throws JDBCException, IOException {
        String dateValue = changeSet.getDatabaseChangeLog().getMigrator().getDatabase().getCurrentDateTimeFunction();
        String sql = "UPDATE DATABASECHANGELOG SET DATEEXECUTED=" + dateValue + ", MD5SUM='?' WHERE ID='?' AND AUTHOR='?' AND FILENAME='?'";
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getMd5sum()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getId()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getAuthor()));
        sql = sql.replaceFirst("\\?", escapeStringForDatabase(changeSet.getDatabaseChangeLog().getFilePath()));

        Writer sqlOutputWriter = changeSet.getDatabaseChangeLog().getMigrator().getOutputSQLWriter();
        if (sqlOutputWriter == null) {
            Connection connection = changeSet.getDatabaseChangeLog().getMigrator().getDatabase().getConnection();
            try {
                Statement statement = connection.createStatement();
                statement.executeUpdate(sql);
                statement.close();
                connection.commit();
            } catch (SQLException e) {
                throw new JDBCException(e);
            }
        } else {
            sqlOutputWriter.write(sql + ";" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator());
        }
    }

    private boolean shouldRun(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException {

        // Ignore changeset completely if it's not for this type of database

        String dbmsType = migrator.getDatabase().getTypeName();
        Set<String> dbms = changeSet.getDbmsSet();
        if (dbms != null && dbms.size() > 0) {
            if (!dbms.contains(dbmsType)) {
                return false;
            }
        }
        
        ChangeSet.RunStatus isChangeSetRan = migrator.getRunStatus(changeSet);
        if (changeSet.shouldAlwaysRun() || isChangeSetRan.equals(ChangeSet.RunStatus.NOT_RAN) || isChangeSetRan.equals(ChangeSet.RunStatus.RUN_AGAIN)) {
            return migrator.contextMatches(changeSet);
        } else {
            return false;
        }
    }

    protected void handleChangeSet(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException, MigrationFailedException, IOException {
        if (shouldRun(changeSet)) {
            changeSet.execute();
            if (migrator.getRunStatus(changeSet).equals(ChangeSet.RunStatus.NOT_RAN)) {
                markChangeSetAsRan(changeSet);
            } else {
                markChangeSetAsReRan(changeSet);
            }
            try {
                migrator.getDatabase().getConnection().commit();
            } catch (SQLException e) {
                throw new JDBCException(e);
            }
        }

    }
}
