package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

@LiquibaseIntegrationTest
class DbDocPostgresIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem postgres = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "Should generate db docs"() {
        given:
        CommandUtil.runDropAll(postgres)
        String outputDirectory = "dbdoc-postgres"
        String changelogFile = "src/test/resources/changelogs/pgsql/complete/basic.formatted.sql"
        CommandUtil.runDropAll(postgres)
        CommandUtil.runUpdate(postgres, changelogFile)
        Files.createDirectories(Paths.get(outputDirectory))

        when:
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): new SearchPathResourceAccessor(".")
        ]
        Scope.child(scopeSettings, {
            CommandScope dbDocCommand = new CommandScope(DbDocCommandStep.COMMAND_NAME[0])
            dbDocCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
            dbDocCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
            dbDocCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())
            dbDocCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changelogFile)
            dbDocCommand.addArgumentValue(DbDocCommandStep.OUTPUT_DIRECTORY_ARG, outputDirectory)
            OutputStream outputStream = new ByteArrayOutputStream()
            dbDocCommand.setOutput(outputStream)
            dbDocCommand.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)

        then:
        noExceptionThrown()
        Files.exists(Paths.get("${outputDirectory}/tables/lbcat.public/person.html"))

        cleanup:
        CommandUtil.runDropAll(postgres)
        new File(outputDirectory).deleteDir()
    }
}
