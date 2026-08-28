package liquibase.command.core

import liquibase.Scope
import liquibase.UpdateSummaryEnum
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.StandardChangeLogHistoryService
import liquibase.changelog.visitor.ChangeExecListener
import liquibase.changelog.visitor.DefaultChangeExecListener
import liquibase.command.CommandResultsBuilder
import liquibase.command.CommandScope
import liquibase.database.Database
import liquibase.database.DatabaseConnection
import liquibase.database.core.PostgresDatabase
import liquibase.executor.Executor
import liquibase.executor.ExecutorService
import liquibase.executor.jvm.JdbcExampleExecutor
import liquibase.lockservice.LockService
import liquibase.lockservice.LockServiceFactory
import spock.lang.Specification

class AbstractUpdateCommandStepTest extends Specification {

    def executorService = Scope.getCurrentScope().getSingleton(ExecutorService.class)
    def changeLogHistoryServiceFactory = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class)

    def setup() {
        executorService.reset()
        changeLogHistoryServiceFactory.resetAll()
    }

    def cleanup() {
        LockServiceFactory.reset()
        executorService.reset()
        changeLogHistoryServiceFactory.resetAll()
    }

    /**
     * Minimal concrete AbstractUpdateCommandStep, standing in for commands like UpdateCountCommandStep /
     * UpdateSqlCommandStep / UpdateToTagCommandStep, which never call setDBLock(false) and therefore rely
     * entirely on the CommandFramework pipeline's LockServiceCommandStep to manage the changelog lock.
     */
    private static class FastCheckUpToDateCommandStep extends AbstractUpdateCommandStep {
        @Override
        String getChangelogFileArg(CommandScope commandScope) { return "changelog.xml" }

        @Override
        String getContextsArg(CommandScope commandScope) { return null }

        @Override
        String getLabelFilterArg(CommandScope commandScope) { return null }

        @Override
        String[] getCommandName() { return ["fastCheckUpToDate"] as String[] }

        @Override
        UpdateSummaryEnum getShowSummary(CommandScope commandScope) { return UpdateSummaryEnum.OFF }

        @Override
        String[][] defineCommandNames() { return [["fastCheckUpToDate"]] as String[][] }

        @Override
        boolean isUpToDate(CommandScope commandScope, Database database, DatabaseChangeLog databaseChangeLog,
                            liquibase.Contexts contexts, liquibase.LabelExpression labelExpression, OutputStream outputStream) {
            // Simulate the exact scenario from #5438: fast-check finds nothing pending, so run()
            // returns before ever acquiring the changelog lock itself.
            return true
        }
    }

    private static CommandScope buildCommandScope(Database database) {
        // Reuse a real, registered command name purely so CommandScope's constructor can resolve a
        // CommandDefinition; run() is invoked directly below (not via CommandScope#execute()), so the
        // pipeline behind this name is irrelevant to what's under test.
        def commandScope = new CommandScope(UpdateCountCommandStep.COMMAND_NAME)
        commandScope.provideDependency(Database.class, database)
        commandScope.provideDependency(DatabaseChangeLog.class, new DatabaseChangeLog("changelog.xml"))
        commandScope.provideDependency(ChangeExecListener.class, new DefaultChangeExecListener())
        commandScope.provideDependency(ChangeLogParameters.class, new ChangeLogParameters())
        return commandScope
    }

    def "run() must not release the changelog lock when the fast-check finds nothing pending (#5438)"() {
        given: "a step that never acquires the lock itself, mirroring updateCount/updateSql/updateToTag"
        def step = new FastCheckUpToDateCommandStep()
        def mockLockService = Mock(LockService)
        def mockDatabase = Mock(Database) {
            getConnection() >> Mock(DatabaseConnection) {
                getVisibleUrl() >> "jdbc:test"
                getURL() >> "jdbc:test"
            }
        }
        def mockFactory = Mock(LockServiceFactory) {
            getLockService(mockDatabase) >> mockLockService
        }
        LockServiceFactory.setInstance(mockFactory)

        def commandScope = buildCommandScope(mockDatabase)
        def resultsBuilder = new CommandResultsBuilder(commandScope, new ByteArrayOutputStream())

        when: "run() takes the fast-check early-return path"
        step.run(resultsBuilder)

        then: "the lock, which was never acquired by this method, must not be released by it"
        0 * mockLockService.waitForLock()
        0 * mockLockService.releaseLock()
    }

    def "run() releases the lock exactly once when it is genuinely held, even via a pipeline-managed acquire (#5438)"() {
        given: "the lock is already held (as it would be if LockServiceCommandStep acquired it in the pipeline)"
        def step = new FastCheckUpToDateCommandStep()
        def mockLockService = Mock(LockService) { hasChangeLogLock() >> true }
        def mockDatabase = Mock(Database) {
            getConnection() >> Mock(DatabaseConnection) {
                getVisibleUrl() >> "jdbc:test"
                getURL() >> "jdbc:test"
            }
        }
        def mockFactory = Mock(LockServiceFactory) {
            getLockService(mockDatabase) >> mockLockService
        }
        LockServiceFactory.setInstance(mockFactory)

        def commandScope = buildCommandScope(mockDatabase)
        def resultsBuilder = new CommandResultsBuilder(commandScope, new ByteArrayOutputStream())

        when: "run() takes the fast-check early-return path, same as above, but the lock is actually held"
        step.run(resultsBuilder)

        then: "the genuinely-held lock is still released here (not left for LockServiceCommandStep alone), so Sql-output commands still capture the release statement"
        0 * mockLockService.waitForLock()
        1 * mockLockService.releaseLock()
    }

    def "cleanUp only clears the executor override and ChangeLogHistoryService for its own database"() {
        // Cross-module state corruption in parallel (-T) Maven reactor builds, bug 2 of 3: updateSql's
        // cleanUp() used to call the blanket ExecutorService.reset() and
        // ChangeLogHistoryServiceFactory.resetAll() unconditionally on every command completion.
        // Since both factories are single JVM-wide singletons since #7877, one module's cleanUp()
        // wiped out every other concurrently-running module's cached "jdbc"/"logging" Executor
        // overrides (e.g. the SQL-writing LoggingExecutor installed by updateSql) and cached
        // ChangeLogHistoryService too, not just its own. Exercised directly here (rather than via
        // an actual -T Maven build) since the real race was intermittent.
        given: "two independent executions (as concurrently-running Maven modules would each have) with their own executor override and cached history service"
        def databaseA = new PostgresDatabase()
        def databaseB = new PostgresDatabase()
        Executor overrideA = new JdbcExampleExecutor()
        Executor overrideB = new JdbcExampleExecutor()
        executorService.setExecutor("jdbc", databaseA, overrideA)
        executorService.setExecutor("jdbc", databaseB, overrideB)

        def resets = [:].withDefault { 0 }
        def serviceA = new StandardChangeLogHistoryService() {
            @Override void reset() { resets["A"]++; super.reset() }
        }
        serviceA.setDatabase(databaseA)
        def serviceB = new StandardChangeLogHistoryService() {
            @Override void reset() { resets["B"]++; super.reset() }
        }
        serviceB.setDatabase(databaseB)
        changeLogHistoryServiceFactory.registerForDatabase(databaseA, serviceA)
        changeLogHistoryServiceFactory.registerForDatabase(databaseB, serviceB)

        def commandA = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME)
                .provideDependency(Database.class, databaseA)
        def resultsBuilderA = new CommandResultsBuilder(commandA, new ByteArrayOutputStream())

        when: "module A finishes and cleans up while module B is still in flight"
        new UpdateSqlCommandStep().cleanUp(resultsBuilderA)

        then: "module B's executor override and history service are untouched by module A's cleanup"
        executorService.getExecutor("jdbc", databaseB).is(overrideB)
        resets["B"] == 0

        and: "module A's own executor override was dropped and its history service invalidated"
        !executorService.getExecutor("jdbc", databaseA).is(overrideA)
        resets["A"] == 1
    }

    def "cleanUp only drops the LockService for its own database"() {
        // Same JVM-wide-singleton hazard as the test above, for LockServiceFactory: cleanUp() used to
        // call the blanket LockServiceFactory.resetAll(), which nulls the singleton and with it every
        // database's LockService. A concurrently running execution then looked its lock service up
        // again, got a freshly built one whose hasChangeLogLock() is false, skipped its release without
        // logging anything, and left its DATABASECHANGELOGLOCK row set.
        given: "two independent executions, each with an open lock service"
        def databaseA = new PostgresDatabase()
        def databaseB = new PostgresDatabase()
        def lockServiceA = LockServiceFactory.getInstance().getLockService(databaseA)
        def lockServiceB = LockServiceFactory.getInstance().getLockService(databaseB)

        def commandA = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME)
                .provideDependency(Database.class, databaseA)
        def resultsBuilderA = new CommandResultsBuilder(commandA, new ByteArrayOutputStream())

        when: "execution A finishes and cleans up while execution B is still in flight"
        new UpdateSqlCommandStep().cleanUp(resultsBuilderA)

        then: "B keeps the very lock service that knows it holds the changelog lock"
        LockServiceFactory.getInstance().getLockService(databaseB).is(lockServiceB)

        and: "A's own lock service is dropped, its execution being over"
        !LockServiceFactory.getInstance().getLockService(databaseA).is(lockServiceA)
    }
}
