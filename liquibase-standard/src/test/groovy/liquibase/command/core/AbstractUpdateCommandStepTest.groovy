package liquibase.command.core

import liquibase.UpdateSummaryEnum
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.visitor.ChangeExecListener
import liquibase.changelog.visitor.DefaultChangeExecListener
import liquibase.command.CommandResultsBuilder
import liquibase.command.CommandScope
import liquibase.database.Database
import liquibase.database.DatabaseConnection
import liquibase.lockservice.LockService
import liquibase.lockservice.LockServiceFactory
import spock.lang.Specification

class AbstractUpdateCommandStepTest extends Specification {

    def cleanup() {
        LockServiceFactory.reset()
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
}
