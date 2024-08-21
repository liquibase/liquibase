package liquibase.command.core

import liquibase.Scope
import liquibase.changelog.visitor.DefaultChangeExecListener
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class UpdateToTagIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("h2")

    def "validate updateToTag is successfully executed when there is not TagDatabaseChange, but tag argument is specified "() {
        when:
        def outputStream =  new ByteArrayOutputStream()
        def updateToTagCommand = new CommandScope(UpdateToTagCommandStep.COMMAND_NAME)
        updateToTagCommand.addArgumentValue(UpdateToTagCommandStep.TAG_ARG, "testTag")
        updateToTagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        updateToTagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        updateToTagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
        updateToTagCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "liquibase/update-tests.yml")
        updateToTagCommand.setOutput(outputStream)
        def result = updateToTagCommand.execute().getResults()

        then:
        def a = ((DefaultChangeExecListener)result.get("defaultChangeExecListener"))
        a.getDeployedChangeSets().get(0).getId() == "1"
        outputStream.toString().contains("Run:                          1")
        outputStream.toString().contains("Filtered out:                 0")
    }

    def "validate updateToTag is successfully executed when there is TagDatabaseChange, but tag argument specified does not match with TagDatabaseChange's tag"() {
        when:
        def outputStream =  new ByteArrayOutputStream()
        def updateToTagCommand = new CommandScope(UpdateToTagCommandStep.COMMAND_NAME)
        updateToTagCommand.addArgumentValue(UpdateToTagCommandStep.TAG_ARG, "testTag")
        updateToTagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        updateToTagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        updateToTagCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
        updateToTagCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "liquibase/update-to-tag-changelog.xml")
        updateToTagCommand.setOutput(outputStream)
        def result = updateToTagCommand.execute().getResults()

        then:
        def a = ((DefaultChangeExecListener)result.get("defaultChangeExecListener"))
        a.getDeployedChangeSets().get(0).getId() == "1"
        a.getDeployedChangeSets().get(1).getId() == "tagChange"
        a.getDeployedChangeSets().get(2).getId() == "2"
        outputStream.toString().contains("Run:                          3")
        outputStream.toString().contains("Filtered out:                 0")
    }

}
