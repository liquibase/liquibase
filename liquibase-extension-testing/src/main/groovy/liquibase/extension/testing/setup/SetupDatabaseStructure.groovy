package liquibase.extension.testing.setup

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

class SetupDatabaseStructure extends TestSetup {

    private final List<Change> changes

    SetupDatabaseStructure(List<Change> changes) {
        this.changes = changes;
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        Database database = getDatabase(testSetupEnvironment)

        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database)
        changeLogService.init()
        changeLogService.generateDeploymentId()

        changeLogService.reset()

        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database)
        changes.each {
            try {
                executor.execute(it)
                database.commit()
            } catch (DatabaseException dbe) {
                throw new RuntimeException(dbe)
            }
        }
    }

    protected Database getDatabase(TestSetupEnvironment testSetupEnvironment) {
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(testSetupEnvironment.connection))
    }
}
