package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandResultsBuilder
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.FileUtil
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class GenerateChangeLogMSSQLIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem mssql = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mssql")

    def "Should generate table comments, view comments, table column comments, view column comments and be able to use the generated sql changelog"() {
        given:
        runUpdate('changelogs/mssql/issues/generate.changelog.table.view.comments.sql')

        when:
        runGenerateChangelog('output.mssql.sql')

        then:
        def outputFile = new File('output.mssql.sql')
        def contents = FileUtil.getContents(outputFile)
        contents.contains("COMMENT 1")
        contents.contains("COMMENT 2")
        contents.contains("COMMENT 3")
        contents.contains("COMMENT 4")
        contents.contains("COMMENT 5")
        contents.contains("COMMENT 6")

        when:
        runDropAll()
        runUpdate('output.mssql.sql')

        then:
        noExceptionThrown()

        cleanup:
        runDropAll()
        outputFile.delete()
    }

    def "Should generate table comments, view comments, table column comments, view column comments and be able to use the generated xml/json/yml changelog"(String fileType) {
        given:
        runUpdate('changelogs/mssql/issues/generate.changelog.table.view.comments.sql')

        when:
        runGenerateChangelog("output.mssql.$fileType")

        then:
        def outputFile = new File("output.mssql.$fileType")
        def contents = FileUtil.getContents(outputFile)
        contents.count('columnParentType') == 2 //Should appear for the two view column comments.

        when:
        runDropAll()
        runUpdate("output.mssql.$fileType")

        then:
        noExceptionThrown()

        cleanup:
        runDropAll()
        outputFile.delete()

        where:
        fileType << ['xml', 'json', 'yml']
    }

    private void runGenerateChangelog(String outputFile) {
        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, mssql.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, mssql.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, mssql.getPassword())
        commandScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, outputFile)
        OutputStream outputStream = new ByteArrayOutputStream()
        commandScope.setOutput(outputStream)
        commandScope.execute()
    }

    private void runUpdate(String changelog) {
        UpdateCommandStep step = new UpdateCommandStep()
        CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(UpdateCommandStep.URL_ARG, mssql.getConnectionUrl())
        commandScope.addArgumentValue(UpdateCommandStep.USERNAME_ARG, mssql.getUsername())
        commandScope.addArgumentValue(UpdateCommandStep.PASSWORD_ARG, mssql.getPassword())
        commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelog)
        OutputStream outputStream = new ByteArrayOutputStream()
        CommandResultsBuilder commandResultsBuilder = new CommandResultsBuilder(commandScope, outputStream)
        step.run(commandResultsBuilder)
    }

    private void runDropAll() {
        DropAllCommandStep step = new DropAllCommandStep()
        CommandScope commandScope = new CommandScope(DropAllCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(UpdateCommandStep.URL_ARG, mssql.getConnectionUrl())
        commandScope.addArgumentValue(UpdateCommandStep.USERNAME_ARG, mssql.getUsername())
        commandScope.addArgumentValue(UpdateCommandStep.PASSWORD_ARG, mssql.getPassword())
        OutputStream outputStream = new ByteArrayOutputStream()
        CommandResultsBuilder commandResultsBuilder = new CommandResultsBuilder(commandScope, outputStream)
        step.run(commandResultsBuilder)
    }
}
