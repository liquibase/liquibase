package liquibase.parser;

import liquibase.ChangeSet;
import liquibase.FileOpener;
import liquibase.migrator.Migrator;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.JDBCException;
import liquibase.exception.MigrationFailedException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

/**
 * An implementation of BaseChangeLogHandler for generating statements to update a database.
 *
 * @see liquibase.parser.BaseChangeLogHandler
 */
public class UpdateDatabaseChangeLogHandler extends BaseChangeLogHandler {

    public UpdateDatabaseChangeLogHandler(Migrator migrator, String physicalFilePath, FileOpener fileOpener) {
        super(migrator, physicalFilePath,fileOpener);
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
        	log.info("Running Changeset:" + changeSet);
            changeSet.execute();
            if (migrator.getRunStatus(changeSet).equals(ChangeSet.RunStatus.NOT_RAN)) {
                migrator.markChangeSetAsRan(changeSet);
            } else {
                migrator.markChangeSetAsReRan(changeSet);
            }
            try {
                migrator.getDatabase().getConnection().commit();
            } catch (SQLException e) {
                throw new JDBCException(e);
            }
        }

    }
}
