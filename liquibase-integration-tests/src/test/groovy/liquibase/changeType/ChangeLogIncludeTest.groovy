package liquibase.changeType

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.UpdateSummaryEnum
import liquibase.UpdateSummaryOutputEnum
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.core.helpers.ShowSummaryArgument
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.logging.core.BufferedLogService
import spock.lang.Shared
import spock.lang.Specification

import java.util.logging.Level

@LiquibaseIntegrationTest
class ChangeLogIncludeTest extends Specification {

    @Shared
    private DatabaseTestSystem db = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("h2")

    def "make sure correct path is set according to each logicalFilePath set on the different levels"() {

        given:
        def logService = new BufferedLogService()
        Map<String, Object> scopeValues = new HashMap<>()
        scopeValues.put(Scope.Attr.logService.name(), logService)
        scopeValues.put(GlobalConfiguration.ALLOW_INHERIT_LOGICAL_FILE_PATH.getKey(), false)

        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnectionUrl())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, db.getUsername())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, db.getPassword())
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/changeType/include/changelog.xml")
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.VERBOSE)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.LOG)
                commandScope.execute()
            }
        })

        when:
        def logContent = logService.getLogAsString(Level.INFO)

        then:
        // Changelogs with explicit logicalFilePath use it
        logContent.contains("ChangeSet page-service-changelog::initial_db_setup::dev")
        // Changelogs WITHOUT logicalFilePath use their physical path (not parent's)
        logContent.contains("ChangeSet changelogs/changeType/include/changelog-users.xml::initial_db_setup::dev")
        logContent.contains("ChangeSet changelogs/changeType/include/changelog-teams.xml::initial_db_setup::dev")
        // Changesets with explicit logicalFilePath use it
        logContent.contains("ChangeSet myownlfp::myown::dev")
        // Changesets WITHOUT logicalFilePath use changelog's physical path
        logContent.contains("ChangeSet changelogs/changeType/include/changelog-level3.xml::myown::dev")
    }

    def "upgrading from 4.31.1 to 4.31.1+ using logicalFilePath in includes works and fixes dbcl"() {

        given:
        // create dbcl table
        def changelogFile = "changelogs/changeType/logicalfilepath/changelog.xml"
        CommandUtil.runStatus(db, changelogFile)

        // populate it with 4.31.0 generated entries
        db.executeSql("""
insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id)
values (1,'dev','main_changelog_level','2025-02-07 13:56:14',1,'EXECUTED','9:6dc9d0ae449f51b7b5fa3824e082dc58','createTable tableName=table1','',null,'4.31.0',null,null,8947372856);
    """)

        and:
        def logService = new BufferedLogService()
        Map<String, Object> scopeValues = new HashMap<>()
        scopeValues.put(Scope.Attr.logService.name(), logService)

        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                def commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnectionUrl())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, db.getUsername())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, db.getPassword())
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelogFile)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.VERBOSE)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.LOG)
                commandScope.execute()
            }
        })

        when:
        def logContent = logService.getLogAsString(Level.INFO)

        then:
        logContent.contains("Replacing path in databasechangelog table for changeset [main_changelog_level::1::dev] by [included_changelog_level]")

        when:
        def resultSet = db.getConnection().createStatement().executeQuery("select count(1) from databasechangelog where filename = 'included_changelog_level'")
        resultSet.next()

        then:
        resultSet.getInt(1) == 1
    }

    def "included changelogs without logicalFilePath use physical paths when allowInheritLogicalFilePath=false"() {

        given:
        def logService = new BufferedLogService()
        Map<String, Object> scopeValues = new HashMap<>()
        scopeValues.put(Scope.Attr.logService.name(), logService)
        scopeValues.put(GlobalConfiguration.ALLOW_INHERIT_LOGICAL_FILE_PATH.getKey(), false)

        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnectionUrl())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, db.getUsername())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, db.getPassword())
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/changeType/logicalFilePathTestData/issue-7222-parent.yaml")
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.VERBOSE)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.LOG)
                commandScope.execute()
            }
        })

        when:
        def logContent = logService.getLogAsString(Level.INFO)

        then:
        // Both changesets should use their actual file paths, not the parent's logicalFilePath
        logContent.contains("ChangeSet changelogs/changeType/logicalFilePathTestData/issue-7222-child1.yaml::1::testauthor")
        logContent.contains("ChangeSet changelogs/changeType/logicalFilePathTestData/issue-7222-child2.yaml::1::testauthor")
        // Should NOT contain the parent's logicalFilePath
        !logContent.contains("ChangeSet parent-logical-path::1::testauthor")

        when:
        def resultSet = db.getConnection().createStatement().executeQuery(
            "select filename from databasechangelog where id = '1' and author = 'testauthor' order by filename"
        )
        def filenames = []
        while (resultSet.next()) {
            filenames.add(resultSet.getString(1))
        }

        then:
        filenames.size() == 2
        filenames[0] == "changelogs/changeType/logicalFilePathTestData/issue-7222-child1.yaml"
        filenames[1] == "changelogs/changeType/logicalFilePathTestData/issue-7222-child2.yaml"
    }

    def "included changelogs without logicalFilePath inherit parent's logicalFilePath when allowInheritLogicalFilePath=true (legacy behavior)"() {

        given:
        def logService = new BufferedLogService()
        Map<String, Object> scopeValues = new HashMap<>()
        scopeValues.put(Scope.Attr.logService.name(), logService)
        scopeValues.put(GlobalConfiguration.ALLOW_INHERIT_LOGICAL_FILE_PATH.getKey(), true)

        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnectionUrl())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, db.getUsername())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, db.getPassword())
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/changeType/logicalFilePathTestData/issue-7222-legacy-parent.yaml")
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.VERBOSE)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.LOG)
                commandScope.execute()
            }
        })

        when:
        def logContent = logService.getLogAsString(Level.INFO)

        then:
        // Both changesets should inherit the parent's logicalFilePath
        logContent.contains("ChangeSet parent-logical-path::legacy_child1::testauthor")
        logContent.contains("ChangeSet parent-logical-path::legacy_child2::testauthor")
        // Should NOT contain physical file paths
        !logContent.contains("ChangeSet changelogs/changeType/logicalFilePathTestData/issue-7222-legacy-child1.yaml")
        !logContent.contains("ChangeSet changelogs/changeType/logicalFilePathTestData/issue-7222-legacy-child2.yaml")

        when:
        def resultSet = db.getConnection().createStatement().executeQuery(
            "select filename from databasechangelog where author = 'testauthor' and (id = 'legacy_child1' or id = 'legacy_child2') order by id"
        )
        def filenames = []
        while (resultSet.next()) {
            filenames.add(resultSet.getString(1))
        }

        then:
        filenames.size() == 2
        filenames[0] == "parent-logical-path"
        filenames[1] == "parent-logical-path"
    }

    def "include statement with explicit logicalFilePath uses that value directly, not parent's logicalFilePath"() {

        given:
        def logService = new BufferedLogService()
        Map<String, Object> scopeValues = new HashMap<>()
        scopeValues.put(Scope.Attr.logService.name(), logService)

        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnectionUrl())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, db.getUsername())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, db.getPassword())
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/changeType/logicalFilePathTestData/explicit-include-parent.yaml")
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.VERBOSE)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.LOG)
                commandScope.execute()
            }
        })

        when:
        def logContent = logService.getLogAsString(Level.INFO)

        then:
        // Changeset should use the explicit logicalFilePath from the include statement
        logContent.contains("ChangeSet explicit-include-path::explicit_test::testauthor")
        // Should NOT use parent's logicalFilePath
        !logContent.contains("ChangeSet parent-logical-path::explicit_test::testauthor")
        // Should NOT use physical file path
        !logContent.contains("ChangeSet changelogs/changeType/logicalFilePathTestData/explicit-include-child.yaml::explicit_test::testauthor")

        when:
        def resultSet = db.getConnection().createStatement().executeQuery(
            "select filename from databasechangelog where id = 'explicit_test' and author = 'testauthor'"
        )
        def filename = null
        if (resultSet.next()) {
            filename = resultSet.getString(1)
        }

        then:
        filename == "explicit-include-path"
    }

    def "child changelog's own logicalFilePath takes highest priority over include statement and parent"() {

        given:
        def logService = new BufferedLogService()
        Map<String, Object> scopeValues = new HashMap<>()
        scopeValues.put(Scope.Attr.logService.name(), logService)

        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnectionUrl())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, db.getUsername())
                commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, db.getPassword())
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/changeType/logicalFilePathTestData/child-with-own-lfp-parent.yaml")
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.VERBOSE)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.LOG)
                commandScope.execute()
            }
        })

        when:
        def logContent = logService.getLogAsString(Level.INFO)

        then:
        // Changeset should use the child changelog's own logicalFilePath (highest priority)
        logContent.contains("ChangeSet child-own-logical-path::child_own_test::testauthor")
        // Should NOT use include statement's logicalFilePath
        !logContent.contains("ChangeSet include-statement-path::child_own_test::testauthor")
        // Should NOT use parent's logicalFilePath
        !logContent.contains("ChangeSet parent-logical-path::child_own_test::testauthor")

        when:
        def resultSet = db.getConnection().createStatement().executeQuery(
            "select filename from databasechangelog where id = 'child_own_test' and author = 'testauthor'"
        )
        def filename = null
        if (resultSet.next()) {
            filename = resultSet.getString(1)
        }

        then:
        filename == "child-own-logical-path"
    }
}
