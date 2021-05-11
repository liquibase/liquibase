package liquibase.extension.testing.setup

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.ChangelogRewriter
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.extension.testing.TestDatabaseConnections
import liquibase.integration.commandline.CommandLineResourceAccessor
import liquibase.resource.CompositeResourceAccessor
import liquibase.resource.FileSystemResourceAccessor

import java.nio.file.Paths

class SetupModifyChangelog extends TestSetup {

    private final String changeLogFile
    private final String id

    SetupModifyChangelog(String changeLogFile) {
        this.changeLogFile = changeLogFile
    }

    SetupModifyChangelog(String changeLogFile, String id) {
        this.changeLogFile = changeLogFile
        this.id = id
    }


    @Override
    void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
        ChangelogRewriter.addChangeLogId(changeLogFile, id, null)
    }
}
