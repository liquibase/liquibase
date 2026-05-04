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

    def cleanup() {
        LockServiceFactory.reset()
    }

    def "isDBLocked ThreadLocal ensures each thread tracks its own lock state"() {
        given: "a shared LockServiceCommandStep instance (simulating static COMMAND_DEFINITIONS)"
        def step = new LockServiceCommandStep()
        def threadCount = 4
        def barrier = new CyclicBarrier(threadCount)
        def executor = Executors.newFixedThreadPool(threadCount)
        def cleanUpSawLocked = new AtomicInteger(0)

        when: "multiple threads run and cleanUp concurrently"
        def futures = (1..threadCount).collect {
            executor.submit {
                def field = LockServiceCommandStep.getDeclaredField("isDBLocked")
                field.setAccessible(true)
                def threadLocal = (ThreadLocal<Boolean>) field.get(step)

                threadLocal.set(true)

                // Synchronize: all threads have "locked" before any starts cleanup
                barrier.await(10, TimeUnit.SECONDS)

                if (threadLocal.get()) {
                    cleanUpSawLocked.incrementAndGet()
                }

                threadLocal.remove()
            }
        }
        futures.each { it.get(30, TimeUnit.SECONDS) }

        then: "every thread should have seen its own locked state as true"
        cleanUpSawLocked.get() == threadCount

        cleanup:
        executor.shutdownNow()
    }

    def "isDBLocked is false by default for new threads"() {
        given:
        def step = new LockServiceCommandStep()
        def field = LockServiceCommandStep.getDeclaredField("isDBLocked")
        field.setAccessible(true)
        def threadLocal = (ThreadLocal<Boolean>) field.get(step)

        expect: "default value is false, not null"
        threadLocal.get() == Boolean.FALSE
    }

    def "isDBLocked removal in one thread does not affect another"() {
        given:
        def step = new LockServiceCommandStep()
        def field = LockServiceCommandStep.getDeclaredField("isDBLocked")
        field.setAccessible(true)
        def threadLocal = (ThreadLocal<Boolean>) field.get(step)
        def executor = Executors.newFixedThreadPool(2)
        def barrier1 = new CyclicBarrier(2)
        def barrier2 = new CyclicBarrier(2)

        when:
        def thread1Value = null
        def futures = [
            executor.submit {
                threadLocal.set(true)
                barrier1.await(10, TimeUnit.SECONDS)
                barrier2.await(10, TimeUnit.SECONDS)
                thread1Value = threadLocal.get()
            },
            executor.submit {
                threadLocal.set(true)
                barrier1.await(10, TimeUnit.SECONDS)
                threadLocal.remove()
                barrier2.await(10, TimeUnit.SECONDS)
            }
        ]
        futures.each { it.get(30, TimeUnit.SECONDS) }

        then: "thread 1's value is unaffected by thread 2's removal"
        thread1Value == true

        cleanup:
        executor.shutdownNow()
    }

    def "concurrent run and cleanUp through public API releases only the calling thread's lock"() {
        given: "a mock LockServiceFactory that tracks per-thread lock/release calls"
        def step = new LockServiceCommandStep()
        def threadCount = 4
        def barrier = new CyclicBarrier(threadCount)
        def executor = Executors.newFixedThreadPool(threadCount)
        def releasedCount = new AtomicInteger(0)

        and: "mock lock services — one per thread to verify independent release"
        def mockLockServices = (1..threadCount).collect { Mock(LockService) }
        def mockDatabases = (1..threadCount).collect { Mock(Database) }
        def lockServiceIndex = new AtomicInteger(0)

        def mockFactory = Mock(LockServiceFactory) {
            getLockService(_) >> { Database db ->
                def idx = mockDatabases.indexOf(db)
                return mockLockServices[idx]
            }
        }
        LockServiceFactory.setInstance(mockFactory)

        when: "multiple threads call run() then cleanUp() concurrently"
        def futures = (0..<threadCount).collect { idx ->
            executor.submit {
                def command = new CommandScope(LockServiceCommandStep.COMMAND_NAME)
                        .provideDependency(Database.class, mockDatabases[idx])
                def resultsBuilder = new CommandResultsBuilder(command, new ByteArrayOutputStream())

                // run() acquires the lock and sets isDBLocked for this thread
                step.run(resultsBuilder)

                // sync: all threads have acquired locks before any cleans up
                barrier.await(10, TimeUnit.SECONDS)

                // cleanUp() should release only this thread's lock
                step.cleanUp(resultsBuilder)
                releasedCount.incrementAndGet()
            }
        }
        futures.each { it.get(30, TimeUnit.SECONDS) }

        then: "each thread's lock service had waitForLock and releaseLock called exactly once"
        1 * mockLockServices[0].waitForLock()
        1 * mockLockServices[0].releaseLock()
        1 * mockLockServices[1].waitForLock()
        1 * mockLockServices[1].releaseLock()
        1 * mockLockServices[2].waitForLock()
        1 * mockLockServices[2].releaseLock()
        1 * mockLockServices[3].waitForLock()
        1 * mockLockServices[3].releaseLock()

        and: "all threads completed cleanup"
        releasedCount.get() == threadCount

        cleanup:
        executor.shutdownNow()
    }

    def "cleanUp after successful run releases the lock"() {
        given:
        def step = new LockServiceCommandStep()
        def mockLockService = Mock(LockService)
        def mockDatabase = Mock(Database)
        def mockFactory = Mock(LockServiceFactory) {
            getLockService(mockDatabase) >> mockLockService
        }
        LockServiceFactory.setInstance(mockFactory)

        def command = new CommandScope(LockServiceCommandStep.COMMAND_NAME)
                .provideDependency(Database.class, mockDatabase)
        def resultsBuilder = new CommandResultsBuilder(command, new ByteArrayOutputStream())

        when:
        step.run(resultsBuilder)
        step.cleanUp(resultsBuilder)

        then:
        1 * mockLockService.waitForLock()
        1 * mockLockService.releaseLock()
    }

    def "cleanUp without prior run does not attempt lock release"() {
        given:
        def step = new LockServiceCommandStep()
        def mockLockService = Mock(LockService)
        def mockDatabase = Mock(Database)
        def mockFactory = Mock(LockServiceFactory) {
            getLockService(mockDatabase) >> mockLockService
        }
        LockServiceFactory.setInstance(mockFactory)

        def command = new CommandScope(LockServiceCommandStep.COMMAND_NAME)
                .provideDependency(Database.class, mockDatabase)
        def resultsBuilder = new CommandResultsBuilder(command, new ByteArrayOutputStream())

        when:
        step.cleanUp(resultsBuilder)

        then:
        0 * mockLockService.releaseLock()
    }
}
