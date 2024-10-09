package liquibase.command.update

import liquibase.Scope
import liquibase.command.CommandResults
import liquibase.command.CommandScope
import liquibase.command.core.ExecuteSqlCommandStep
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.logging.core.BufferedLogService
import liquibase.ui.ConsoleUIService
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

    def "jmltodo" () {
        //ATTEMPT 1
        given:
        def outputStream = new ByteArrayOutputStream()
        def commandResults

        when:
        BufferedLogService bufferLog = new BufferedLogService()

        Scope.child([
                (Scope.Attr.logService.name()) : bufferLog,
        ], {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, "test2")
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, testDatabase.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, testDatabase.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, testDatabase.getPassword())
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "src/test/resources/changelogs/h2/update/output.xml")
            commandScope.setOutput(outputStream)
            commandResults = commandScope.execute()
        } as Scope.ScopedRunner)

        System.out.println("JMLTEST")
        System.out.println(bufferLog.getLogAsString(Level.SEVERE))

//        //ATTEMPT 2
//        given:
//        Scope.Attr.logService.name()
//        def changelogFile = 'src/test/resources/changelogs/h2/update/output.xml'
//        ConsoleUIService console = Scope.getCurrentScope().getUI() as ConsoleUIService
//        def outputStream = new ByteArrayOutputStream()
//        console.setOutputStream(new PrintStream(outputStream))
//
//        when:
//        CommandUtil.runUpdate(testDatabase, changelogFile)
//        String outputString = outputStream.toString()
//        System.out.println("JMLTEST1")
//        System.out.println(outputString)

//        then:
//        noExceptionThrown()
//        assert outputString.contains("Rolling Back Changeset: target/test-classes/changelogs/pgsql/rollback/rollback-to-tag-changelog.xml::4b::createTable::Liquibase Pro User")
//        assert outputString.contains("Rolling Back Changeset: target/test-classes/changelogs/pgsql/rollback/rollback-to-tag-changelog.xml::13.2::testuser")

        then:
        1==0

        cleanup:
        testDatabase.getConnection().close()
        CommandUtil.runDropAll(testDatabase)
    }


}
