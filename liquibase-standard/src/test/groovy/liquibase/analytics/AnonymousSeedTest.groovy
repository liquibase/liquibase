package liquibase.analytics

import spock.lang.Specification

class AnonymousSeedTest extends Specification {

    def "psuedo-random user ID generation is consistent on the same machine over multiple creations"() {
        when:
        def seed1 = new AnonymousSeed().generateId()
        def seed2 = new AnonymousSeed().generateId()
        def seed3 = new AnonymousSeed().generateId()

        then:
        seed1.equals(seed2)
        seed1.equals(seed3)
    }
}
