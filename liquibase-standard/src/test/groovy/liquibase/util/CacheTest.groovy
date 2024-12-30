package liquibase.util

import spock.lang.Specification

import java.util.concurrent.TimeUnit

class CacheTest extends Specification {

    def "time to live is respected"() {
        when:
        Cache<Long> cache = new Cache<>(() -> {
            return new Date().getTime()
        }, false, TimeUnit.SECONDS.toMillis(1))

        def get1 = cache.get()
        def get2 = cache.get()
        TimeUnit.SECONDS.sleep(2)
        def get3 = cache.get()

        then:
        get1 == get2
        get1 != get3
    }
}
