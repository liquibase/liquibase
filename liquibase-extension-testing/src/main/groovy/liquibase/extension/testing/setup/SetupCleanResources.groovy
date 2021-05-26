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

class SetupCleanResources extends TestSetup {

    private final List<String> resourcesToDelete = new ArrayList<>()

    SetupCleanResources(String[] resourcesToDelete) {
        this.resourcesToDelete.addAll(resourcesToDelete as Set)
    }

    @Override
    void setup(TestDatabaseConnections.ConnectionStatus connectionStatus) throws Exception {
        for (String fileToDelete : resourcesToDelete) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(fileToDelete)
            if (url == null) {
                return
            }
            File f = new File(url.toURI())
            if (f.exists()) {
                boolean b = f.delete()
                if (b) {
                    assert !f.exists(): "The file '$f' was not deleted"
                }
            }
        }
    }
}
