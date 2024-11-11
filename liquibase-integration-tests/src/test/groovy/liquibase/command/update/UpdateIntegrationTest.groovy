package liquibase.command.update

import liquibase.Scope
import liquibase.command.CommandResults
import liquibase.command.CommandScope
import liquibase.command.core.ExecuteSqlCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
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
    }

    def "output change type only outputs single time to console" () {
        given:
        def changelogFileName = "src/test/resources/changelogs/h2/update/output.xml"
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
        def bufferLog = new BufferedLogService()
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()) : resourceAccessor,
                (Scope.Attr.logService.name()) : bufferLog,
        ]

        when:
        Scope.child(scopeSettings, {
            CommandUtil.runUpdate(testDatabase, changelogFileName, null, null, null)
        } as Scope.ScopedRunner)

        then:
        bufferLog.getLogAsString(Level.INFO).contains("INFO Output: Some output text to identify")

        cleanup:
        testDatabase.getConnection().close()
    }


}
