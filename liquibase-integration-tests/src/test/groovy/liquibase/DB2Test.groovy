package liquibase


import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.UpdateCountCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@LiquibaseIntegrationTest
class DB2Test extends Specification {

    @Shared
    private DatabaseTestSystem db2 = Scope.getCurrentScope().getSingleton(TestSystemFactory).getTestSystem("db2") as DatabaseTestSystem

    /**
     * Performance timeout threshold in seconds.
     * Issue #6660 reported precondition checks taking "many minutes" with ~30 views and numerous indexes.
     * Our test uses a simple changelog (1 table, 1 view, 1 index) on an empty database.
     * With the optimization using hasIgnoreNested(), execution should complete in seconds.
     * We set a 60-second timeout to account for:
     * - CI environment variability and system load
     * - Remote DB2 instance network latency
     * - Database container startup overhead
     * This is generous enough for reliable CI execution while still catching major regressions.
     * Even if the optimization broke, this simple test wouldn't take "many minutes", but any
     * execution exceeding 60 seconds would indicate a performance problem.
     */
    private static final int PERFORMANCE_TIMEOUT_SECONDS = 60

    def "verify tableExists, viewExists, and indexExists preconditions work efficiently on DB2"() {
        given:
        def changeLogFile = "changelogs/db2/preconditions-test.xml"
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()): new SearchPathResourceAccessor(".,target/test-classes")
        ]

        when:
        def startTime = System.currentTimeMillis()
        Scope.child(scopeSettings, {
            CommandScope commandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db2.getConnectionUrl())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, db2.getUsername())
            commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, db2.getPassword())
            commandScope.addArgumentValue(UpdateCountCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
            commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)
        def endTime = System.currentTimeMillis()
        def executionTimeSeconds = (endTime - startTime) / 1000.0

        then:
        noExceptionThrown()

        and: "precondition checks complete within performance threshold"
        executionTimeSeconds < PERFORMANCE_TIMEOUT_SECONDS

        and: "log execution time for monitoring"
        println "DB2 precondition test completed in ${executionTimeSeconds} seconds (threshold: ${PERFORMANCE_TIMEOUT_SECONDS}s)"
    }
}
