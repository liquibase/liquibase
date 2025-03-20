package liquibase.command.core

import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.parser.core.ParsedNode
import liquibase.resource.SearchPathResourceAccessor
import liquibase.util.StringUtil
import spock.lang.Shared
import spock.lang.Specification

import java.sql.ResultSet

@LiquibaseIntegrationTest
class ClearCheckSumsIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = (DatabaseTestSystem) Scope.currentScope.getSingleton(TestSystemFactory.class).getTestSystem("h2")

    def "clearing checksums in an empty database does not throw an exception"() {
        given:
        CommandUtil.runDropAll(h2)

        when:
        def h2Database = h2.getDatabaseFromFactory()
        def commandResults = new CommandScope("clearChecksums")
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, h2Database)
                .execute()

        then:
        commandResults.results.size() == 0

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def "validate checksums are cleared"() {
        given:
        def updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, h2.getDatabaseFromFactory())
        updateCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "liquibase/update-tests.yml")
        updateCommand.execute()

        when:
        def h2Database = h2.getDatabaseFromFactory()
        def commandResults = new CommandScope("clearChecksums")
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, h2.getConnectionUrl())
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, h2Database)
                .execute()

        ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(h2Database);

        then:
        changeLogService.getRanChangeSets().stream().allMatch({ ranChangeSet -> ranChangeSet.getLastCheckSum() == null })

        cleanup:
        CommandUtil.runDropAll(h2)
    }

    def "validate generated CheckSum doesn't change after perform a clearCheckSum command and then a new update command"() {
        given:
        def changeLogFile = "changelogs/h2/complete/sqlchange.with.dbms.changelog.xml"
        def id = "clearCheckSumTest"
        def author = "mallod"
        def filePath = "changelogs/h2/complete/sqlchange.with.dbms.changelog.xml"
        CommandUtil.runUpdate(h2, changeLogFile, null, null, null)

        def originalCheckSum = queryGeneratedCheckSum(id, author, filePath)

        when:
        CommandUtil.runClearCheckSum(h2)

        then:
        queryGeneratedCheckSum(id, author, filePath) == null

        when:
        CommandUtil.runUpdate(h2, changeLogFile, null, null, null)

        then:
        noExceptionThrown()
        def newCheckSum = queryGeneratedCheckSum(id, author, filePath)
        originalCheckSum == newCheckSum
    }

    private String queryGeneratedCheckSum(String id, String author, String filePath) {
        ResultSet resultSet = h2.getConnection().createStatement().executeQuery(String.format("select md5sum from databasechangelog WHERE id='%s' AND author='%s' AND filename='%s'", id, author, filePath))
        def originalCheckSum = ""
        if (resultSet.next()) {
            originalCheckSum = resultSet.getString("md5sum")
        }

        return originalCheckSum
    }
}
