package liquibase.util

import liquibase.Scope
import liquibase.UpdateSummaryEnum
import liquibase.UpdateSummaryOutputEnum
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.core.helpers.ShowSummaryArgument
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.logging.core.BufferedLogService
import spock.lang.Shared
import spock.lang.Specification

import java.util.logging.Level

@LiquibaseIntegrationTest
class ShowSummaryUtilCommandTest extends Specification {
    @Shared
    private DatabaseTestSystem h2 = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("h2")

    def "Should show summary output when run multiple times"() {
        given:
        String outputFile = "target/test-classes/output.txt"

        when:
        new File(outputFile).delete()
        CommandUtil.runUpdate(h2,'changelogs/pgsql/update/showSummaryWithLabels.xml', "testtable1", "none", outputFile)

        then:
        new File(outputFile).getText("UTF-8").contains("Run:                          2")
        new File(outputFile).getText("UTF-8").contains("Filtered out:                 4")

        when:
        new File(outputFile).delete()
        CommandUtil.runUpdate(h2,'changelogs/pgsql/update/showSummaryWithLabels.xml', "testtable1", "none", outputFile)

        then:
        new File(outputFile).getText("UTF-8").contains("Run:                          0")
        new File(outputFile).getText("UTF-8").contains("Filtered out:                 4")

        cleanup:
        CommandUtil.runDropAll(h2)
        if (h2.getConnection() != null) {
            h2.getConnection().close()
        }
    }

    def "validate update summary output is only written to LOG service"() {

        when:
        Map<String, Object> scopeValues = new HashMap<>()
        def outputStream = new ByteArrayOutputStream()
        def logService = new BufferedLogService()
        scopeValues.put(Scope.Attr.logService.name(), logService)
        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/pgsql/update/showSummaryWithLabels.xml")
                commandScope.addArgumentValue(UpdateCommandStep.LABEL_FILTER_ARG, "testtable1")
                commandScope.addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, null)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.VERBOSE)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.LOG)
                commandScope.setOutput(outputStream)
                commandScope.execute()
            }
        })

        then:
        def logContent = logService.getLogAsString(Level.INFO);
        logContent.contains("UPDATE SUMMARY")
        logContent.contains("Run:                          2")
        logContent.contains("Previously run:               0")
        logContent.contains("Filtered out:                 4")
        logContent.contains("-------------------------------")
        logContent.contains("Total change sets:            6")
        logContent.contains("FILTERED CHANGE SETS SUMMARY")
        logContent.contains("Label mismatch:               3")
        logContent.contains("DBMS mismatch:                1")

        !outputStream.toString().contains("UPDATE SUMMARY")

        cleanup:
        CommandUtil.runDropAll(h2)
        if (h2.getConnection() != null) {
            h2.getConnection().close()
        }
    }

    def "validate update summary output is only written to CONSOLE"() {

        when:
        Map<String, Object> scopeValues = new HashMap<>()
        def outputStream = new ByteArrayOutputStream()
        def logService = new BufferedLogService()
        scopeValues.put(Scope.Attr.logService.name(), logService)
        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/pgsql/update/showSummaryWithLabels.xml")
                commandScope.addArgumentValue(UpdateCommandStep.LABEL_FILTER_ARG, "testtable1")
                commandScope.addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, null)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.VERBOSE)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.CONSOLE)
                commandScope.setOutput(outputStream)
                commandScope.execute()
            }
        })

        then:
        def logContent = logService.getLogAsString(Level.INFO);
        def streamContent = outputStream.toString();

        streamContent.contains("UPDATE SUMMARY")
        streamContent.contains("Run:                          2")
        streamContent.contains("Previously run:               0")
        streamContent.contains("Filtered out:                 4")
        streamContent.contains("-------------------------------")
        streamContent.contains("Total change sets:            6")
        streamContent.contains("FILTERED CHANGE SETS SUMMARY")
        streamContent.contains("Label mismatch:               3")
        streamContent.contains("DBMS mismatch:                1")

        !logContent.contains("UPDATE SUMMARY")

        cleanup:
        CommandUtil.runDropAll(h2)
        if (h2.getConnection() != null) {
            h2.getConnection().close()
        }
    }

    def "validate update summary output is written in both LOG and CONSOLE"() {

        when:
        Map<String, Object> scopeValues = new HashMap<>()
        def outputStream = new ByteArrayOutputStream()
        def logService = new BufferedLogService()
        scopeValues.put(Scope.Attr.logService.name(), logService)
        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/pgsql/update/showSummaryWithLabels.xml")
                commandScope.addArgumentValue(UpdateCommandStep.LABEL_FILTER_ARG, "testtable1")
                commandScope.addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, null)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.VERBOSE)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.ALL)
                commandScope.setOutput(outputStream)
                commandScope.execute()
            }
        })

        then:
        def logContent = logService.getLogAsString(Level.INFO);
        def streamContent = outputStream.toString();

        logContent.contains("UPDATE SUMMARY")
        logContent.contains("Run:                          2")
        logContent.contains("Previously run:               0")
        logContent.contains("Filtered out:                 4")
        logContent.contains("-------------------------------")
        logContent.contains("Total change sets:            6")
        logContent.contains("FILTERED CHANGE SETS SUMMARY")
        logContent.contains("Label mismatch:               3")
        logContent.contains("DBMS mismatch:                1")

        streamContent.contains("UPDATE SUMMARY")
        streamContent.contains("Run:                          2")
        streamContent.contains("Previously run:               0")
        streamContent.contains("Filtered out:                 4")
        streamContent.contains("-------------------------------")
        streamContent.contains("Total change sets:            6")
        streamContent.contains("FILTERED CHANGE SETS SUMMARY")
        streamContent.contains("Label mismatch:               3")
        streamContent.contains("DBMS mismatch:                1")

        cleanup:
        CommandUtil.runDropAll(h2)
        if (h2.getConnection() != null) {
            h2.getConnection().close()
        }
    }


}
