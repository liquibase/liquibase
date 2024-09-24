package liquibase

import liquibase.command.CommandScope
import liquibase.command.core.UpdateSqlCommandStep
import liquibase.command.core.UpdateToTagCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.exception.CommandExecutionException
import liquibase.extension.testing.setup.SetupEnvironmentVariableProvider
import liquibase.extension.testing.setup.TestSetupEnvironment
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class StrictIntegrationTests extends Specification {

    @Shared
    private DatabaseTestSystem h2 = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("h2")
    @Shared
    private SetupEnvironmentVariableProvider environmentVariableProvider

    def setup() {
        def scopeSettings = [
                (GlobalConfiguration.STRICT.getKey()): Boolean.TRUE
        ]
        String[] remove = [:]
        environmentVariableProvider = new SetupEnvironmentVariableProvider(scopeSettings, remove)
        TestSetupEnvironment testSetupEnvironment = new TestSetupEnvironment(h2, null)
        environmentVariableProvider.setup(testSetupEnvironment)
    }

    def "validate exception is thrown if STRICT global argument is set as true and there is not TagDatabaseChange, but tag argument is specified "() {
        when:
        Scope.child([:], {
            def updateToTagCommand = new CommandScope(UpdateToTagCommandStep.COMMAND_NAME)
            updateToTagCommand.addArgumentValue(UpdateToTagCommandStep.TAG_ARG, "testTag")
            updateToTagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
            updateToTagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
            updateToTagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
            updateToTagCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "liquibase/update-tests.yml")
            updateToTagCommand.execute()
        } as Scope.ScopedRunner)

        then:
        final CommandExecutionException exception = thrown()
        exception.message.contains("Change 'TagDatabaseChange' not found or supported")
        cleanup:
        environmentVariableProvider.cleanup()
    }

    def "validate exception is thrown if STRICT global argument is set as true, there is a TagDatabaseChange, but tag argument specified does not match with TagDatabaseChange's tag"() {
        when:
        Scope.child([:], {
            def updateToTagCommand = new CommandScope(UpdateToTagCommandStep.COMMAND_NAME)
            updateToTagCommand.addArgumentValue(UpdateToTagCommandStep.TAG_ARG, "testTag")
            updateToTagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
            updateToTagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
            updateToTagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
            updateToTagCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "liquibase/update-to-tag-changelog.xml")
            updateToTagCommand.execute()
        } as Scope.ScopedRunner)

        then:
        final CommandExecutionException exception = thrown()
        exception.message.contains("liquibase.exception.LiquibaseException: Command execution tag testTag does not match with any changeSet tag")

        cleanup:
        Scope.exit(Scope.getCurrentScope().getScopeId())
        environmentVariableProvider.cleanup()
    }
}
