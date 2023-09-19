package liquibase.command.core

import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.RanChangeSet
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.statement.core.RawSqlStatement
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@LiquibaseIntegrationTest
class UpgradeChecksumVersionIntegrationTest extends Specification{
    @Shared
    private DatabaseTestSystem mysql = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mysql")

    private List<RanChangeSet> getRanChangesets(ChangeLogHistoryService changeLogService) {
        mysql.getConnection().commit()
        changeLogService.reset()
        return changeLogService.getRanChangeSets()
    }

    private insertDbclRecord(String changelogfile) {
        mysql.execute(new RawSqlStatement("""
INSERT INTO DATABASECHANGELOG
(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE, CONTEXTS, LABELS, DEPLOYMENT_ID)
VALUES('1', 'your.name', '$changelogfile', '2023-05-31 14:33:39.108', 1, 'EXECUTED', '8:d925207397a5bb8863a41d513a65afd1', 'sql', 'example comment', NULL, 'DEV', 'example-context1', 'example-label', '5561619071');
"""))
    }

    @Unroll
    def "update command should upgrade all checksums when no filters supplied" () {
        def changesetFilepath = "changelogs/common/checksum-changelog.xml"
        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(mysql.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(storedFilepathPrefix + changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, mysql.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, mysql.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, mysql.getPassword())
        updateCommandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 2
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 9 })

        cleanup:
        CommandUtil.runDropAll(mysql)

        where:
        storedFilepathPrefix | _
        "" | _
        "classpath:" | _
    }

    def "update command should upgrade only matching changesets when filter is applied" () {
        def changesetFilepath = "changelogs/common/checksum-changelog.xml"
        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(mysql.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, mysql.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, mysql.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, mysql.getPassword())
        updateCommandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, "example-context2")
        updateCommandScope.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 2
        ranChangeSets.get(0).getLastCheckSum().getVersion() == 8
        ranChangeSets.get(1).getLastCheckSum().getVersion() == 9

        cleanup:
        CommandUtil.runDropAll(mysql)
    }

    def "update-to-tag" () {
        def changesetFilepath = "changelogs/common/checksum-changelog-tag.xml"
        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(mysql.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateToTagCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, mysql.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, mysql.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, mysql.getPassword())
        updateCommandScope.addArgumentValue(UpdateToTagCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.addArgumentValue(UpdateToTagCommandStep.TAG_ARG, "version_1.3")
        updateCommandScope.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 3
        ranChangeSets.get(0).getLastCheckSum().getVersion() == 9
        ranChangeSets.get(1).getLastCheckSum().getVersion() == 9
        ranChangeSets.get(2).getLastCheckSum().getVersion() == 9

        cleanup:
        CommandUtil.runDropAll(mysql)
    }

    def "update-count"() {
        def changesetFilepath = "changelogs/common/checksum-changelog.xml"
        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(mysql.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateCountCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, mysql.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, mysql.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, mysql.getPassword())
        updateCommandScope.addArgumentValue(UpdateCountCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.addArgumentValue(UpdateCountCommandStep.COUNT_ARG, 1)
        updateCommandScope.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 2
        ranChangeSets.get(0).getLastCheckSum().getVersion() == 8
        ranChangeSets.get(1).getLastCheckSum().getVersion() == 9

        cleanup:
        CommandUtil.runDropAll(mysql)
    }


    def "update-testing-rollback"() {
        def changesetFilepath = "changelogs/common/checksum-changelog.xml"
        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(mysql.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)
        mysql.executeSql("CREATE TABLE person(id int)")

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateTestingRollbackCommand = new CommandScope(UpdateTestingRollbackCommandStep.COMMAND_NAME)
        updateTestingRollbackCommand.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, mysql.getConnectionUrl())
        updateTestingRollbackCommand.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, mysql.getUsername())
        updateTestingRollbackCommand.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, mysql.getPassword())
        updateTestingRollbackCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateTestingRollbackCommand.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 2
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 9 })

        cleanup:
        CommandUtil.runDropAll(mysql)
    }


    def "update-sql calculates checksum but does not update DBCL"() {
        def changesetFilepath = "changelogs/common/checksum-changelog.xml"
        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(mysql.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, mysql.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, mysql.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, mysql.getPassword())
        updateCommandScope.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        cleanup:
        CommandUtil.runDropAll(mysql)
    }

    def "update-count-sql calculates checksum but does not update DBCL"() {
        def changesetFilepath = "changelogs/common/checksum-changelog.xml"
        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(mysql.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateCountSqlCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, mysql.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, mysql.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, mysql.getPassword())
        updateCommandScope.addArgumentValue(UpdateCountSqlCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.addArgumentValue(UpdateCountSqlCommandStep.COUNT_ARG, 1)
        updateCommandScope.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        cleanup:
        CommandUtil.runDropAll(mysql)
    }

    def "update-to-tag-sql calculates checksum but does not update DBCL"() {
        def changesetFilepath = "changelogs/common/checksum-changelog-tag.xml"
        final ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(mysql.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateToTagSqlCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, mysql.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, mysql.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, mysql.getPassword())
        updateCommandScope.addArgumentValue(UpdateToTagSqlCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.addArgumentValue(UpdateToTagSqlCommandStep.TAG_ARG, "version_1.3")
        updateCommandScope.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        cleanup:
        CommandUtil.runDropAll(mysql)
    }
}
