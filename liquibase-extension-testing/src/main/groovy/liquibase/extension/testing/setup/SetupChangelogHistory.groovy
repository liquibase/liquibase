package liquibase.extension.testing.setup

import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.ChangeSet
import liquibase.changelog.RanChangeSet
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection

class SetupChangelogHistory extends TestSetup {

    private final List<HistoryEntry> wantedHistory

    SetupChangelogHistory(List<HistoryEntry> wantedHistory) {
        this.wantedHistory = wantedHistory
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(testSetupEnvironment.connection))

        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database)
        changeLogService.init()
        changeLogService.generateDeploymentId()

        List<RanChangeSet> toRemoveList = new ArrayList<>()
        for (RanChangeSet ranChangeSet : changeLogService.getRanChangeSets()) {
            toRemoveList.add(ranChangeSet)
        }
        for (RanChangeSet ranChangeSet : toRemoveList) {
            changeLogService.removeFromHistory(new ChangeSet(ranChangeSet.getId(), ranChangeSet.getAuthor(), false, false, ranChangeSet.getChangeLog(), null, null, null))
        }
        changeLogService.reset()

        for (HistoryEntry entry : wantedHistory) {
            changeLogService.setExecType(
                    new ChangeSet(entry.id, entry.author, false, false, entry.path, null, null, null),
                    entry.execType
            )
        }

    }

}
