package liquibase.resource

import spock.lang.Specification
import spock.lang.Unroll

class ClassLoaderResourceAccessorTest extends Specification {

    def "rootUrls populated"() {
        when:
        def accessor = new ClassLoaderResourceAccessor(new URLClassLoader([
                new File(System.getProperty("java.io.tmpdir")).toURL()].toArray() as URL[]
        ))

        then:
        accessor.getRootPaths().size() >= 3
        accessor.getRootPaths().findAll({ it.endsWith("/test-classes/") }).size() == 1
        accessor.getRootPaths().findAll({ it.endsWith("/classes/") }).size() == 1
    }

    @Unroll("#featureName: #relativeTo / #path -> #expected")
    def "convertToPath using relative paths"() {
        when:
        def accessor = new ClassLoaderResourceAccessor(new URLClassLoader([].toArray() as URL[]))

        then:
        accessor.convertToPath(relativeTo, path) == expected

        where:
        relativeTo                         | path                             | expected
        null                               | "liquibase/Liquibase.class"      | "liquibase/Liquibase.class"
        ""                                 | "liquibase/Liquibase.class"      | "liquibase/Liquibase.class"
        "liquibase"                        | "Liquibase.class"                | "liquibase/Liquibase.class"
        "liquibase"                        | "Contexts.class"                 | "liquibase/Contexts.class"
        "liquibase/Liquibase.class"        | "Contexts.class"                 | "liquibase/Contexts.class"
        "liquibase/"                       | "sql/Sql.class"                  | "liquibase/sql/Sql.class"
        "liquibase"                        | "sql/Sql.class"                  | "liquibase/sql/Sql.class"
        "liquibase/Liquibase.class"        | "sql/Sql.class"                  | "liquibase/sql/Sql.class"
        "liquibase/sql"                    | "../Liquibase.class"             | "liquibase/Liquibase.class"
    }

    def "can recursively enumerate files inside JARs on the classpath"() {
        given:
        def accessor = new ClassLoaderResourceAccessor(Thread.currentThread().contextClassLoader)

        when:
        def listedResources = accessor.list(null, "org/springframework/core/io", true, false, true)

        then:
        listedResources.contains("org/springframework/core/io/Resource.class")
        listedResources.contains("org/springframework/core/io/support/ResourcePatternUtils.class")
    }

    def "can non-recursively enumerate files inside JARs on the classpath"() {
        given:
        def accessor = new ClassLoaderResourceAccessor(Thread.currentThread().contextClassLoader)

        when:
        def listedResources = accessor.list(null, "org/springframework/core/io", true, false, false)

        then:
        listedResources.contains("org/springframework/core/io/Resource.class")
        !listedResources.contains("org/springframework/core/io/support/ResourcePatternUtils.class")
    }

    // Test case for [CORE-3139]
//    def "can recursively enumerate files inside JARs using JAR file URL"() {
//        given:
//        def accessor = new ClassLoaderResourceAccessor(Thread.currentThread().contextClassLoader)
//        def jarPkgURL = this.getClass().getClassLoader().getResource("org/springframework/core/io/").toExternalForm()
//
//        when:
//        def listedResources = accessor.list(null, jarPkgURL, true, false, true)
//
//        then:
//        listedResources.contains("org/springframework/core/io/Resource.class")
//        listedResources.contains("org/springframework/core/io/support/ResourcePatternUtils.class")
//    }

}
