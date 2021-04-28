package liquibase.extension.testing.setup

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.extension.testing.TestDatabaseConnections
import liquibase.integration.commandline.CommandLineResourceAccessor
import liquibase.resource.CompositeResourceAccessor
import liquibase.resource.FileSystemResourceAccessor

import java.nio.file.Paths

class SetupRunChangelog extends TestSetup {

    private final String changeLog
    private final String labels

    SetupRunChangelog(String changeLog) {
        this.changeLog = changeLog
    }

    SetupRunChangelog(String changeLog, String labels) {
        this.changeLog = changeLog
        this.labels = labels
    }


    @Override
    void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connectionStatus.connection))

        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database)
        changeLogService.init()
        changeLogService.generateDeploymentId()

        changeLogService.reset()
        CompositeResourceAccessor fileOpener = new CompositeResourceAccessor(
                new FileSystemResourceAccessor(Paths.get(".").toAbsolutePath().toFile()),
                new CommandLineResourceAccessor(getClass().getClassLoader())
        )
        Liquibase liquibase = new Liquibase(this.changeLog, fileOpener, database)
        Contexts contexts = null
        liquibase.update(contexts, new LabelExpression(labels))
    }
}
