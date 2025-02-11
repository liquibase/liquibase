package liquibase.command

import liquibase.Scope
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.visitor.IncludeVisitor
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.database.core.MockDatabase
import liquibase.exception.PreconditionErrorException
import liquibase.exception.PreconditionFailedException
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.parser.ChangeLogParser
import liquibase.parser.core.xml.XMLChangeLogSAXParser
import liquibase.resource.SearchPathResourceAccessor
import liquibase.test.JUnitResourceAccessor
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
        def runtimeException = thrown(RuntimeException)
        def preconditionFailedException = runtimeException.getCause()
        preconditionFailedException.class == PreconditionFailedException.class
        preconditionFailedException.getMessage().contains("Preconditions Failed")
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

    def "run include with preconditions fail option MARK_RAN"() {
        when:
        String changelogFile = "changelogs/h2/include/master-mark_ran.xml"
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
        includedFiles.count("changelogs/h2/include/master-mark_ran.xml") == 1

        includedTables.count("DEFAULT_TABLE") == 1
        includedTables.count("INCLUDED_TABLE") == 1
        includedTables.count("UNDER_PRECONDITION_TABLE") == 0
    }

    def "run include with preconditions fail option MARK_RAN_RECURSIVE"() {
        when:
        String changelogFile = "changelogs/h2/include/master-mark_ran-recursive.xml"
        def changelog =
                DatabaseChangelogCommandStep.getDatabaseChangeLog(changelogFile, new ChangeLogParameters(), h2.getDatabaseFromFactory())
        def resourceAccessor = new SearchPathResourceAccessor("target/test-classes")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()) : resourceAccessor,
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
        includedFiles.size() == 7
        includedFiles.count("included") == 1
        includedFiles.count("included1") == 1
        includedFiles.count("included2") == 1
        includedFiles.count("included3") == 1
        includedFiles.count("include-container") == 1
        includedFiles.count("changelogs/h2/include/master-mark_ran-recursive.xml") == 1
        includedFiles.count("includeAll") == 1

        includedTables.count("DEFAULT_TABLE") == 1
        includedTables.count("INCLUDED_TABLE") == 1
        includedTables.count("INCLUDE_CONTAINER_TABLE") == 0
        includedTables.count("INCLUDED_TABLE_1") == 0
        includedTables.count("INCLUDED_TABLE_2") == 0
        includedTables.count("INCLUDED_TABLE_3") == 0
        includedTables.count("TEST_PRECONDITION_TABLE") == 0
    }

    def "assert changesets MARK_RUN state precondition fail case"() {
        when:
        def resourceAccessor = new JUnitResourceAccessor()

        def scopeSettings = [
                (Scope.Attr.database.name()) : h2.getDatabaseFromFactory(),
        ]
        Scope.enter(scopeSettings)

        def changeLogFile = "changelogs/h2/include/master-mark_ran-recursive.xml"

        ChangeLogParser parser = new XMLChangeLogSAXParser()

        DatabaseChangeLog dbChangeLog = parser.parse(changeLogFile, new ChangeLogParameters(), resourceAccessor)

        new IncludeVisitor().visit(dbChangeLog)
        def changesets = dbChangeLog.getChangeSets()
        def actual = new ArrayList(4)
        then:
        changesets.forEach {
            c ->
                def execType = c.execute(dbChangeLog, new MockDatabase())
                if(execType == ChangeSet.ExecType.MARK_RAN)
                    actual.add(c.getLogicalFilePath())
        }
        actual.size() == 5
        actual.count("include-container") == 1
        actual.count("included1") == 1
        actual.count("included2") == 1
        actual.count("included3") == 1
        actual.count("includeAll") == 1
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
        def runtimeException = thrown(RuntimeException)
        def preconditionErrorException = runtimeException.getCause();
        preconditionErrorException.class == PreconditionErrorException.class
        preconditionErrorException.getMessage().contains("Precondition Error")
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

    def "assert changesets MARK_RUN state precondition error case"() {
        when:
        def resourceAccessor = new JUnitResourceAccessor()

        def scopeSettings = [
                (Scope.Attr.database.name()) : h2.getDatabaseFromFactory(),
        ]
        Scope.enter(scopeSettings)

        def changeLogFile = "changelogs/h2/include/master-err-mark_ran.xml"

        ChangeLogParser parser = new XMLChangeLogSAXParser()

        DatabaseChangeLog dbChangeLog = parser.parse(changeLogFile, new ChangeLogParameters(), resourceAccessor)

        new IncludeVisitor().visit(dbChangeLog)
        def changesets = dbChangeLog.getChangeSets()
        def actual = new ArrayList(4)
        then:
        changesets.forEach {
            c ->
                def execType = c.execute(dbChangeLog, new MockDatabase())
                if(execType == ChangeSet.ExecType.MARK_RAN)
                    actual.add(c.getLogicalFilePath())
        }
        actual.size() == 1
        actual.count("under_precondition") == 1
    }

    def "run include with preconditions using a YAML changelog format"() {
        when:
        String changelogFile = "changelogs/h2/include/yaml/include-container.yaml"
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
        h2.getConnection().createStatement().executeQuery("select * from INCLUDEPRECONDITIONTEST")
        h2.getConnection().createStatement().executeQuery("select * from INCLUDEALLPRECONDITIONTEST")
        h2.getConnection().createStatement().executeQuery("select * from TEST_PRECONDITION_TABLE")
        then:
        noExceptionThrown()
        List<String> includedFiles = new ArrayList<>(3)
        while (changelogResultSet.next()) {
            def filename = changelogResultSet.getString("filename")
            includedFiles.add(filename)
        }
        includedFiles.size() == 3
        includedFiles.count("included1") == 1
        includedFiles.count("includeAll") == 1
        includedFiles.count("changelogs/h2/include/yaml/include-container.yaml") == 1
    }

    def "run include with preconditions using a JSON changelog format"() {
        when:
        String changelogFile = "changelogs/h2/include/json/include-container.json"
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
        h2.getConnection().createStatement().executeQuery("select * from INCLUDEPRECONDITIONTEST")
        h2.getConnection().createStatement().executeQuery("select * from INCLUDEALLPRECONDITIONTEST")
        h2.getConnection().createStatement().executeQuery("select * from TEST_PRECONDITION_TABLE")
        then:
        noExceptionThrown()
        List<String> includedFiles = new ArrayList<>(3)
        while (changelogResultSet.next()) {
            def filename = changelogResultSet.getString("filename")
            includedFiles.add(filename)
        }
        includedFiles.size() == 3
        includedFiles.count("included1") == 1
        includedFiles.count("includeAll") == 1
        includedFiles.count("changelogs/h2/include/json/include-container.json") == 1
    }

}
