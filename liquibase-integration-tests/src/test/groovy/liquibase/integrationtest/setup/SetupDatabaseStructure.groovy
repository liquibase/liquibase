package liquibase.integrationtest.setup

import liquibase.Scope
import liquibase.change.Change
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.DatabaseException
import liquibase.executor.Executor
import liquibase.executor.ExecutorService
import liquibase.integrationtest.TestDatabaseConnections
import liquibase.integrationtest.TestSetup

class SetupDatabaseStructure extends TestSetup {

    private final List<Entry> wantedStructure

    SetupDatabaseStructure(List<Entry> wantedStructure) {
        this.wantedStructure = wantedStructure
    }

    SetupDatabaseStructure() {
        this(new ArrayList<Entry>())
    }

    static SetupDatabaseStructure create(Change... changes) {
        SetupDatabaseStructure setup = new SetupDatabaseStructure()
        for (Change change : changes) {
            setup.wantedStructure.add(new Entry(change))
        }

        return setup
    }

    @Override
    void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connectionStatus.connection))

        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database)
        changeLogService.init()
        changeLogService.generateDeploymentId()

        changeLogService.reset()

        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)
        for (Entry entry : wantedStructure) {
            List<Change> changes = entry.changes
            changes.each {
                try {
                    executor.execute(it)
                } catch (DatabaseException dbe) {
                    throw new RuntimeException(dbe)
                }
            }
        }
    }

    static class Entry {
        public List<Change> changes = new ArrayList<>()

        Entry(Change change) {
            changes.add(change)
        }
    }
}
