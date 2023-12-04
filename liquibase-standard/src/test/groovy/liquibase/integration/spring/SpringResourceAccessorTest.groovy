package liquibase.integration.spring

import liquibase.test.TestContext
import org.springframework.core.io.DefaultResourceLoader
import spock.lang.Specification
import spock.lang.Unroll

class SpringResourceAccessorTest extends Specification {

    def loader = new DefaultResourceLoader(new URLClassLoader([new File(TestContext.getInstance().findCoreJvmProjectRoot(), "/target/classes").toURI().toURL()] as URL[]))
    def resourceAccessor = new SpringResourceAccessor(loader)

    def "getAll"() {
        expect:
        resourceAccessor.getAll("liquibase/integration/spring/SpringResourceAccessorTest.class")*.getPath().equals(["liquibase/integration/spring/SpringResourceAccessorTest.class"])
        resourceAccessor.getAll("invalid/path") == null
    }

    def "openStreams for relative file in root"() {
        when:
        def list = resourceAccessor.openStreams("file-in-root.txt", "liquibase/database/core/UnsupportedDatabase.class")

        then:
        list.size() == 1
    }

    def "list just non-recursive files"() {
        when:
        def list = resourceAccessor.search("liquibase/database", false)*.getPath()

        then:
        list.contains("liquibase/database/AbstractJdbcDatabaseTest.class")
        list.contains("liquibase/database/DatabaseFactoryTest.class")
        !list.contains("core,")
    }

    def "search recursive files"() {
        when:
        def list = resourceAccessor.search("liquibase/database", true)*.getPath()

        then:
        list.contains("liquibase/database/AbstractJdbcDatabaseTest.class")
        list.contains("liquibase/database/DatabaseFactoryTest.class")
        list.contains("liquibase/database/core/H2Database.class")
        list.contains("liquibase/database/test-changelog with-space-in-filename.yml")
    }

    def "list relative to file"() {
        when:
        def list = resourceAccessor.list("liquibase/database/Database.class", "core", true, true, true).toListString()

        then:
        !list.contains("/Database.class,")
        list.contains("/OracleDatabaseTest.class,")
        list.contains("MSSQLDatabaseTest.class,")
    }

    def "list relative to file in root"() {
        when:
        def list = resourceAccessor.list("liquibase.properties", "liquibase/database/core", false, true, true).toListString()

        then:
        !list.contains("file-in-root.txt")
        list.contains("/OracleDatabaseTest.class,")
        list.contains("MSSQLDatabaseTest.class,")
    }


    def "list relative to directory"() {
        when:
        def list = resourceAccessor.list("liquibase/database", "core", true, true, true).toListString()

        then:
        thrown(IOException)
    }

    @Unroll
    def finalizeSearchPath() {
        expect:
        new SpringResourceAccessor().finalizeSearchPath(input) == expected

        where:
        input                               | expected
        "/path/to/file"                     | "classpath*:/path/to/file"
        "//path////to/file"                 | "classpath*:/path/to/file"
        "path/to/file"                      | "classpath*:/path/to/file"
        "classpath:path/to/file"            | "classpath*:/path/to/file"
        "classpath:/path/to/file"           | "classpath*:/path/to/file"
        "classpath:classpath:/path/to/file" | "classpath*:/path/to/file"
        "classpath*:/path/to/file"          | "classpath*:/path/to/file"
        "classpath*:path/to/file"           | "classpath*:/path/to/file"
        "file:/path/to/file"                | "file:/path/to/file"
    }
}
