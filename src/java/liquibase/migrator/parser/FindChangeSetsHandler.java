package liquibase.migrator.parser;

import liquibase.migrator.ChangeSet;
import liquibase.migrator.Migrator;
import liquibase.migrator.exception.DatabaseHistoryException;
import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.MigrationFailedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FindChangeSetsHandler extends BaseChangeLogHandler {

    private static List<ChangeSet> unrunChangeSets;

    public FindChangeSetsHandler(Migrator migrator, String physicalChangeLogLocation) {
        super(migrator, physicalChangeLogLocation);

        if (unrunChangeSets == null) {
            unrunChangeSets = new ArrayList<ChangeSet>();
        }
    }

    protected void handleChangeSet(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException, MigrationFailedException, IOException {
        if (migrator.getRunStatus(changeSet).equals(ChangeSet.RunStatus.NOT_RAN) && migrator.contextMatches(changeSet)) {
            unrunChangeSets.add(changeSet);
        }
    }

    public static List<ChangeSet> getUnrunChangeSets() {
        return unrunChangeSets;
    }
}
