package liquibase.command.core.helpers

import liquibase.command.CommandResultsBuilder
import liquibase.command.CommandScope
import liquibase.database.Database
import liquibase.lockservice.LockService
import liquibase.lockservice.LockServiceFactory
import spock.lang.Specification

import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class LockServiceCommandStepTest extends Specification {

    LockServiceFactory mockLockServiceFactory
    LockService mockLockService
    Database mockDatabase

    def setup() {
        mockLockServiceFactory = Mock(LockServiceFactory)
        mockLockService = Mock(LockService)
        mockDatabase = Mock(Database)
        LockServiceFactory.setInstance(mockLockServiceFactory)
        mockLockServiceFactory.getLockService(_) >> mockLockService
    }

    def cleanup() {
        LockServiceFactory.reset()
    }

    def "cleanUp() releases the lock exactly once when run() was called on the same thread"() {
        given:
        def step = new LockServiceCommandStep()
        def commandScope = Mock(CommandScope)
        commandScope.getDependency(Database.class) >> mockDatabase
        def resultsBuilder = Mock(CommandResultsBuilder)
        resultsBuilder.getCommandScope() >> commandScope

        when:
        step.run(resultsBuilder)
        step.cleanUp(resultsBuilder)

        then:
        1 * mockLockService.waitForLock()
        1 * mockLockService.releaseLock()
    }

    def "cleanUp() does not call releaseLock() when run() was never called"() {
        given:
        def step = new LockServiceCommandStep()
        def commandScope = Mock(CommandScope)
        commandScope.getDependency(Database.class) >> mockDatabase
        def resultsBuilder = Mock(CommandResultsBuilder)
        resultsBuilder.getCommandScope() >> commandScope

        when:
        step.cleanUp(resultsBuilder)

        then:
        0 * mockLockService.releaseLock()
    }

    def "each thread's cleanUp() releases only that thread's lock state, not other threads'"() {
        given: "a shared LockServiceCommandStep instance (simulating the static COMMAND_DEFINITIONS in CommandFactory)"
        def step = new LockServiceCommandStep()
        def threadCount = 4
        def releaseCount = new AtomicInteger(0)

        mockLockService.releaseLock() >> { releaseCount.incrementAndGet() }

        def allThreadsAcquiredLockBarrier = new CyclicBarrier(threadCount) // all threads complete run() before any calls cleanUp()
        def startCleanupTogetherBarrier = new CyclicBarrier(threadCount)  // all threads start cleanUp() together
        def executor = Executors.newFixedThreadPool(threadCount)

        when: "N threads each call run() and then cleanUp() on the shared step instance"
        def futures = (1..threadCount).collect {
            executor.submit {
                def commandScope = Mock(CommandScope)
                commandScope.getDependency(Database.class) >> mockDatabase
                def resultsBuilder = Mock(CommandResultsBuilder)
                resultsBuilder.getCommandScope() >> commandScope

                step.run(resultsBuilder)
                allThreadsAcquiredLockBarrier.await(10, TimeUnit.SECONDS) // all threads have acquired lock
                startCleanupTogetherBarrier.await(10, TimeUnit.SECONDS)   // all threads start cleanup together
                step.cleanUp(resultsBuilder)
            }
        }
        futures.each { it.get(30, TimeUnit.SECONDS) }

        then: "every thread independently released its own lock (N total releases)"
        releaseCount.get() == threadCount

        cleanup:
        executor.shutdownNow()
    }

    def "cleanUp() in one thread does not prevent another thread from releasing its lock"() {
        given: "a shared step and two threads"
        def step = new LockServiceCommandStep()
        def releaseCount = new AtomicInteger(0)

        mockLockService.releaseLock() >> { releaseCount.incrementAndGet() }

        def thread1DoneBarrier = new CyclicBarrier(2)  // thread1 has called cleanUp()
        def executor = Executors.newFixedThreadPool(2)

        when: "thread 1 calls run() and cleanUp(), then thread 2 calls run() and cleanUp()"
        def commandScope1 = Mock(CommandScope)
        commandScope1.getDependency(Database.class) >> mockDatabase
        def resultsBuilder1 = Mock(CommandResultsBuilder)
        resultsBuilder1.getCommandScope() >> commandScope1

        def commandScope2 = Mock(CommandScope)
        commandScope2.getDependency(Database.class) >> mockDatabase
        def resultsBuilder2 = Mock(CommandResultsBuilder)
        resultsBuilder2.getCommandScope() >> commandScope2

        def futures = [
            executor.submit {
                step.run(resultsBuilder1)
                step.cleanUp(resultsBuilder1)
                thread1DoneBarrier.await(10, TimeUnit.SECONDS)
            },
            executor.submit {
                step.run(resultsBuilder2)
                thread1DoneBarrier.await(10, TimeUnit.SECONDS)  // wait for thread 1 to finish cleanup
                step.cleanUp(resultsBuilder2)
            }
        ]
        futures.each { it.get(30, TimeUnit.SECONDS) }

        then: "both threads released their locks regardless of cleanup ordering"
        releaseCount.get() == 2

        cleanup:
        executor.shutdownNow()
    }
}
