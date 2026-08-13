package liquibase.command.core

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Scope
import liquibase.UpdateSummaryEnum
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.visitor.ChangeExecListener
import liquibase.changelog.visitor.DefaultChangeExecListener
import liquibase.command.CommandResultsBuilder
import liquibase.command.CommandScope
import liquibase.database.Database
import liquibase.database.DatabaseConnection
import liquibase.executor.ExecutorService
import liquibase.executor.LoggingExecutor
import liquibase.lockservice.LockService
import liquibase.lockservice.LockServiceFactory
import spock.lang.Specification

class UpdateSqlCommandStepTest extends Specification {

    def cleanup() {
        LockServiceFactory.reset()
    }

    /**
     * Bypasses the real FastCheckService/DB round-trip, standing in for the case where the fast-check finds nothing pending.
     */
    private static class FastCheckUpToDateUpdateSqlCommandStep extends UpdateSqlCommandStep {
        @Override
        boolean isUpToDate(CommandScope commandScope, Database database, DatabaseChangeLog databaseChangeLog,
                            Contexts contexts, LabelExpression labelExpression, OutputStream outputStream) {
            return true
        }
    }

    def "run() must not touch the changelog lock when the fast-check finds nothing pending (#6102)"() {
        given: "updateSql, like update, now acquires the lock lazily instead of via the pipeline"
        def step = new FastCheckUpToDateUpdateSqlCommandStep()
        def mockLockService = Mock(LockService)
        def mockDatabase = Mock(Database) {
            getConnection() >> Mock(DatabaseConnection) {
                getVisibleUrl() >> "jdbc:test"
                getURL() >> "jdbc:test"
                getConnectionUserName() >> "test"
            }
            getLineComment() >> "--"
        }
        def mockFactory = Mock(LockServiceFactory) {
            getLockService(mockDatabase) >> mockLockService
        }
        LockServiceFactory.setInstance(mockFactory)

        def commandScope = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME)
                .addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "changelog.xml")
                .provideDependency(Database.class, mockDatabase)
                .provideDependency(DatabaseChangeLog.class, new DatabaseChangeLog("changelog.xml"))
                .provideDependency(ChangeExecListener.class, new DefaultChangeExecListener())
                .provideDependency(ChangeLogParameters.class, new ChangeLogParameters())
        def resultsBuilder = new CommandResultsBuilder(commandScope, new ByteArrayOutputStream())

        // In the real pipeline, AbstractOutputWriterCommandStep sets this up before UpdateSqlCommandStep runs.
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("logging", mockDatabase,
                new LoggingExecutor(null, new OutputStreamWriter(resultsBuilder.getOutputStream()), mockDatabase))

        when: "run() takes the fast-check early-return path"
        step.run(resultsBuilder)

        then: "in SQL-output mode waitForLock() is what writes the lock statements, so 0 calls means none were written"
        0 * mockLockService.waitForLock()
        0 * mockLockService.releaseLock()
    }
}
