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

    def "run include with preconditions fail option WARN"() {
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
        def changelogResultSet = h2.getConnection().createStatement().executeQuery("select * from databasechangelog")
        h2.getConnection().createStatement().executeQuery("select * from INCLUDED_TABLE")
        h2.getConnection().createStatement().executeQuery("select * from DEFAULT_TABLE")
        h2.getConnection().createStatement().executeQuery("select * from TEST_PRECONDITION_TABLE")
        then:
        noExceptionThrown()
        List<String> includedFiles = new ArrayList<>(3)
        while (changelogResultSet.next()) {
            def filename = changelogResultSet.getString("filename")
            includedFiles.add(filename)
        }
        includedFiles.size() == 3
        includedFiles.count("included") == 1
        includedFiles.count("test_precondition") == 1
        includedFiles.count("changelogs/h2/includeAll/master.xml") == 1
    }
}
