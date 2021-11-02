package liquibase.util

import spock.lang.Specification

class LiquibaseUtilTest extends Specification {

    def getBuildVersion() {
        expect:
        LiquibaseUtil.getBuildVersion() != "UNKNOWN"
    }


    def getBuildNumber() {
        expect:
        LiquibaseUtil.getBuildNumber() != "UNKNOWN"
    }

    def getBuildTime() {
        expect:
        LiquibaseUtil.getBuildTime() != "UNKNOWN"
    }
}
