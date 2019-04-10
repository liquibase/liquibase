package liquibase.resource

import spock.lang.Specification
import spock.lang.Unroll

class ClassLoaderResourceAccessorTest extends Specification {

    def "rootUrls populated"() {
        when:
        def accessor = new ClassLoaderResourceAccessor(this.getClass().getClassLoader(), new URLClassLoader([
                new File(System.getProperty("java.io.tmpdir")).toURL()].toArray() as URL[]
        ))

        then:
        accessor.getRootPaths().size() >= 3
        accessor.getRootPaths().findAll({ it.endsWith("test-classes") }).size() == 1
        accessor.getRootPaths().findAll({ it.endsWith("classes") }).size() == 1
    }

    def "can recursively enumerate files inside JARs on the classpath"() {
        given:
        def accessor = new ClassLoaderResourceAccessor(getClass().getClassLoader())

        when:
        def listedResources = accessor.list(null, "org/junit", true, true, false)

        then:
        listedResources.contains("org/junit/Assert.class")
        listedResources.contains("org/junit/runner/Runner.class")
    }

    def "can non-recursively enumerate files inside JARs on the classpath"() {
        given:
        def accessor = new ClassLoaderResourceAccessor(getClass().getClassLoader())

        when:
        def listedResources = accessor.list(null, "org/junit", false, true, false)

        then:
        listedResources.contains("org/junit/Assert.class")
        !listedResources.contains("org/junit/runner/Runner.class")
    }
}
