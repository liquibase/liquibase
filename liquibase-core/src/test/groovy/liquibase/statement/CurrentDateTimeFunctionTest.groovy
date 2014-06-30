package liquibase.statement

import spock.lang.Specification

class CurrentDateTimeFunctionTest extends Specification {

    def "constructor"() {
        expect:
        new CurrentDateTimeFunction().getText() == "CURRENT_TIME"
    }
}
