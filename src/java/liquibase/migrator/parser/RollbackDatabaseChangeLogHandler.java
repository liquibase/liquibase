package liquibase.migrator.parser;

import liquibase.migrator.ChangeSet;
import liquibase.migrator.FileOpener;
import liquibase.migrator.Migrator;
import liquibase.migrator.RanChangeSet;
import liquibase.migrator.exception.DatabaseHistoryException;
import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.MigrationFailedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An implementation of BaseChangeLogHandler for generating rollback statements.
 *
 * @see BaseChangeLogHandler
 */
public class RollbackDatabaseChangeLogHandler extends BaseChangeLogHandler {
    private List<RanChangeSet> ranChangesToRollback;
    private List<ChangeSet> allChangeSets;

    public RollbackDatabaseChangeLogHandler(Migrator migrator, String physicalFilePath, FileOpener fileOpener, String rollbackToTag) throws JDBCException {
        super(migrator, physicalFilePath,fileOpener);
        ranChangesToRollback = new ArrayList<RanChangeSet>();
        int currentChangeSetCount = migrator.getRanChangeSetList().size();
        for (int i = currentChangeSetCount - 1; i >= 0; i--) {
            RanChangeSet ranChangeSet = migrator.getRanChangeSetList().get(i);
            if (rollbackToTag.equals(ranChangeSet.getTag())) {
                break;
            }
            ranChangesToRollback.add(ranChangeSet);
        }
        allChangeSets = new ArrayList<ChangeSet>();
    }

    public RollbackDatabaseChangeLogHandler(Migrator migrator, String physicalFilePath, FileOpener fileOpener, Date rollbackToDate) throws JDBCException {
        super(migrator, physicalFilePath,fileOpener);
        ranChangesToRollback = new ArrayList<RanChangeSet>();
        int currentChangeSetCount = migrator.getRanChangeSetList().size();
        for (int i = currentChangeSetCount - 1; i >= 0; i--) {
            RanChangeSet ranChangeSet = migrator.getRanChangeSetList().get(i);
            if (ranChangeSet.getDateExecuted().getTime() > rollbackToDate.getTime()) {
                ranChangesToRollback.add(ranChangeSet);
            }
        }
        allChangeSets = new ArrayList<ChangeSet>();
    }

    public RollbackDatabaseChangeLogHandler(Migrator migrator, String physicalFilePath, FileOpener fileOpener, Integer numberOfChangeSetsToRollback) throws JDBCException {
        super(migrator, physicalFilePath,fileOpener);
        ranChangesToRollback = new ArrayList<RanChangeSet>();
        int currentChangeSetCount = migrator.getRanChangeSetList().size();
        for (int i = currentChangeSetCount - 1; i >= 0; i--) {
            RanChangeSet ranChangeSet = migrator.getRanChangeSetList().get(i);
            ranChangesToRollback.add(ranChangeSet);
            if (ranChangesToRollback.size() >= numberOfChangeSetsToRollback) {
                break;
            }
        }
        allChangeSets = new ArrayList<ChangeSet>();
    }

    protected void handleChangeSet(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException, MigrationFailedException, IOException {
        for (RanChangeSet cs : ranChangesToRollback) {
            if (cs.isSameAs(changeSet)) {
                allChangeSets.add(0, changeSet);
            }
        }
    }

    public void doRollback() throws MigrationFailedException,  JDBCException, IOException {
        for (ChangeSet changeSet : allChangeSets) {
            changeSet.execute();
            migrator.removeRanStatus(changeSet);
        }
    }

    public ChangeSet getUnRollBackableChangeSet() {
        for (ChangeSet changeSet : allChangeSets) {
            if (!changeSet.canRollBack()) {
                return changeSet;
            }
        }
        return null;
    }
}
