package liquibase.command

import liquibase.Scope
import liquibase.changelog.ChangeLogParameters
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
import liquibase.ui.ConsoleUIService
import spock.lang.Shared
import spock.lang.Specification

/**
 * IncludeAll preconditions test
 * @author Edoardo Patti
 */
@LiquibaseIntegrationTest
class IncludeAllPreconditionsTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    def "run includeAll with preconditions"() {
        given:
        ConsoleUIService console = Scope.getCurrentScope().getUI() as ConsoleUIService
        def outputStream = new ByteArrayOutputStream()
        console.setOutputStream(new PrintStream(outputStream))
        when:
        String changelogFile = "changelogs/h2/includeAll/master.xml"
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
        String outputString = outputStream.toString()
        def changelogResultSet = h2.getConnection().createStatement().executeQuery("select * from databasechangelog")
        h2.getConnection().createStatement().executeQuery("select * from INCLUDED_TABLE")
        h2.getConnection().createStatement().executeQuery("select * from DEFAULT_TABLE")
        then:
        noExceptionThrown()
        outputString.contains("WARNING: Executing changelogs/h2/includeAll/master.xml despite precondition failure due to onFail='WARN':")
        outputString.contains("1 preconditions failed")
        outputString.contains("changelogs/h2/includeAll/master.xml : Table PUBLIC.NOT_EXISTANT_TABLE does not exist")
        while (changelogResultSet.next()) {
            def filename = changelogResultSet.getString("filename")
            filename == "changelogs/h2/includeAll/master.xml" || filename == "included";
        }
        cleanup:
        CommandUtil.runDropAll(h2)
    }
}
