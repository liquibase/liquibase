package liquibase.integration.commandline

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class LiquibaseCommandLineThreadingTest extends Specification {

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
