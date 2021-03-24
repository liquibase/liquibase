package liquibase.integrationtest.setup;

import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.integrationtest.TestDatabaseConnections;
import liquibase.integrationtest.TestSetup;

import java.util.ArrayList;
import java.util.List;

public class SetupChangelogHistory extends TestSetup {

    private final List<Entry> wantedHistory;

    public SetupChangelogHistory(List<Entry> wantedHistory) {
        this.wantedHistory = wantedHistory;
    }

    @Override
    public void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connectionStatus.connection));

        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogService.init();
        changeLogService.generateDeploymentId();

        List<RanChangeSet> toRemoveList = new ArrayList<>();
        for (RanChangeSet ranChangeSet : changeLogService.getRanChangeSets()) {
            toRemoveList.add(ranChangeSet);
        }
        for (RanChangeSet ranChangeSet : toRemoveList) {
            changeLogService.removeFromHistory(new ChangeSet(ranChangeSet.getId(), ranChangeSet.getAuthor(), false, false, ranChangeSet.getChangeLog(), null, null, null));
        }
        changeLogService.reset();

        for (Entry entry : wantedHistory) {
            changeLogService.setExecType(
                    new ChangeSet(entry.id, entry.author, false, false, entry.path, null, null, null),
                    entry.execType
            );
        }

    }

    public static class Entry {
        public String id;
        public String author;
        public String path;
        public Integer deploymentId;
        public ChangeSet.ExecType execType = ChangeSet.ExecType.EXECUTED;
    }
}
