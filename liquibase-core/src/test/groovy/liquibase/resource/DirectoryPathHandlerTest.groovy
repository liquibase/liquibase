package liquibase.resource

import liquibase.util.StreamUtil
import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.plugin.Plugin.PRIORITY_DEFAULT
import static liquibase.plugin.Plugin.PRIORITY_NOT_APPLICABLE

class DirectoryPathHandlerTest extends Specification {

    @Unroll
    def "getPriority: #input"() {
        expect:
        new DirectoryPathHandler().getPriority(input) == expected

        where:
        input                       | expected
        null                        | PRIORITY_NOT_APPLICABLE
        "simple"                    | PRIORITY_DEFAULT
        "with/path"                 | PRIORITY_DEFAULT
        "with\\path"                | PRIORITY_DEFAULT
        "c:\\windows\\path"         | PRIORITY_DEFAULT
        "c:/windows/path"           | PRIORITY_DEFAULT
        "/c:/windows/path"          | PRIORITY_DEFAULT
        "D:\\windows\\path"         | PRIORITY_DEFAULT
        "file:/tmp/liquibase.xml"   | PRIORITY_DEFAULT
        "file:///tmp/liquibase.xml" | PRIORITY_DEFAULT
        "http://localhost"          | PRIORITY_NOT_APPLICABLE
    }

    def "open reads existing file"() {
        expect:
        StreamUtil.readStreamAsString(new DirectoryPathHandler().getResource("../README.md").openInputStream()).startsWith("# Liquibase")
    }

    def "getResource when does not exist"() {
        expect:
        new DirectoryPathHandler().getResource("/invalid/file/path.txt") == null
    }

    @Unroll
    def getResourceAccessor() {
        when:
        def accessor = new DirectoryPathHandler().getResourceAccessor(root)

        then:
        accessor instanceof DirectoryResourceAccessor
        ((DirectoryResourceAccessor) accessor).getRootPath().toString() == new File("").absolutePath.normalize().toString()

        where:
        root << [
                new File("").absolutePath,
                new File("").absolutePath.replace("/", "\\"),
                "file:/" + new File("").absolutePath,

        ]
    }
}
