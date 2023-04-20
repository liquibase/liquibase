package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandResultsBuilder
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class ListLocksIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem mariadb =
            (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mariadb")


    def "Can list locks on mariadb"() {
        when:
        CommandScope commandScope = new CommandScope(ListLocksCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, mariadb.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, mariadb.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, mariadb.getPassword())

        then:
        commandScope.execute()
    }
}
