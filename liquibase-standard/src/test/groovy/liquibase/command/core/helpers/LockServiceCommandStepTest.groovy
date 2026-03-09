package liquibase.command.core.helpers

import spock.lang.Specification

import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class LockServiceCommandStepTest extends Specification {

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
                // Access the private isDBLocked field to simulate run() setting it to true
                def field = LockServiceCommandStep.getDeclaredField("isDBLocked")
                field.setAccessible(true)
                def threadLocal = (ThreadLocal<Boolean>) field.get(step)

                // Simulate run() acquiring lock
                threadLocal.set(true)

                // Synchronize: all threads have "locked" before any starts cleanup
                barrier.await(10, TimeUnit.SECONDS)

                // Check if this thread still sees its own locked state
                if (threadLocal.get()) {
                    cleanUpSawLocked.incrementAndGet()
                }

                // Simulate one thread's cleanup clearing its value
                threadLocal.remove()
            }
        }
        futures.each { it.get(30, TimeUnit.SECONDS) }
        executor.shutdown()

        then: "every thread should have seen its own locked state as true"
        cleanUpSawLocked.get() == threadCount
    }

    def "isDBLocked is false by default for new threads"() {
        given:
        def step = new LockServiceCommandStep()
        def field = LockServiceCommandStep.getDeclaredField("isDBLocked")
        field.setAccessible(true)
        def threadLocal = (ThreadLocal<Boolean>) field.get(step)

        expect: "default value is false"
        !threadLocal.get()
    }

    def "isDBLocked removal in one thread does not affect another"() {
        given:
        def step = new LockServiceCommandStep()
        def field = LockServiceCommandStep.getDeclaredField("isDBLocked")
        field.setAccessible(true)
        def threadLocal = (ThreadLocal<Boolean>) field.get(step)
        def executor = Executors.newFixedThreadPool(2)
        def barrier1 = new CyclicBarrier(2) // both threads have set their values
        def barrier2 = new CyclicBarrier(2) // thread 2 has removed, thread 1 can now read

        when:
        def thread1Value = null
        def futures = [
            executor.submit {
                threadLocal.set(true)
                barrier1.await(10, TimeUnit.SECONDS) // sync: both set
                barrier2.await(10, TimeUnit.SECONDS) // wait for thread 2 to remove
                thread1Value = threadLocal.get()     // read after thread 2 removed its value
            },
            executor.submit {
                threadLocal.set(true)
                barrier1.await(10, TimeUnit.SECONDS) // sync: both set
                threadLocal.remove()                 // thread 2 removes its value
                barrier2.await(10, TimeUnit.SECONDS) // signal thread 1 it can read
            }
        ]
        futures.each { it.get(30, TimeUnit.SECONDS) }
        executor.shutdown()

        then: "thread 1's value is unaffected by thread 2's removal"
        thread1Value == true
    }
}
