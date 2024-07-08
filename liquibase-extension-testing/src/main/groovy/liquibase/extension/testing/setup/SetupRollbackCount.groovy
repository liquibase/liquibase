package liquibase.extension.testing.setup

import liquibase.Liquibase
import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection

import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.CompositeResourceAccessor
import liquibase.resource.DirectoryResourceAccessor

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

        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database)
        changeLogService.init()
        changeLogService.generateDeploymentId()

        changeLogService.reset()
        CompositeResourceAccessor fileOpener = new CompositeResourceAccessor(
                new DirectoryResourceAccessor(Paths.get(".").toAbsolutePath().toFile()),
                new ClassLoaderResourceAccessor(getClass().getClassLoader())
        )
        Liquibase liquibase = new Liquibase(this.changeLog, fileOpener, database)
        liquibase.rollback(count, null)
    }
}
