package liquibase.command.update

import liquibase.Scope
import liquibase.UpdateSummaryEnum
import liquibase.UpdateSummaryOutputEnum
import liquibase.command.CommandResults
import liquibase.command.CommandScope
import liquibase.command.core.ExecuteSqlCommandStep
import liquibase.command.core.HistoryCommandStep
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.core.helpers.ShowSummaryArgument
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.logging.core.BufferedLogService
import liquibase.resource.SearchPathResourceAccessor
import spock.lang.Shared
import spock.lang.Specification

import java.util.logging.Level

@LiquibaseIntegrationTest
class UpdateIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem testDatabase = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("h2")

    def "check execution parameters are correctly replaced" () {
        given:
        CommandUtil.runUpdate(testDatabase,"src/test/resources/changelogs/h2/update/execution-parameter.xml")
        String expectedDeploymentId = Scope.getCurrentScope().getDeploymentId();
        String sql = "select * from parameter_value_tests order by id"

        when:
        CommandScope executeSql = new CommandScope(ExecuteSqlCommandStep.COMMAND_NAME[0])
        executeSql.addArgumentValue(ExecuteSqlCommandStep.SQL_ARG, sql)
        executeSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, testDatabase.getConnectionUrl())
        executeSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, testDatabase.getUsername())
        executeSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, testDatabase.getPassword())

        CommandResults results = executeSql.execute()
        String output = results.getResult("output") as String

        then:
        assert output == """Output of select * from parameter_value_tests order by id:
ID | DEPLOYMENT_ID | CHANGELOG_FILE | CHANGESET_ID | CHANGESET_AUTHOR |
1 | """ + expectedDeploymentId + """ | src/test/resources/changelogs/h2/update/execution-parameter/1.xml | 1.1 | jlyle | 
2 | """ + expectedDeploymentId + """ | src/test/resources/changelogs/h2/update/execution-parameter/1.xml | 1.2 | not_jlyle | 
3 | """ + expectedDeploymentId + """ | src/test/resources/changelogs/h2/update/execution-parameter/1.xml | 1.3 | jlyle | 
4 | """ + expectedDeploymentId + """ | src/test/resources/changelogs/h2/update/execution-parameter/2.xml | 2.1 | jlyle | 
5 | """ + expectedDeploymentId + """ | src/test/resources/changelogs/h2/update/execution-parameter/2.xml | 2.2 | not_jlyle | 
6 | """ + expectedDeploymentId + """ | src/test/resources/changelogs/h2/update/execution-parameter/2.xml | 2.3 | jlyle | 

"""
    }

    def "check logical file path setting hierarchy" () {
        given:
        CommandUtil.runUpdate(testDatabase,"src/test/resources/changelogs/h2/update/master.xml")

        when:
        def outputStream = new ByteArrayOutputStream()

        CommandScope history = new CommandScope(HistoryCommandStep.COMMAND_NAME[0])
        history.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, testDatabase.getConnectionUrl())
        history.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, testDatabase.getUsername())
        history.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, testDatabase.getPassword())
        history.setOutput(outputStream)
        history.execute()
        String outputBuffer = outputStream.toString()

        then:
        outputBuffer != null
        assert outputBuffer.contains("src/test/resources/changelogs/h2/update/master.xml | jlyle            | 1")
        assert outputBuffer.contains("myLogical                                          | jlyle            | 2")
        assert outputBuffer.contains("changeSetLogical                                   | jlyle            | 3")
    }

    def "output change type only outputs single time to console" () {
        given:
        def changelogFileName = "src/test/resources/changelogs/h2/update/output.xml"
        def targetString = "INFO Some output text to identify"
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
        def bufferLog = new BufferedLogService()
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()) : resourceAccessor,
                (Scope.Attr.logService.name()) : bufferLog,
        ]
        def infoLog
        def firstIndex
        def lastIndex

        when:
        Scope.child(scopeSettings, {
            CommandUtil.runUpdate(testDatabase, changelogFileName, null, null, null)
        } as Scope.ScopedRunner)
        infoLog = bufferLog.getLogAsString(Level.INFO)
        firstIndex = infoLog.indexOf(targetString)
        lastIndex = infoLog.lastIndexOf(targetString)

        then:
        // Ensure output exits in log at least once
        firstIndex != -1

        //Ensure output exits in log at most once
        firstIndex == lastIndex
    }

    def "No change sets marked as ran if all SQL changes have DBMS mismatch"() {
        def changelogFile = "target/test-classes/changelogs/multiple-sql-changes-none-ran.yaml"
        when:

        Map<String, Object> scopeValues = new HashMap<>()
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes", Scope.getCurrentScope().getResourceAccessor())
        scopeValues.put(Scope.Attr.resourceAccessor.name(), resourceAccessor)
        OutputStream os = new ByteArrayOutputStream()
        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, testDatabase.getConnectionUrl())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, testDatabase.getUsername())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, testDatabase.getPassword())
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelogFile)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.VERBOSE)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.CONSOLE)
                commandScope.setOutput(os)
                try {
                    commandScope.execute()
                } catch (Exception ignored) {

                }
            }
        })

        then:
        def console = os.toString()
        console.contains("UPDATE SUMMARY")
        console.contains("Run:                          0")
        console.contains("Previously run:               0")
        console.contains("Filtered out:                 0")
        console.contains("-------------------------------")
        console.contains("Total change sets:            0")

        when:
        def outputStream = new ByteArrayOutputStream()
        CommandScope history = new CommandScope(HistoryCommandStep.COMMAND_NAME[0])
        history.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, testDatabase.getConnectionUrl())
        history.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, testDatabase.getUsername())
        history.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, testDatabase.getPassword())
        history.setOutput(outputStream)
        history.execute()
        String outputBuffer = outputStream.toString()

        then:
        outputBuffer != null
        outputBuffer.contains("No changesets deployed")
    }

    def "Changeset should be marked as ran if not all SQL changes have DBMS mismatch"() {
        def changelogFile = "target/test-classes/changelogs/multiple-sql-changes-one-ran.yaml"
        when:

        Map<String, Object> scopeValues = new HashMap<>()
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes", Scope.getCurrentScope().getResourceAccessor())
        scopeValues.put(Scope.Attr.resourceAccessor.name(), resourceAccessor)
        OutputStream os = new ByteArrayOutputStream()
        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, testDatabase.getConnectionUrl())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, testDatabase.getUsername())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, testDatabase.getPassword())
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelogFile)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.VERBOSE)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.CONSOLE)
                commandScope.setOutput(os)
                try {
                    commandScope.execute()
                } catch (Exception ignored) {

                }
            }
        })

        then:
        def console = os.toString()
        console.contains("UPDATE SUMMARY")
        console.contains("Run:                          1")
        console.contains("Previously run:               0")
        console.contains("Filtered out:                 0")
        console.contains("-------------------------------")
        console.contains("Total change sets:            1")

        when:
        def outputStream = new ByteArrayOutputStream()
        CommandScope history = new CommandScope(HistoryCommandStep.COMMAND_NAME[0])
        history.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, testDatabase.getConnectionUrl())
        history.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, testDatabase.getUsername())
        history.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, testDatabase.getPassword())
        history.setOutput(outputStream)
        history.execute()
        String outputBuffer = outputStream.toString()

        then:
        outputBuffer != null
        assert outputBuffer.contains("target/test-classes/changelogs/multiple-sql-changes-one-ran.yaml")
        assert outputBuffer.contains("nvoxland")
    }

    def "Changeset should be marked as ran if all SQL changes with DBMS run"() {
        def changelogFile = "target/test-classes/changelogs/multiple-sql-changes-all-ran.yaml"
        when:

        Map<String, Object> scopeValues = new HashMap<>()
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes", Scope.getCurrentScope().getResourceAccessor())
        scopeValues.put(Scope.Attr.resourceAccessor.name(), resourceAccessor)
        OutputStream os = new ByteArrayOutputStream()
        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, testDatabase.getConnectionUrl())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, testDatabase.getUsername())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, testDatabase.getPassword())
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelogFile)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.VERBOSE)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.CONSOLE)
                commandScope.setOutput(os)
                try {
                    commandScope.execute()
                } catch (Exception ignored) {

                }
            }
        })

        then:
        def console = os.toString()
        console.contains("UPDATE SUMMARY")
        console.contains("Run:                          1")
        console.contains("Previously run:               0")
        console.contains("Filtered out:                 0")
        console.contains("-------------------------------")
        console.contains("Total change sets:            1")

        when:
        def outputStream = new ByteArrayOutputStream()
        CommandScope history = new CommandScope(HistoryCommandStep.COMMAND_NAME[0])
        history.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, testDatabase.getConnectionUrl())
        history.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, testDatabase.getUsername())
        history.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, testDatabase.getPassword())
        history.setOutput(outputStream)
        history.execute()
        String outputBuffer = outputStream.toString()

        then:
        outputBuffer != null
        assert outputBuffer.contains("target/test-classes/changelogs/multiple-sql-changes-all-ran.yaml")
        assert outputBuffer.contains("nvoxland")
    }
}
