package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandResults
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class ExecuteSqlPostgresIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem postgres = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "Can use executeSql using command framework"() {
        given:
        CommandUtil.runUpdate(postgres,"src/test/resources/changelogs/pgsql/update/showSummaryWithLabels.xml")
        String sql = "select * from databasechangelog"

        when:
        CommandScope executeSql = new CommandScope(ExecuteSqlCommandStep.COMMAND_NAME[0])
        executeSql.addArgumentValue(ExecuteSqlCommandStep.SQL_ARG, sql)
        executeSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
        executeSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
        executeSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())

        then:
        CommandResults results = executeSql.execute()
        String output = results.getResult("output") as String
        output.contains("Output of select * from databasechangelog")

        cleanup:
        CommandUtil.runDropAll(postgres)
    }

    def "validate executeSql output display columns in the same order they were specified in the select"() {
        given:
        CommandUtil.runUpdate(postgres,"src/test/resources/changelogs/pgsql/update/showSummaryWithLabels.xml")
        String sql = "select filename, author, exectype, comments, description from databasechangelog order by filename,exectype"

        when:
        CommandScope executeSql = new CommandScope(ExecuteSqlCommandStep.COMMAND_NAME[0])
        executeSql.addArgumentValue(ExecuteSqlCommandStep.SQL_ARG, sql)
        executeSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
        executeSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
        executeSql.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())

        then:
        CommandResults results = executeSql.execute()
        String output = results.getResult("output") as String
        output.contains("Output of select filename, author, exectype, comments, description from databasechangelog order by filename,exectype:")
        output.contains("FILENAME | AUTHOR | EXECTYPE | COMMENTS | DESCRIPTION |")

        cleanup:
        CommandUtil.runDropAll(postgres)
    }
}
