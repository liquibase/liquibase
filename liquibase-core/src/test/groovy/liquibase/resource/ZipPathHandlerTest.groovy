package liquibase.resource

import liquibase.plugin.Plugin
import liquibase.servicelocator.PrioritizedService
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Unroll

class ZipPathHandlerTest extends Specification {

    @Requires({ os.windows })
    @Unroll
    def "getResourceAccessor with different root patterns"() {
        when:
        new ZipPathHandler().getResourceAccessor(input)

        then:
        //exception message shows how the file is converted
        def e = thrown(IllegalArgumentException)
        e.message == "Non-existent file: $expected"

        where:
        input                                  | expected
        "c:/path/here.jar"                     | "c:\\path\\here.jar"
        "c:\\path\\here.jar"                   | "c:\\path\\here.jar"
        "/path/here.jar"                       | "C:\\path\\here.jar"
        "\\path\\here.jar"                     | "C:\\path\\here.jar"
        "file:/C:/path/here.jar"               | "C:\\path\\here.jar"
        "jar:file:/C:/path/here.jar"           | "C:\\path\\here.jar"
        "file:/C:/path/with%20spaces/here.jar" | "C:\\path\\with spaces\\here.jar"
    }

    @Unroll
    def supports() {
        expect:
        new ZipPathHandler().getPriority(input) == expected

        where:
        input                             | expected
        "path/here.jar"                   | Plugin.PRIORITY_SPECIALIZED
        "path/here.zip"                   | Plugin.PRIORITY_SPECIALIZED
        "path/there"                      | Plugin.PRIORITY_NOT_APPLICABLE
        "file:/C:/path/here.jar"          | Plugin.PRIORITY_SPECIALIZED
        "jar:file:/C:/path/here.jar!/bar" | Plugin.PRIORITY_NOT_APPLICABLE
        "jar:file:/C:/path/here.jar"      | Plugin.PRIORITY_SPECIALIZED

    }
}
