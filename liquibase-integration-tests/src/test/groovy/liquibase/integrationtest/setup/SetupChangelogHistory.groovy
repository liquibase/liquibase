package liquibase.integrationtest.setup

import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.ChangeSet
import liquibase.changelog.RanChangeSet
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.integrationtest.TestDatabaseConnections
import liquibase.integrationtest.TestSetup

class SetupChangelogHistory extends TestSetup {

    private final List<HistoryEntry> wantedHistory

    SetupChangelogHistory() {
        this(new ArrayList<HistoryEntry>());
    }

    SetupChangelogHistory(List<HistoryEntry> wantedHistory) {
        this.wantedHistory = wantedHistory
    }

    static SetupChangelogHistory setupChangelogHistory(@DelegatesTo(HistoryEntry) Closure... wantedHistory) {
        SetupChangelogHistory history = new SetupChangelogHistory()
        for (Closure closure : wantedHistory) {
            def entry = new HistoryEntry()
            def code = closure.rehydrate(entry, this, entry)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()

            history.wantedHistory.add(entry)
        }

        return history
    }

    @Override
    void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connectionStatus.connection))

        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database)
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

    static class HistoryEntry {
        public String id
        public String author
        public String path
        public Integer deploymentId
        public ChangeSet.ExecType execType = ChangeSet.ExecType.EXECUTED
    }
}
