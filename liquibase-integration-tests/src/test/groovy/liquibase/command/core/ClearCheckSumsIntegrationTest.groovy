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
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class ClearCheckSumsIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = (DatabaseTestSystem) Scope.currentScope.getSingleton(TestSystemFactory.class).getTestSystem("h2")

    def setupSpec(){
        def updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME)
        updateCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, h2.getDatabaseFromFactory())
        updateCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "liquibase/update-tests.yml")
        updateCommand.execute()
    }

    def "validate checksums are cleared"() {
        when:
        def h2Database = h2.getDatabaseFromFactory()
        def commandResults = new CommandScope("clearChecksums")
                .addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, h2.getConnectionUrl())
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, h2Database)
                .execute()

        ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(h2Database);

        then:
        changeLogService.getRanChangeSets().stream().allMatch({ ranChangeSet -> ranChangeSet.getLastCheckSum() == null })

        cleanup:
        CommandUtil.runDropAll(h2)
    }
}
