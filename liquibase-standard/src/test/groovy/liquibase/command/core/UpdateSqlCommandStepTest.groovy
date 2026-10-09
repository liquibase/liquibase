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
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.executor.ExecutorService
import liquibase.executor.LoggingExecutor
import liquibase.lockservice.LockService
import liquibase.lockservice.LockServiceFactory
import spock.lang.Specification

import java.sql.DriverManager

class UpdateSqlCommandStepTest extends Specification {

    private java.sql.Connection jdbcConnection
    private Database database

    def cleanup() {
        LockServiceFactory.reset()
        if (database != null) {
            Scope.getCurrentScope().getSingleton(ExecutorService.class).clearExecutor("logging", database)
        }
        jdbcConnection?.close()
    }

    /**
     * Bypasses the real FastCheckService/DB round-trip, standing in for the case where the fast-check finds pending changes.
     * Records completion so the test can assert the lock is acquired only after the fast-check runs, not before it.
     */
    private static class FastCheckPendingUpdateSqlCommandStep extends UpdateSqlCommandStep {
        boolean fastCheckCompleted = false

        @Override
        boolean isUpToDate(CommandScope commandScope, Database database, DatabaseChangeLog databaseChangeLog,
                            Contexts contexts, LabelExpression labelExpression, OutputStream outputStream) {
            fastCheckCompleted = true
            return false
        }
    }

    def "run() acquires the changelog lock itself exactly once when the fast-check finds pending changes (#6102)"() {
        given: "updateSql now owns lock acquisition instead of relying on a pipeline-managed LockServiceCommandStep"
        def step = new FastCheckPendingUpdateSqlCommandStep()

        // A real (fresh, empty) H2 database: run() queries the changelog history/snapshot tables as
        // part of building the status summary, which a plain mock Database can't satisfy.
        jdbcConnection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID(), "sa", "")
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(jdbcConnection))

        def mockLockService = Mock(LockService) {
            hasChangeLogLock() >> true
        }
        def mockFactory = Mock(LockServiceFactory) {
            getLockService(database) >> mockLockService
        }
        LockServiceFactory.setInstance(mockFactory)

        def commandScope = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME)
                .addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, "changelog.xml")
                .provideDependency(Database.class, database)
                .provideDependency(DatabaseChangeLog.class, new DatabaseChangeLog("changelog.xml"))
                .provideDependency(ChangeExecListener.class, new DefaultChangeExecListener())
                .provideDependency(ChangeLogParameters.class, new ChangeLogParameters())
        def resultsBuilder = new CommandResultsBuilder(commandScope, new ByteArrayOutputStream())

        // In the real pipeline, AbstractOutputWriterCommandStep sets this up before UpdateSqlCommandStep runs.
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("logging", database,
                new LoggingExecutor(null, new OutputStreamWriter(resultsBuilder.getOutputStream()), database))

        when: "run() proceeds past the fast-check since there are pending changes"
        step.run(resultsBuilder)

        then: "the lock is acquired only after the fast-check completes, not by a pipeline-managed LockServiceCommandStep beforehand"
        1 * mockLockService.waitForLock() >> { assert step.fastCheckCompleted }

        and: "the lock this call acquired is also released before run() returns"
        1 * mockLockService.releaseLock()
    }
}
