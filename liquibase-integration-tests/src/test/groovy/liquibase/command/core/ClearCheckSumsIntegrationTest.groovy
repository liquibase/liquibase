package liquibase.command.core

import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

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
}
