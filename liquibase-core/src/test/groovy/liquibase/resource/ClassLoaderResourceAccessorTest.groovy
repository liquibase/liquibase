package liquibase.resource

import spock.lang.Specification
import spock.lang.Unroll

class ClassLoaderResourceAccessorTest extends Specification {

    def "can recursively enumerate files inside JARs on the classpath"() {
        given:
        def accessor = new ClassLoaderResourceAccessor(Thread.currentThread().contextClassLoader)

        when:
        def listedResources = accessor.list(null, "org/apache/log4j", true, false, true)

        then:
        listedResources.contains("org/apache/log4j/Logger.class") == true
        listedResources.contains("org/apache/log4j/spi/Filter.class") == true
    }

    def "can non-recursively enumerate files inside JARs on the classpath"() {
        given:
        def accessor = new ClassLoaderResourceAccessor(Thread.currentThread().contextClassLoader)

        when:
        def listedResources = accessor.list(null, "org/apache/log4j", true, false, false)

        then:
        listedResources.contains("org/apache/log4j/Logger.class") == true
        listedResources.contains("org/apache/log4j/spi/Filter.class") == false
    }
}
