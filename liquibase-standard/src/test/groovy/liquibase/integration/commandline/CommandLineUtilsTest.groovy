package liquibase.integration.commandline

import liquibase.GlobalConfiguration
import liquibase.Scope
import spock.lang.Specification

import static java.util.ResourceBundle.getBundle

class CommandLineUtilsTest extends Specification {

    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core")

    def getBanner() {
        when:
        String banner = Scope.child([(GlobalConfiguration.SHOW_BANNER.key): true], { ->
            return CommandLineUtils.getBanner()
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        banner.contains("Get documentation at docs.liquibase.com")
        banner.contains(coreBundle.getString("starting.liquibase.at.timestamp").replace("%s", ""))
        banner.contains(coreBundle.getString("liquibase.version.builddate").replaceFirst("%s.*", ""))
    }

    def "getBanner with banner disabled"() {
        when:
        String banner = Scope.child([(GlobalConfiguration.SHOW_BANNER.key): false], { ->
            return CommandLineUtils.getBanner()
        } as Scope.ScopedRunnerWithReturn<String>)

        then:
        !banner.contains("Get documentation at docs.liquibase.com")
        banner.contains(coreBundle.getString("starting.liquibase.at.timestamp").replace("%s", ""))
        banner.contains(coreBundle.getString("liquibase.version.builddate").replaceFirst("%s.*", ""))
    }
}
