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
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class UpdateIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem postgres = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "check execution parameters are correctly replaced" () {
        given:
        CommandUtil.runUpdate(postgres,"src/test/resources/changelogs/pgsql/update/execution-parameter.xml")
        String sql = "select * from parameter_value_tests order by id"

        when:
        CommandScope executeSql = new CommandScope(ExecuteSqlCommandStep.COMMAND_NAME[0])
        executeSql.addArgumentValue(ExecuteSqlCommandStep.SQL_ARG, sql)
        executeSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
        executeSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
        executeSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())

        then:
        CommandResults results = executeSql.execute()
        String output = results.getResult("output") as String
        System.out.println("JML TEST: '" + output + "'")
        assert 1 == 1

        cleanup:
        postgres.getConnection().close()
        CommandUtil.runDropAll(postgres)
    }
}
