package liquibase.changeType

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
        logContent.contains("ChangeSet page-service-changelog::initial_db_setup::dev")
        logContent.contains("ChangeSet mid::initial_db_setup::dev")
        logContent.contains("ChangeSet app-changelog::initial_db_setup::dev")
        logContent.contains("ChangeSet myownlfp::myown::dev")
        logContent.contains("ChangeSet app-changelog::myown::dev")
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
}
