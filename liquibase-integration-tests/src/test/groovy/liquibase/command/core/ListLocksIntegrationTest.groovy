package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
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
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, mariadb.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, mariadb.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, mariadb.getPassword())

        then:
        commandScope.execute()
    }
}
