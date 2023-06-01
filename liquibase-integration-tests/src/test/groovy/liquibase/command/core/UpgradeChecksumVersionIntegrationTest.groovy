package liquibase.command.core

import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.statement.core.RawSqlStatement
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class UpgradeChecksumVersionIntegrationTest extends Specification{
    @Shared
    private DatabaseTestSystem mysql = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mysql")

    def "update command should upgrade all checksums" () {
        def changesetFilepath = "changelogs/common/example-changelog.xml"
        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(mysql.getDatabaseFromFactory())
        changeLogService.init()
        mysql.execute(new RawSqlStatement("""
INSERT INTO DATABASECHANGELOG
(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE, CONTEXTS, LABELS, DEPLOYMENT_ID)
VALUES('1', 'your.name', 'example-changelog.sql', '2023-05-31 14:33:39.108', 1, 'EXECUTED', '8:e3a3ed80b1b6b22ae5e9317c1fd14334', 'sql', 'example comment', NULL, 'DEV', 'example-context', 'example-label', '5561619071');
"""))
        mysql.execute(new RawSqlStatement("""
INSERT INTO DATABASECHANGELOG
(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE, CONTEXTS, LABELS, DEPLOYMENT_ID)
VALUES('2', 'your.name', 'example-changelog.sql', '2023-05-31 14:33:39.118', 2, 'EXECUTED', '8:7db038f9d66960203d8a260e9751a2bd', 'sql', 'example comment', NULL, 'DEV', 'example-context', 'example-label', '5561619071');
"""))

        when:
        def ranChangeSets = changeLogService.getRanChangeSets()

        then:
        ranChangeSets.forEach({ rcs -> rcs.getLastCheckSum().getVersion() == 8 })

        when:
        changeLogService.reset()
        CommandScope updateCommandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, mysql.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, mysql.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, mysql.getPassword())
        updateCommandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.execute()

        then:
        ranChangeSets.forEach({ rcs -> rcs.getLastCheckSum().getVersion() == 9 })

        cleanup:
        CommandUtil.runDropAll(mysql)
    }
}
