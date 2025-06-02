package liquibase


import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.UpdateCountCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class MySQLTest extends Specification {

    @Shared
    private DatabaseTestSystem mysql = Scope.getCurrentScope().getSingleton(TestSystemFactory).getTestSystem("mysql") as DatabaseTestSystem

    def "verify foreignKeyExists constraint is not created again when precondition fails because it already exists"() {
        when:
        def changeLogFile = "changelogs/mysql/complete/fkep.test.changelog.xml"
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): new SearchPathResourceAccessor(".,target/test-classes")
        ]
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, mysql.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, mysql.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, mysql.getPassword())
            commandScope.addArgumentValue(UpdateCountCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
            commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)

        then:
        noExceptionThrown()
    }

    def "verify Unique constraint is not created again when precondition fails because it already exists"() {
        when:
        def changeLogFile = "changelogs/uniqueConstraint-mysql.xml"
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): new SearchPathResourceAccessor(".,target/test-classes")
        ]
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, mysql.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, mysql.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, mysql.getPassword())
            commandScope.addArgumentValue(UpdateCountCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
            commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)

        then:
        noExceptionThrown()
    }

}
