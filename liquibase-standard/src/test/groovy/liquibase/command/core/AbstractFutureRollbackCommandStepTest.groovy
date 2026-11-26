package liquibase.command.core

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Scope
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.visitor.DefaultChangeExecListener
import liquibase.database.Database
import liquibase.exception.LockException
import liquibase.executor.Executor
import liquibase.executor.ExecutorService
import liquibase.lockservice.LockService
import liquibase.lockservice.LockServiceFactory
import spock.lang.Specification

class AbstractFutureRollbackCommandStepTest extends Specification {

    def executorService = Scope.getCurrentScope().getSingleton(ExecutorService.class)

    def cleanup() {
        LockServiceFactory.reset()
        executorService.reset()
    }

    /** Minimal concrete subclass, standing in for FutureRollbackSqlCommandStep and friends. */
    private static class TestFutureRollbackCommandStep extends AbstractFutureRollbackCommandStep {
        @Override
        String[][] defineCommandNames() { return [["futureRollbackSqlTest"]] as String[][] }
    }

    def "futureRollbackSQL propagates a releaseLock failure instead of reporting success (#8008)"() {
        given: "a lock service whose releaseLock() fails, as a lost session lock would"
        def step = new TestFutureRollbackCommandStep()
        def mockLockService = Mock(LockService) {
            releaseLock() >> { throw new LockException("simulated release failure") }
        }
        def mockDatabase = Mock(Database) {
            getRanChangeSetList() >> []
        }
        def mockFactory = Mock(LockServiceFactory) {
            getLockService(mockDatabase) >> mockLockService
        }
        LockServiceFactory.setInstance(mockFactory)
        executorService.setExecutor("jdbc", mockDatabase, Mock(Executor))
        executorService.setExecutor("logging", mockDatabase, Mock(Executor))

        when: "the future-rollback SQL has been generated and the lock release fails in the finally block"
        step.futureRollbackSQL(null, null, new Contexts(), new LabelExpression(), new StringWriter(),
                mockDatabase, new DatabaseChangeLog("changelog.xml"), new DefaultChangeExecListener())

        then: "the LockException propagates so the command fails, instead of exiting 0 with only a SEVERE log line"
        thrown(LockException)
    }
}
