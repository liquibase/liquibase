package liquibase.integration.commandline

import liquibase.GlobalConfiguration
import liquibase.Scope
import spock.lang.Specification

class CommandLineUtilsTest extends Specification {

    def getBanner() {
        when:
        String banner = Scope.child([(GlobalConfiguration.SHOW_BANNER.key): true], { ->
            return CommandLineUtils.getBanner()
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        banner.contains("Get documentation at docs.liquibase.com")
        banner.contains("Starting Liquibase at");
        banner.contains("version ")
    }

    def "getBanner with banner disabled"() {
        when:
        String banner = Scope.child([(GlobalConfiguration.SHOW_BANNER.key): false], { ->
            return CommandLineUtils.getBanner()
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        !banner.contains("Get documentation at docs.liquibase.com")
        banner.contains("Starting Liquibase at");
        banner.contains("version ")
    }
}
