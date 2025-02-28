package liquibase.command.core

import liquibase.ChecksumVersion
import liquibase.Scope
import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.ChangeSet
import liquibase.changelog.RanChangeSet
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.statement.SqlStatement
import liquibase.statement.core.MarkChangeSetRanStatement
import liquibase.statement.core.RawParameterizedSqlStatement
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@LiquibaseIntegrationTest
class UpgradeChecksumVersionIntegrationTest extends Specification{
    @Shared
    private DatabaseTestSystem h2 = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("h2")

    private List<RanChangeSet> getRanChangesets(ChangeLogHistoryService changeLogService) {
        h2.getConnection().commit()
        changeLogService.reset()
        return changeLogService.getRanChangeSets()
    }

    private insertDbclRecord(String changelogfile) {
        h2.execute(new RawParameterizedSqlStatement("""
INSERT INTO DATABASECHANGELOG
(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE, CONTEXTS, LABELS, DEPLOYMENT_ID)
VALUES('1', 'your.name', '$changelogfile', '2023-05-31 14:33:39.108', 1, 'EXECUTED', '8:d925207397a5bb8863a41d513a65afd1', 'sql', 'example comment', NULL, 'DEV', 'example-context1', 'example-label', '5561619071');
"""))
    }

    @Unroll
    def "update command should upgrade all checksums when no filters supplied" () {
        def changesetFilepath = "changelogs/common/checksum-changelog.xml"
        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(h2.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(storedFilepathPrefix + changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
        updateCommandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 2
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 9 })

        cleanup:
        CommandUtil.runDropAll(h2)

        where:
        storedFilepathPrefix | _
        "" | _
        "classpath:" | _
    }

