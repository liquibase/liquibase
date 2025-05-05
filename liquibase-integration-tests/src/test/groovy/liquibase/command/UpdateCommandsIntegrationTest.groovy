package liquibase.command

import liquibase.Contexts
import liquibase.Liquibase
import liquibase.Scope
import liquibase.UpdateSummaryEnum
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.visitor.DefaultChangeExecListener
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.UpdateSqlCommandStep
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.core.helpers.ShowSummaryArgument
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.SearchPathResourceAccessor
import liquibase.util.FileUtil
import org.h2.jdbc.JdbcSQLSyntaxErrorException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@LiquibaseIntegrationTest
class UpdateCommandsIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    def "run UpdateSql from CommandStep"() {
        when:
        def updateSqlCommand = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME)
        updateSqlCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, h2.getDatabaseFromFactory())
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
    }

    @Unroll
    def "run Update from CommandStep"() {
        when:

        def resourceAccessor = new SearchPathResourceAccessor("target/test-classes")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()) : resourceAccessor
        ]

        Scope.child(scopeSettings, {
            def updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, h2.getDatabaseFromFactory())
            if (changelog instanceof DatabaseChangeLog) {
                updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_ARG, changelog)
            } else {
                updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelog)
            }

            updateCommand.execute()
        } as Scope.ScopedRunner)

        then:
        def resultSet = h2.getConnection().createStatement().executeQuery("select count(1) from databasechangelog")
        resultSet.next()
        resultSet.getInt(1) == 1

        def detailsResultSet = h2.getConnection().createStatement().executeQuery("select DEPLOYMENT_ID from databasechangelog")
        detailsResultSet.next()
        assert detailsResultSet.getString(1) != null: "No deployment ID found for the update"

        def rsTableExist = h2.getConnection().createStatement().executeQuery("select count(1) from example_table")
        rsTableExist.next()
        rsTableExist.getInt(1) == 0

        where:
        changelog                                                                                                                               | _
        "liquibase/update-tests.yml"                                                                                                            | _
        DatabaseChangelogCommandStep.getDatabaseChangeLog("liquibase/update-tests.yml", new ChangeLogParameters(), h2.getDatabaseFromFactory()) | _
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
    }

    def "run Update from Liquibase class using print writer"() {
        when:
        def liquibase = new Liquibase("liquibase/update-tests.yml", new ClassLoaderResourceAccessor(), h2.getDatabaseFromFactory())
        liquibase.updateSql(new Contexts(), null, new PrintWriter(System.out))
        h2.getConnection().createStatement().executeQuery("select count(1) from databasechangelog")

        then:
        final JdbcSQLSyntaxErrorException exception = thrown()
        exception.message.contains("Table \"DATABASECHANGELOG\" not found")
    }

    def "column value set with nullPlaceholder should be null"() {
        given:
        def outputFileName = "update-sql.output.mysql.sql"
        def outputFile = new File(outputFileName)
        def outputStream = new FileOutputStream(outputFile)
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): resourceAccessor
        ]
        when:
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
            commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/mysql/complete/nullPlaceholder.test.changelog.xml")
            commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.SUMMARY)
            commandScope.setOutput(outputStream)

            commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)

        then:
        def outputFileContent = FileUtil.getContents(outputFile)
        //As can be seen in the below assertion, we are verifying nullPlaceholder set is translated as a NULL column value
        outputFileContent.containsIgnoreCase("INSERT INTO public.nullPlaceholderTable (colKey, col2) VALUES (1+1, NULL);")
        outputFileContent.containsIgnoreCase("INSERT INTO public.nullPlaceholderTable (colKey, col2) VALUES (5, 'there');")

        cleanup:
        outputStream.close()
        outputFile.delete()
    }
}
