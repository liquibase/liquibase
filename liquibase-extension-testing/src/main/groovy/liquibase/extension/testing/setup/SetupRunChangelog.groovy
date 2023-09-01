package liquibase.extension.testing.setup

import liquibase.Contexts
import liquibase.GlobalConfiguration
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.configuration.AbstractMapConfigurationValueProvider
import liquibase.configuration.ConfigurationValueProvider
import liquibase.configuration.LiquibaseConfiguration
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection

import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.CompositeResourceAccessor
import liquibase.resource.DirectoryResourceAccessor
import liquibase.resource.ResourceAccessor
import liquibase.resource.SearchPathResourceAccessor

import java.nio.file.Paths

class SetupRunChangelog extends TestSetup {

    private final String changeLog
    private final String labels
    private final String searchPath

    SetupRunChangelog(String changeLog) {
        this.changeLog = changeLog
    }

    SetupRunChangelog(String changeLog, String labels) {
        this.changeLog = changeLog
        this.labels = labels
    }

    SetupRunChangelog(String changeLog, String labels, String searchPath) {
        this.changeLog = changeLog
        this.labels = labels
        this.searchPath = searchPath
    }


    @Override
    void setup(TestSetupEnvironment testSetupEnvironment) throws Exception {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(testSetupEnvironment.connection))

        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database)
        changeLogService.init()
        changeLogService.generateDeploymentId()

        changeLogService.reset()
        ResourceAccessor resourceAccessor;

        if (searchPath != null) {
            def config = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)

            ConfigurationValueProvider propertiesProvider = new AbstractMapConfigurationValueProvider() {
                @Override
                protected Map<?, ?> getMap() {
                    return Collections.singletonMap(GlobalConfiguration.SEARCH_PATH.getKey(), searchPath)
                }

                @Override
                protected String getSourceDescription() {
                    return "command tests search path override"
                }

                @Override
                int getPrecedence() {
                    return 1
                }
            }

            config.registerProvider(propertiesProvider)
            resourceAccessor = new SearchPathResourceAccessor(Scope.getCurrentScope().getResourceAccessor())
        } else {
            resourceAccessor = new CompositeResourceAccessor(
                    new DirectoryResourceAccessor(Paths.get(".").toAbsolutePath().toFile()),
                    new ClassLoaderResourceAccessor(getClass().getClassLoader())
            )
        }

        Liquibase liquibase = new Liquibase(this.changeLog, resourceAccessor, database)
        Contexts contexts = null
        liquibase.update(contexts, new LabelExpression(labels))
    }
}
