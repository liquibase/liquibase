package liquibase.resource


import spock.lang.Specification
import spock.lang.Unroll

import static liquibase.plugin.Plugin.PRIORITY_DEFAULT
import static liquibase.plugin.Plugin.PRIORITY_NOT_APPLICABLE

class FileSystemResourceRootParserTest extends Specification {

    @Unroll
    def "getPriority: #input"() {
        expect:
        new FileSystemResourceRootParser().getPriority(input) == expected

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
}
