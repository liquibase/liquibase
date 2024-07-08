package liquibase.logging


import spock.lang.Specification

class LogFactoryTest extends Specification {

    def getInstance() {
        expect:
        LogFactory.getInstance() != null
    }
}
