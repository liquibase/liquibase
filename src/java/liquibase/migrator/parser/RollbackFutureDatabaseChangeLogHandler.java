package liquibase.migrator.parser;

import liquibase.migrator.preconditions.PreconditionFailedException;
import liquibase.migrator.parser.BaseChangeLogHandler;
import liquibase.migrator.*;

import java.io.IOException;
import java.sql.SQLException;
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

    public RollbackFutureDatabaseChangeLogHandler(Migrator migrator, String physicalFilePath) throws SQLException {
        super(migrator, physicalFilePath);
        changesToRollback = new ArrayList<ChangeSet>();
        ranChangeSets = migrator.getRanChangeSetList();
    }

    protected void handleChangeSet(ChangeSet changeSet) throws SQLException, DatabaseHistoryException, MigrationFailedException, PreconditionFailedException, IOException {
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

    public void doRollback() throws MigrationFailedException, DatabaseHistoryException, SQLException, IOException {
        for (ChangeSet changeSet : changesToRollback) {
            changeSet.execute();
            removeRanStatus(changeSet);
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
