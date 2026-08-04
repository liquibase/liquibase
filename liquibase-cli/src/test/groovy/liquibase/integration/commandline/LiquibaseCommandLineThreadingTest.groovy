package liquibase.integration.commandline

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class LiquibaseCommandLineThreadingTest extends Specification {

    def "2 concurrent updates do not interfere with each other" () {
        given:
        // Reproduces #6927: commands running at the same time used to share the cached
        // CommandDefinition's step instances, letting one thread clean up the other's run.
        def startGate = new java.util.concurrent.CountDownLatch(1)
        def results = Collections.synchronizedList([])

        when:
        def threads = (1..2).collect { n ->
            Thread.start {
                startGate.await()
                3.times {
                    results.add(new LiquibaseCommandLine().execute("update", "--show-summary=OFF",
                            "--url=jdbc:h2:mem:liquibaseConcurrent" + n, "--changeLogFile=changelog.xml"))
                }
            }
        }
        startGate.countDown()
        threads*.join()

        then:
        results.size() == 6
        results.every { it == 0 }
    }

    @Unroll
    def "2 threads global flags" () {
        given:
        def returnCode = 0
        def returnCode2 = 0
        def returnCode3 = 0

        when:
        Thread.start {
            returnCode = new LiquibaseCommandLine().execute("update", "--show-summary=OFF", "--url=jdbc:h2:mem:liquibaseThreads", "--changeLogFile=changelog.xml")
        }.join()
        Thread.start {
            returnCode2 = new LiquibaseCommandLine().execute("update", "--show-summary=OFF", "--url=jdbc:h2:mem:liquibaseThreads", "--changeLogFile=changelog.xml")
        }.join()
        // should fail as we are not passing required arguments
        Thread.start {
            returnCode3 = new LiquibaseCommandLine().execute("update", "--show-summary=OFF")
        }.join()

        then:
        returnCode == 0
        returnCode2 == 0
        returnCode3 == 1
    }

}
