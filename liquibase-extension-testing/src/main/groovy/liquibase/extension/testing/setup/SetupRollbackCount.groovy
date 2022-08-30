package liquibase.extension.testing.setup

import liquibase.Liquibase
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.integration.commandline.CommandLineResourceAccessor
import liquibase.resource.CompositeResourceAccessor
import liquibase.resource.FileSystemResourceAccessor

import java.nio.file.Paths

class SetupRollbackCount extends TestSetup {

    private final Integer count
    private final String changeLog

    SetupRollbackCount(Integer count, String changeLog) {
        this.count = count
        this.changeLog = changeLog
    }

    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(testSetupEnvironment.connection))

        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database)
        changeLogService.init()
        changeLogService.generateDeploymentId()

        changeLogService.reset()
        CompositeResourceAccessor fileOpener = new CompositeResourceAccessor(
                new FileSystemResourceAccessor(Paths.get(".").toAbsolutePath().toFile()),
                new CommandLineResourceAccessor(getClass().getClassLoader())
        )
        Liquibase liquibase = new Liquibase(this.changeLog, fileOpener, database)
        liquibase.rollback(count, null)
    }
}
