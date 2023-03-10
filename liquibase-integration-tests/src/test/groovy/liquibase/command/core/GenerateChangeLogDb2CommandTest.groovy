package liquibase.command.core

import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.FileUtil
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
@Ignore
class GenerateChangeLogDb2CommandTest extends Specification {
    @Shared
    private DatabaseTestSystem db2 = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("db2")

    def "Should generate view comments and be able to use the generated sql changelog"() {
        given:
        CommandUtil.runUpdate(db2,'changelogs/db2/issues/view.comments.sql')

        when:
        CommandUtil.runGenerateChangelog(db2,'output.db2.sql')

        then:
        def outputFile = new File('output.db2.sql')
        def contents = FileUtil.getContents(outputFile)
        contents.contains("COMMENT ON TABLE SOME_VIEW IS 'THIS IS A COMMENT ON SOME_VIEW VIEW. THIS VIEW COMMENT SHOULD BE CAPTURED BY GenerateChangeLog.'")

        when:
        CommandUtil.runDropAll(db2)
        CommandUtil.runUpdate(db2,'output.mssql.sql')

        then:
        noExceptionThrown()

        cleanup:
        CommandUtil.runDropAll(db2)
        outputFile.delete()
    }

    private void runGenerateChangelog(String outputFile) {
        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, db2.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, db2.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, db2.getPassword())
        commandScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, outputFile)
        OutputStream outputStream = new ByteArrayOutputStream()
        commandScope.setOutput(outputStream)
        commandScope.execute()
    }

    private void runUpdate(String changelog) {
        UpdateCommandStep step = new UpdateCommandStep()
        CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(UpdateCommandStep.URL_ARG, db2.getConnectionUrl())
        commandScope.addArgumentValue(UpdateCommandStep.USERNAME_ARG, db2.getUsername())
        commandScope.addArgumentValue(UpdateCommandStep.PASSWORD_ARG, db2.getPassword())
        commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelog)
        OutputStream outputStream = new ByteArrayOutputStream()
        CommandResultsBuilder commandResultsBuilder = new CommandResultsBuilder(commandScope, outputStream)
        step.run(commandResultsBuilder)
    }

    private void runDropAll() {
        DropAllCommandStep step = new DropAllCommandStep()
        CommandScope commandScope = new CommandScope(DropAllCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(UpdateCommandStep.URL_ARG, db2.getConnectionUrl())
        commandScope.addArgumentValue(UpdateCommandStep.USERNAME_ARG, db2.getUsername())
        commandScope.addArgumentValue(UpdateCommandStep.PASSWORD_ARG, db2.getPassword())
        OutputStream outputStream = new ByteArrayOutputStream()
        CommandResultsBuilder commandResultsBuilder = new CommandResultsBuilder(commandScope, outputStream)
        step.run(commandResultsBuilder)
    }
}
