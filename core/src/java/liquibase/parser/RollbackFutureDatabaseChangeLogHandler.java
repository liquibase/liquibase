package liquibase.parser;

import liquibase.ChangeSet;
import liquibase.FileOpener;
import liquibase.RanChangeSet;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.JDBCException;
import liquibase.exception.MigrationFailedException;
import liquibase.migrator.Migrator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of BaseChangeLogHandler for generating rollback statements of not-yet-ran changes.
 *
 * @see BaseChangeLogHandler 
 */
public class RollbackFutureDatabaseChangeLogHandler extends BaseChangeLogHandler {
    private List<ChangeSet> changesToRollback;
    private List<RanChangeSet> ranChangeSets;

    public RollbackFutureDatabaseChangeLogHandler(Migrator migrator, String physicalFilePath, FileOpener fileOpener) throws JDBCException {
        super(migrator, physicalFilePath,fileOpener);
        changesToRollback = new ArrayList<ChangeSet>();
        ranChangeSets = migrator.getRanChangeSetList();
    }

    protected void handleChangeSet(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException, MigrationFailedException, IOException {
        boolean alreadyRan = false;
        for (RanChangeSet cs : ranChangeSets) {
            if (cs.isSameAs(changeSet)) {
                alreadyRan = true;
                break;
            }
        }
        if (!alreadyRan) {
            changesToRollback.add(0, changeSet);
        }
    }

    public void doRollback() throws MigrationFailedException,  JDBCException, IOException {
        for (ChangeSet changeSet : changesToRollback) {
            changeSet.execute();
            migrator.removeRanStatus(changeSet);
        }
    }

    public ChangeSet getUnRollBackableChangeSet() {
        for (ChangeSet changeSet : changesToRollback) {
            if (!changeSet.canRollBack()) {
                return changeSet;
            }
        }
        return null;
    }
}
