package liquibase.command

import liquibase.Contexts
import liquibase.Liquibase
import liquibase.Scope
import liquibase.changelog.visitor.DefaultChangeExecListener
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.UpdateSqlCommandStep
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.ClassLoaderResourceAccessor
import org.h2.jdbc.JdbcSQLSyntaxErrorException
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class UpdateCommandsIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    def "run UpdateSql from CommandStep"() {
        when:
        def updateSqlCommand = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME)
        updateSqlCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, h2.getDatabaseFromFactory())
        updateSqlCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "liquibase/update-tests.yml")

        then:
        def result = updateSqlCommand.execute().getResults()
        def a = ((DefaultChangeExecListener)result.get("defaultChangeExecListener"))
        a.getDeployedChangeSets().get(0).getId() == "1"

        when:
        h2.getConnection().createStatement().executeQuery("select count(1) from databasechangelog")

        then:
        final JdbcSQLSyntaxErrorException exception = thrown()
        exception.message.contains("Table \"DATABASECHANGELOG\" not found")

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def "run Update from CommandStep"() {
        when:
        def updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        updateCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, h2.getDatabaseFromFactory())
        updateCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "liquibase/update-tests.yml")

        and:
        updateCommand.execute()

        then:
        def resultSet = h2.getConnection().createStatement().executeQuery("select count(1) from databasechangelog")
        resultSet.next()
        resultSet.getInt(1) == 1

        def detailsResultSet = h2.getConnection().createStatement().executeQuery("select DEPLOYMENT_ID from databasechangelog")
        detailsResultSet.next()
        assert detailsResultSet.getString(1) != null : "No deployment ID found for the update"

        def rsTableExist = h2.getConnection().createStatement().executeQuery("select count(1) from example_table")
        rsTableExist.next()
        rsTableExist.getInt(1) == 0

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def "run Update from Liquibase class"() {
        when:
        def liquibase = new Liquibase("liquibase/update-tests.yml", new ClassLoaderResourceAccessor(), h2.getDatabaseFromFactory())
        liquibase.update(new Contexts())

        then:
        def resultSet = h2.getConnection().createStatement().executeQuery("select count(1) from databasechangelog")
        resultSet.next()
        resultSet.getInt(1) == 1

        def rsTableExist = h2.getConnection().createStatement().executeQuery("select count(1) from example_table")
        rsTableExist.next()
        rsTableExist.getInt(1) == 0

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def "run Update from Liquibase class using print writer"() {
        when:
        def liquibase = new Liquibase("liquibase/update-tests.yml", new ClassLoaderResourceAccessor(), h2.getDatabaseFromFactory())
        liquibase.update(new Contexts(), new PrintWriter(System.out))
        h2.getConnection().createStatement().executeQuery("select count(1) from databasechangelog")

        then:
        final JdbcSQLSyntaxErrorException exception = thrown()
        exception.message.contains("Table \"DATABASECHANGELOG\" not found")
    }
}
