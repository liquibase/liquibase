package liquibase.configuration

import spock.lang.Specification

class GlobalConfigurationTest extends Specification {

    def "string properties are set correctly"() {
        expect:
        assert GlobalConfiguration.HEADLESS == "liquibase.headless"
        assert GlobalConfiguration.SHOULD_RUN == "liquibase.shouldRun"
    }
}
