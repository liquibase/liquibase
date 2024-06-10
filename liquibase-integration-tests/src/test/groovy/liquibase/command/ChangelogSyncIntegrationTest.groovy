package liquibase.command

import liquibase.Scope
import liquibase.command.core.ChangelogSyncCommandStep
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class ChangelogSyncIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    def "Verify deploymentId is populated when running changelogSync"() {
        when:
        def changelogSync = new CommandScope(ChangelogSyncCommandStep.COMMAND_NAME)
        changelogSync.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, "liquibase/update-tests.yml")
        changelogSync.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        changelogSync.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        changelogSync.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
        changelogSync.execute()

        then:
        def detailsResultSet = h2.getConnection().createStatement().executeQuery("select DEPLOYMENT_ID from databasechangelog")
        detailsResultSet.next()
        assert detailsResultSet.getString(1) != null: "No deployment ID found for changelog sync"

        cleanup:
        CommandUtil.runDropAll(h2)
    }
}
