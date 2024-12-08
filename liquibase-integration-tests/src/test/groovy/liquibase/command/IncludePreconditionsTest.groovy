package liquibase.command

import liquibase.Scope
import liquibase.changelog.ChangeLogParameters
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.exception.MigrationFailedException
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
import spock.lang.Shared
import spock.lang.Specification

/**
 * Include preconditions test
 * @author Edoardo Patti
 */
@LiquibaseIntegrationTest
class IncludePreconditionsTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    def "run include with preconditions fail option WARN"() {
        when:
        String changelogFile = "changelogs/h2/include/master-warn.xml"
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
        h2.getConnection().createStatement().executeQuery("select * from UNDER_PRECONDITION_TABLE")
        then:
        noExceptionThrown()
        List<String> includedFiles = new ArrayList<>(3)
        while (changelogResultSet.next()) {
            def filename = changelogResultSet.getString("filename")
            includedFiles.add(filename)
        }
        includedFiles.size() == 3
        includedFiles.count("included") == 1
        includedFiles.count("under_precondition") == 1
        includedFiles.count("changelogs/h2/include/master-warn.xml") == 1
    }

    def "run include with preconditions fail option HALT"() {
        when:
        String changelogFile = "changelogs/h2/include/master-halt.xml"
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
        then:
        def commandExecutionException = thrown(liquibase.exception.CommandExecutionException)
        def migrationFailedException = commandExecutionException.getCause().getCause();
        migrationFailedException.class == MigrationFailedException.class
        migrationFailedException.getMessage().contains("Migration failed for changeset changelogs/h2/include/under_precondition.xml::under_precondition::cagliostro")
    }

    def "run include with preconditions fail option CONTINUE"() {
        when:
        String changelogFile = "changelogs/h2/include/master-continue.xml"
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
        then:
        noExceptionThrown()
        List<String> includedFiles = new ArrayList<>(3)
        while (changelogResultSet.next()) {
            def filename = changelogResultSet.getString("filename")
            includedFiles.add(filename)
        }
        includedFiles.size() == 2
        includedFiles.count("included") == 1
        includedFiles.count("changelogs/h2/include/master-continue.xml") == 1
    }

    def "run include with preconditions error option WARN"() {
        when:
        String changelogFile = "changelogs/h2/include/master-err-warn.xml"
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
        h2.getConnection().createStatement().executeQuery("select * from UNDER_PRECONDITION_TABLE")
        then:
        noExceptionThrown()
        List<String> includedFiles = new ArrayList<>(3)
        while (changelogResultSet.next()) {
            def filename = changelogResultSet.getString("filename")
            includedFiles.add(filename)
        }
        includedFiles.size() == 3
        includedFiles.count("included") == 1
        includedFiles.count("under_precondition") == 1
        includedFiles.count("changelogs/h2/include/master-err-warn.xml") == 1
    }

    def "run include with preconditions error option HALT"() {
        when:
        String changelogFile = "changelogs/h2/include/master-err-halt.xml"
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
        then:
        def commandExecutionException = thrown(liquibase.exception.CommandExecutionException)
        def migrationFailedException = commandExecutionException.getCause().getCause();
        migrationFailedException.class == MigrationFailedException.class
        migrationFailedException.getMessage().contains("Migration failed for changeset changelogs/h2/include/under_precondition.xml::under_precondition::cagliostro")
    }

    def "run include with preconditions error option CONTINUE"() {
        when:
        String changelogFile = "changelogs/h2/include/master-err-continue.xml"
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
        then:
        noExceptionThrown()
        List<String> includedFiles = new ArrayList<>(3)
        while (changelogResultSet.next()) {
            def filename = changelogResultSet.getString("filename")
            includedFiles.add(filename)
        }
        includedFiles.size() == 2
        includedFiles.count("included") == 1
        includedFiles.count("changelogs/h2/include/master-err-continue.xml") == 1
    }

    def "run include with preconditions error option MARK_RAN"() {
        when:
        String changelogFile = "changelogs/h2/include/master-err-mark_ran.xml"
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
        def tablesResultSet = h2.getConnection().createStatement().executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES")
        then:
        noExceptionThrown()
        List<String> includedFiles = new ArrayList<>(3)
        List<String> includedTables = new ArrayList<>(40)
        while (changelogResultSet.next()) {
            def filename = changelogResultSet.getString("filename")
            includedFiles.add(filename)
        }
        while (tablesResultSet.next()) {
            def filename = tablesResultSet.getString("TABLE_NAME")
            includedTables.add(filename)
        }
        includedFiles.size() == 3
        includedFiles.count("included") == 1
        includedFiles.count("under_precondition") == 1
        includedFiles.count("changelogs/h2/include/master-err-mark_ran.xml") == 1

        includedTables.count("DEFAULT_TABLE") == 1
        includedTables.count("INCLUDED_TABLE") == 1
        includedTables.count("UNDER_PRECONDITION_TABLE") == 0
    }

}
