package liquibase.resource

import liquibase.util.StreamUtil
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.plugin.Plugin.PRIORITY_DEFAULT
import static liquibase.plugin.Plugin.PRIORITY_NOT_APPLICABLE

class FileSystemPathHandlerTest extends Specification {

    @Unroll
    def "getPriority: #input"() {
        expect:
        new FileSystemPathHandler().getPriority(input) == expected

        where:
        input                       | expected
        null                        | PRIORITY_NOT_APPLICABLE
        "simple"                    | PRIORITY_DEFAULT
        "with/path"                 | PRIORITY_DEFAULT
        "with\\path"                | PRIORITY_DEFAULT
        "c:\\windows\\path"         | PRIORITY_DEFAULT
        "c:/windows/path"           | PRIORITY_DEFAULT
        "/c:/windows/path"           | PRIORITY_DEFAULT
        "D:\\windows\\path"         | PRIORITY_DEFAULT
        "file:/tmp/liquibase.xml"   | PRIORITY_DEFAULT
        "file:///tmp/liquibase.xml" | PRIORITY_DEFAULT
        "http://localhost"          | PRIORITY_NOT_APPLICABLE
    }

    def "open reads existing file"() {
        expect:
        StreamUtil.readStreamAsString(new FileSystemPathHandler().getResource("../README.md").openInputStream()).startsWith("# Liquibase")
    }

    def "open fails on invalid file"() {
        when:
        def resource= new FileSystemPathHandler().getResource("/invalid/file/path.txt")

        then:
        assert resource == null
    }
}
