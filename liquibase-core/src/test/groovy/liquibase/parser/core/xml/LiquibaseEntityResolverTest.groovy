package liquibase.parser.core.xml

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.resource.FileSystemResourceAccessor
import spock.lang.Specification
import spock.lang.Unroll

class LiquibaseEntityResolverTest extends Specification {

    @Unroll
    def "resolveEntity finds packaged files correctly"() {
        expect:
        new LiquibaseEntityResolver().resolveEntity(null, null, null, systemId) != null

        where:
        systemId << [
                "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd",
                "https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd",
                "http://www.liquibase.org/xml/ns/migrator/dbchangelog-3.1.xsd",
                "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-next.xsd",
                "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd",
                "/liquibase/banner.txt", //can find files without hostnames
                "http://liquibase/banner.txt", //conversion of hostnames to files works for not just liquibase.org URLs
        ]
    }

    @Unroll
    def "resolveEntity finds packaged files correctly even if the configured resourceAccessor doesn't have it"() {
        expect:
        Scope.child([(Scope.Attr.resourceAccessor.name()): new FileSystemResourceAccessor(new File("."))], { ->
            new LiquibaseEntityResolver().resolveEntity(null, null, null, systemId) != null
        } as Scope.ScopedRunnerWithReturn) != null

        where:
        systemId << [
                "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd",
                "https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd",
        ]
    }

    @Unroll
    def "resolveEntity returns null for non-packaged files"() {
        expect:
        Scope.child(GlobalConfiguration.SECURE_PARSING.key, false, { ->
            new LiquibaseEntityResolver().resolveEntity(null, null, null, systemId) == null
        }) == null

        when:
        Scope.child(GlobalConfiguration.SECURE_PARSING.key, true, { ->
            new LiquibaseEntityResolver().resolveEntity(null, null, null, systemId) == null
        })

        then:
        def e = thrown(XSDLookUpException)
        e.message.startsWith("Unable to resolve xml entity")


        where:
        systemId << [
                "http://www.liquibase.org/xml/ns/dbchangelog/invalid.xsd",
                "http://www.example.com/xml/ns/dbchangelog/dbchangelog-3.1.xsd",
                "http://www.example.com/random/file.txt",
        ]
    }

    def "null systemId returns null"() {
        expect:
        new LiquibaseEntityResolver().resolveEntity("passed publicId", null) == null
        new LiquibaseEntityResolver().resolveEntity("passed name", "passed publicId", "passed baseURI", null) == null
    }

    def "getExternalSubset returns null"() {
        expect:
        assert new LiquibaseEntityResolver().getExternalSubset("pased name", "passed baseURI") == null
    }
}
