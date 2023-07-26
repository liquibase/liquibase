package liquibase.parser.core.xml

import liquibase.GlobalConfiguration
import liquibase.LiquibaseTest
import liquibase.Scope
import liquibase.resource.DirectoryResourceAccessor
import liquibase.util.LiquibaseUtil
import org.xml.sax.InputSource
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
                "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd",
                "https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd",
                "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd",
                "/liquibase/banner.txt", //can find files without hostnames
                "http://liquibase/banner.txt", //conversion of hostnames to files works for not just liquibase.org URLs
        ]
    }

    @Unroll
    def "warning message for mismatched xsd and build versions #systemId /// #buildVersion"() {
        given:
        def uiService = new LiquibaseTest.TestConsoleUIService()
        // Save these props for later
        def originalProperties = LiquibaseUtil.liquibaseBuildProperties
        LiquibaseUtil.liquibaseBuildProperties = new Properties()
        LiquibaseUtil.liquibaseBuildProperties.put("build.version", buildVersion)
        def er = new LiquibaseEntityResolver()
        er.setShouldWarnOnMismatchedXsdVersion(true)
        er.hasWarnedAboutMismatchedXsdVersion = false

        expect:
        Scope.child([
                (Scope.Attr.ui.name())        : uiService
        ], {
            er.resolveEntity(null, null, null, systemId)
        } as Scope.ScopedRunnerWithReturn<InputSource>) != null

        // This is an ugly assertion line, it is essentially saying, either we expect the message, so make sure it's there
        // or we expect no message, so make sure there are no messages.
        ((expectedWarningMessage && uiService.getMessages().contains("INFO: An older version of the XSD is specified in one or more changelog's <databaseChangeLog> header. This can lead to unexpected outcomes. If a specific XSD is not required, please replace all XSD version references with \"-latest\". Learn more at https://docs.liquibase.com/concepts/changelogs/xml-format.html"))
        || (!expectedWarningMessage && uiService.getMessages().isEmpty()))

        cleanup:
        // Set the build properties back to what they were before the test.
        LiquibaseUtil.liquibaseBuildProperties = originalProperties

        where:
        buildVersion | systemId | expectedWarningMessage
        "3.1.0" | "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd" | false
        "3.1.1" | "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd" | false
        "4.12.0" | "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd" | true
        "4.12.0" | "https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd" | true
        "4.12.0" | "http://www.liquibase.org/xml/ns/migrator/dbchangelog-3.1.xsd" | true
        "4.12.0" | "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd" | false
        "4.12.0" | "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd" | false
        "4.12.0" | "/liquibase/banner.txt" | false
        "4.12.0" | "http://liquibase/banner.txt" | false
    }

    @Unroll
    def "resolveEntity finds packaged files correctly even if the configured resourceAccessor doesn't have it"() {
        expect:
        Scope.child([(Scope.Attr.resourceAccessor.name()): new DirectoryResourceAccessor(new File("."))], { ->
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