    def "update command should upgrade only matching changesets when filter is applied" () {
        def changesetFilepath = "changelogs/common/checksum-changelog.xml"
        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(h2.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
        updateCommandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, "example-context2")
        updateCommandScope.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 2
        ranChangeSets.get(0).getLastCheckSum().getVersion() == 8
        ranChangeSets.get(1).getLastCheckSum().getVersion() == 9

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def "update-to-tag" () {
        def changesetFilepath = "changelogs/common/checksum-changelog-tag.xml"
        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(h2.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateToTagCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
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
        CommandUtil.runDropAll(h2)
    }

    def "update-count"() {
        def changesetFilepath = "changelogs/common/checksum-changelog.xml"
        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(h2.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandUtil.runUpdateCount(h2, changesetFilepath, 1)
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 2
        ranChangeSets.get(0).getLastCheckSum().getVersion() == 8
        ranChangeSets.get(1).getLastCheckSum().getVersion() == 9

        cleanup:
        CommandUtil.runDropAll(h2)
    }


    def "update-testing-rollback"() {
        def changesetFilepath = "changelogs/common/checksum-changelog.xml"
        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(h2.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)
        h2.executeSql("CREATE TABLE person(id int)")

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateTestingRollbackCommand = new CommandScope(UpdateTestingRollbackCommandStep.COMMAND_NAME)
        updateTestingRollbackCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        updateTestingRollbackCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        updateTestingRollbackCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
        updateTestingRollbackCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateTestingRollbackCommand.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 2
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 9 })

        cleanup:
        CommandUtil.runDropAll(h2)
    }


    def "update-sql calculates checksum but does not update DBCL"() {
        def changesetFilepath = "changelogs/common/checksum-changelog.xml"
        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(h2.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
        updateCommandScope.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def "update-count-sql calculates checksum but does not update DBCL"() {
        def changesetFilepath = "changelogs/common/checksum-changelog.xml"
        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(h2.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateCountSqlCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
        updateCommandScope.addArgumentValue(UpdateCountSqlCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.addArgumentValue(UpdateCountSqlCommandStep.COUNT_ARG, 1)
        updateCommandScope.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def "update-to-tag-sql calculates checksum but does not update DBCL"() {
        def changesetFilepath = "changelogs/common/checksum-changelog-tag.xml"
        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(h2.getDatabaseFromFactory())
        changeLogService.init()
        insertDbclRecord(changesetFilepath)

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateToTagSqlCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
        updateCommandScope.addArgumentValue(UpdateToTagSqlCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.addArgumentValue(UpdateToTagSqlCommandStep.TAG_ARG, "version_1.3")
        updateCommandScope.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 1
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    @Unroll
    def "DBMS filtered changes should be used to calculate v8 checksum but not v9" () {
        def changesetFilepath = "changelogs/h2/checksum/dbms-filter-changelog.xml"
        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(h2.getDatabaseFromFactory())
        changeLogService.init()
        h2.execute(new RawParameterizedSqlStatement("""
INSERT INTO DATABASECHANGELOG
(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE, CONTEXTS, LABELS, DEPLOYMENT_ID)
VALUES('1', 'fl', '$changesetFilepath', '2023-09-29 14:33:39.108', 1, 'EXECUTED', '8:0a36c7b201a287dd3348e8dd19e44be7', 'sql', 'example comment', NULL, 'DEV', 'example-context1', 'example-label', '5561619071');
"""))
        h2.execute(new RawParameterizedSqlStatement("""
INSERT INTO DATABASECHANGELOG
(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE, CONTEXTS, LABELS, DEPLOYMENT_ID)
VALUES('2', 'fl', '$changesetFilepath', '2023-09-29 14:33:39.112', 2, 'EXECUTED', '8:a6a54dbc65048ebf1388da78c31ef1a9', 'sqlFile; sqlFile', '', NULL, 'DEV', 'example-context1', 'example-label', '5561619071');
"""))

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 2
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        CommandScope updateCommandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
        updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
        updateCommandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
        updateCommandScope.execute()
        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 3
        ranChangeSets.get(0).getLastCheckSum().toString() == "9:d41d8cd98f00b204e9800998ecf8427e"
        ranChangeSets.get(1).getLastCheckSum().toString() == "9:62336a615e62e89c9d86128d1ca60ecf"
        ranChangeSets.get(2).getLastCheckSum().toString() == "9:3b78b9910981c62ca27148640fad7bc2"

        cleanup:
        CommandUtil.runDropAll(h2)

    }


    @Unroll
    def "Calculate change using v8 forced calculator" () {
        def changesetFilepath = "changelogs/h2/checksum/dbms-filter-changelog.xml"
        final ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(h2.getDatabaseFromFactory())
        changeLogService.init()
        h2.execute(new RawParameterizedSqlStatement("""
INSERT INTO DATABASECHANGELOG
(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE, CONTEXTS, LABELS, DEPLOYMENT_ID)
VALUES('1', 'fl', '$changesetFilepath', '2023-09-29 14:33:39.108', 1, 'EXECUTED', '8:0a36c7b201a287dd3348e8dd19e44be7', 'sql', 'example comment', NULL, 'DEV', 'example-context1', 'example-label', '5561619071');
"""))
        h2.execute(new RawParameterizedSqlStatement("""
INSERT INTO DATABASECHANGELOG
(ID, AUTHOR, FILENAME, DATEEXECUTED, ORDEREXECUTED, EXECTYPE, MD5SUM, DESCRIPTION, COMMENTS, TAG, LIQUIBASE, CONTEXTS, LABELS, DEPLOYMENT_ID)
VALUES('2', 'fl', '$changesetFilepath', '2023-09-29 14:33:39.112', 2, 'EXECUTED', '8:a6a54dbc65048ebf1388da78c31ef1a9', 'sqlFile; sqlFile', '', NULL, 'DEV', 'example-context1', 'example-label', '5561619071');
"""))

        when:
        def ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 2
        ranChangeSets.forEach({ rcs -> assert rcs.getLastCheckSum().getVersion() == 8 })

        when:
        def scopeSettings = [
                (Scope.Attr.latestChecksumVersion.name()): ChecksumVersion.V8
        ]
        Scope.child(scopeSettings, {
            CommandScope updateCommandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
            updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, h2.getUsername())
            updateCommandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, h2.getPassword())
            updateCommandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changesetFilepath)
            updateCommandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)

        ranChangeSets = getRanChangesets(changeLogService)

        then:
        ranChangeSets.size() == 3
        ranChangeSets.get(0).getLastCheckSum().toString() == "8:0a36c7b201a287dd3348e8dd19e44be7"
        ranChangeSets.get(1).getLastCheckSum().toString() == "8:a6a54dbc65048ebf1388da78c31ef1a9"
        ranChangeSets.get(2).getLastCheckSum().toString() == "8:551a6b5455d661ce7101a063b818ca3e"

        cleanup:
        CommandUtil.runDropAll(h2)

    }

    @Unroll
    def "manually generate v7 checksum" () {

        given:
        def database = h2.getDatabaseFromFactory()
        ChangeSet changeSet = new ChangeSet("1", "mock-author", false, false, "com/example/root.xml",
                        null, null, null, null, false, null, null)

        CreateTableChange exampleChange = new CreateTableChange()
        exampleChange.setTableName("first")
        ColumnConfig config = (ColumnConfig) ColumnConfig.fromName("first")
        config.setType("VARCHAR (255)")
        exampleChange.getColumns().add(config)
        changeSet.addChange(exampleChange)

        when:
        ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database)
        def scopeSettings = [
                (Scope.Attr.latestChecksumVersion.name()): ChecksumVersion.V7
        ]
        Scope.child(scopeSettings, {
            changeLogService.init()
            changeSet.execute(null, null, database)
            database.execute([new MarkChangeSetRanStatement(changeSet, ChangeSet.ExecType.EXECUTED)] as SqlStatement[], null)
            database.commit()
        } as Scope.ScopedRunnerWithReturn<Void>)

        then:
        def ranChangeSets = changeLogService.getRanChangeSets()
        ranChangeSets.size() == 1
        ranChangeSets.get(0).getLastCheckSum().toString() == "7:72c7eea8dda3c3582e3cfb39eec12033"

        cleanup:
        CommandUtil.runDropAll(h2)
    }
}
