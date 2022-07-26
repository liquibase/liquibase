package liquibase.integration.spring

import org.springframework.core.io.DefaultResourceLoader
import spock.lang.Specification
import spock.lang.Unroll

class SpringResourceAccessorTest extends Specification {

    def loader = new DefaultResourceLoader()
    def resourceAccessor = new SpringResourceAccessor(loader)

    def "openStreams for single file"() {
        when:
        def list = resourceAccessor.openStreams(null, "liquibase/integration/spring/SpringResourceAccessorTest.class")

        then:
        list.size() == 1
    }

    def "openStreams for relative file"() {
        when:
        def list = resourceAccessor.openStreams("liquibase/database/Database.class", "core/UnsupportedDatabase.class")

        then:
        list.size() == 1
    }

    def "list just non-recursive files"() {
        when:
        def list = resourceAccessor.list("liquibase/database", false).toListString()

        then:
        list.contains("AbstractJdbcDatabaseTest.class,")
        list.contains("DatabaseFactoryTest.class,")
        !list.contains("core,")
    }

    @Unroll
    def finalizeSearchPath() {
        expect:
        new SpringResourceAccessor().finalizeSearchPath(input) == expected

        where:
        input | expected
        "/path/to/file" | "classpath*:/path/to/file"
        "//path////to/file" | "classpath*:/path/to/file"
        "path/to/file" | "classpath*:/path/to/file"
        "classpath:path/to/file" | "classpath*:/path/to/file"
        "classpath:/path/to/file" | "classpath*:/path/to/file"
        "classpath:classpath:/path/to/file" | "classpath*:/path/to/file"
        "classpath*:/path/to/file" | "classpath*:/path/to/file"
        "classpath*:path/to/file" | "classpath*:/path/to/file"
        "file:/path/to/file" | "file:/path/to/file"
    }
}
