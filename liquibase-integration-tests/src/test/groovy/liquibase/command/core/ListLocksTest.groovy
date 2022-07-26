package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandResultsBuilder
import liquibase.command.CommandScope
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class ListLocksTest extends Specification {

    @Shared
    private DatabaseTestSystem mariadb =
            (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mariadb")


    def "Can list locks on mariadb"() {
        when:
        CommandScope commandScope = new CommandScope(ListLocksCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(ListLocksCommandStep.URL_ARG, mariadb.getConnectionUrl())
        commandScope.addArgumentValue(ListLocksCommandStep.USERNAME_ARG, mariadb.getUsername())
        commandScope.addArgumentValue(ListLocksCommandStep.PASSWORD_ARG, mariadb.getPassword())
        OutputStream outputStream = new ByteArrayOutputStream()
        CommandResultsBuilder commandResultsBuilder = new CommandResultsBuilder(commandScope, outputStream)

        then:
        ListLocksCommandStep diffChangelogCommandStep = new ListLocksCommandStep()
        diffChangelogCommandStep.run(commandResultsBuilder)
    }
}
