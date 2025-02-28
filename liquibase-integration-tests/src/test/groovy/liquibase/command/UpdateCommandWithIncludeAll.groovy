package liquibase.command

import liquibase.Contexts
import liquibase.Liquibase
import liquibase.Scope
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.visitor.DefaultChangeExecListener
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.UpdateSqlCommandStep
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.SearchPathResourceAccessor
import org.h2.jdbc.JdbcSQLSyntaxErrorException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@LiquibaseIntegrationTest
class UpdateCommandWithIncludeAll extends Specification {

    @Shared
    private DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    def "run UpdateCommand with an includeAll logicalFilePath"() {
        when:

        String changelogFile = "changelogs/h2/update/logicalFilePath.xml"
        def changelog =
           DatabaseChangelogCommandStep.getDatabaseChangeLog(changelogFile, new ChangeLogParameters(), h2.getDatabaseFromFactory())
        def resourceAccessor = new SearchPathResourceAccessor("target/test-classes")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()) : resourceAccessor
        ]

        Scope.child(scopeSettings, {
            def updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, h2.getDatabaseFromFactory())
            updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_ARG, changelog)
            updateCommand.execute()
        } as Scope.ScopedRunner)
        def resultSet = h2.getConnection().createStatement().executeQuery("select * from databasechangelog")

        then:
        noExceptionThrown()
        resultSet.next()
        resultSet.getString("filename") == "changelogs/h2/update/logicalFilePath.xml"
        while (resultSet.next()) {
            resultSet.getString("filename") == "myLogical"
            if (resultSet.getString("author") == "includeAll") {
                resultSet.getString("id") == "raw_execution-parameter_test.sql"
            }
        }

        cleanup:
        CommandUtil.runDropAll(h2)
    }
}
