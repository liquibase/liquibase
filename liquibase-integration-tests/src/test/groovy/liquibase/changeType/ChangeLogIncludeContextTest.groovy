package liquibase.changeType

import liquibase.Scope
import liquibase.UpdateSummaryEnum
import liquibase.UpdateSummaryOutputEnum
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.core.helpers.ShowSummaryArgument
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.logging.core.BufferedLogService
import spock.lang.Shared
import spock.lang.Specification

import java.util.logging.Level

@LiquibaseIntegrationTest
class ChangeLogIncludeContextTest extends Specification {

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
                commandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "changelogs/changeType/includeContext/changelog.xml")
                commandScope.addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, "secondary")
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, UpdateSummaryEnum.VERBOSE)
                commandScope.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, UpdateSummaryOutputEnum.LOG)
                commandScope.execute()
            }
        })

        when:
        def logContent = logService.getLogAsString(Level.INFO)

        then:
        logContent.contains("ChangeSet changelogs/changeType/includeContext/changelog-secondary.xml::initial_db_setup_secondary::dev ran successfully")
        logContent.contains("'changelogs/changeType/includeContext/changelog-primary.xml::initial_db_setup_primary::dev' : Context does not match 'secondary'")

        when:
        def resultSet = db.getConnection().createStatement().executeQuery("select countries from country_table where id = 1")
        resultSet.next()

        then:
        resultSet.getString("countries").equals("SG,MY")
    }

}
