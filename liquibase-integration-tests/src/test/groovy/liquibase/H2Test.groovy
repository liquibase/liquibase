package liquibase


import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.UpdateCountCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
import org.apache.commons.io.FileUtils
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

@LiquibaseIntegrationTest
class H2Test extends Specification {

    @Shared
    private DatabaseTestSystem h2 = Scope.getCurrentScope().getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    def "verify sequence is not created again when precondition fails because it already exists"() {
        when:
        def changeLogFile = "changelogs/sequenceExists-h2.xml"
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): new SearchPathResourceAccessor(".,target/test-classes")
        ]
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
            commandScope.addArgumentValue(UpdateCountCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
            commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)

        then:
        noExceptionThrown()
    }

    def "testingOption #params"() {
        when:
        def scopeSettings = [
           (Scope.Attr.resourceAccessor.name()): new SearchPathResourceAccessor(".,target/test-classes")
        ]
        String fileName = './h2/db'
        Files.deleteIfExists(Path.of(fileName + '.mv.db'))

        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, "jdbc:h2:$fileName;$params")
            commandScope.addArgumentValue(UpdateCountCommandStep.CHANGELOG_FILE_ARG, 'changelogs/h2/complete/loadData.test.changelog.xml')
            commandScope.execute() // JdbcBatchUpdateException: Data conversion error converting "'' (LOADDATATESTTABLE: ""DEC"" DECFLOAT)
            commandScope.execute() // This 2nd execution thrown JdbcSQLSyntaxErrorException: Table "DATABASECHANGELOG" already exists; SQL statement
        } as Scope.ScopedRunnerWithReturn<Void>)

        then:
        noExceptionThrown()

        cleanup:
        FileUtils.deleteQuietly(new File('./h2'))

        where:
        params << ['MODE=PostgreSQL','DATABASE_TO_UPPER=false', 'DATABASE_TO_LOWER=true']

    }

}
