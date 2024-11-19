package liquibase.command.update

import liquibase.Scope
import liquibase.command.CommandResults
import liquibase.command.CommandScope
import liquibase.command.core.ExecuteSqlCommandStep
import liquibase.command.core.HistoryCommandStep
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.ui.ConsoleUIService
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class UpdateIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem testDatabase = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("h2")

    def "check execution parameters are correctly replaced" () {
        given:
        CommandUtil.runDropAll(testDatabase)
        CommandUtil.runUpdate(testDatabase,"src/test/resources/changelogs/h2/update/execution-parameter.xml")
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
ID | CHANGELOG_FILE | CHANGESET_ID | CHANGESET_AUTHOR |
1 | src/test/resources/changelogs/h2/update/execution-parameter/1.xml | 1.1 | jlyle | 
2 | src/test/resources/changelogs/h2/update/execution-parameter/1.xml | 1.2 | not_jlyle | 
3 | src/test/resources/changelogs/h2/update/execution-parameter/1.xml | 1.3 | jlyle | 
4 | src/test/resources/changelogs/h2/update/execution-parameter/2.xml | 2.1 | jlyle | 
5 | src/test/resources/changelogs/h2/update/execution-parameter/2.xml | 2.2 | not_jlyle | 
6 | src/test/resources/changelogs/h2/update/execution-parameter/2.xml | 2.3 | jlyle | 

"""

        cleanup:
        testDatabase.getConnection().close()
        CommandUtil.runDropAll(testDatabase)
    }

    def "check logical file path setting hierarchy" () {
        given:
        CommandUtil.runDropAll(testDatabase)
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

        cleanup:
        testDatabase.getConnection().close()
        CommandUtil.runDropAll(testDatabase)
    }
}
