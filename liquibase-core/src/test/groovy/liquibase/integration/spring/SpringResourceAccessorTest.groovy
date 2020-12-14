package liquibase.integration.spring

import org.springframework.core.io.DefaultResourceLoader
import spock.lang.Specification

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
        def list = resourceAccessor.list(null, "liquibase/database", false, true, false).toListString()

        then:
        list.contains("AbstractJdbcDatabaseTest.class,")
        list.contains("DatabaseFactoryTest.class,")
        !list.contains("core,")
    }

    def "list just non-recursive directories"() {
        when:
        def list = resourceAccessor.list(null, "liquibase/database", false, false, true).toListString()

        then:
        !list.contains("AbstractJdbcDatabaseTest.class,")
        !list.contains("DatabaseFactoryTest.class,")
        list.contains("core/")
    }

    def "list recursive files and directories"() {
        when:
        def list = resourceAccessor.list(null, "liquibase", true, true, true).toListString()

        then:
        list.contains("database/core/UnsupportedDatabaseTest.class,")
        list.contains("database/core/")
        list.contains("liquibase/sqlgenerator/core/SelectFromDatabaseChangeLogGeneratorTest.class")
    }

    def "list relative to file"() {
        when:
        def list = resourceAccessor.list("liquibase/database/Database.class", "core", true, true, true).toListString()

        then:
        !list.contains("/Database.class,")
        list.contains("/OracleDatabaseTest.class,")
        list.contains("MSSQLDatabaseTest.class,")
    }


    def "list relative to directory"() {
        when:
        def list = resourceAccessor.list("liquibase/database", "core", true, true, true).toListString()

        then:
        !list.contains("/Database.class,")
        list.contains("/OracleDatabaseTest.class,")
        list.contains("MSSQLDatabaseTest.class,")
    }


}
