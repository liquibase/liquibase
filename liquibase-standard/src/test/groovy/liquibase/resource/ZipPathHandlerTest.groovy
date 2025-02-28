package liquibase.resource

import liquibase.plugin.Plugin
import liquibase.servicelocator.PrioritizedService
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Unroll

class ZipPathHandlerTest extends Specification {

    @Requires({ os.windows })
    @Unroll
    def "getResourceAccessor with different root patterns #input"() {
        when:
        new ZipPathHandler().getResourceAccessor(input)

        then:
        //exception message shows how the file is converted
        def e = thrown(FileNotFoundException)
        e.message.startsWith("Non-existent file: ")
        e.message.contains(expected)

        where:
        input                                  | expected
        "c:/path/here.jar"                     | ":\\path\\here.jar"
        "c:\\path\\here.jar"                   | ":\\path\\here.jar"
        "/path/here.jar"                       | ":\\path\\here.jar"
        "\\path\\here.jar"                     | ":\\path\\here.jar"
        "file:/C:/path/here.jar"               | ":\\path\\here.jar"
        "jar:file:/C:/path/here.jar"           | ":\\path\\here.jar"
        "file:/C:/path/with%20spaces/here.jar" | ":\\path\\with spaces\\here.jar"
        "jar:file:/C:/path/outer.jar!/BOOT-INF/lib/embedded.jar!/" | ":\\path\\outer.jar"
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
        "jar:file:/C:/path/outer.jar!/BOOT-INF/lib/embedded.jar!/" | Plugin.PRIORITY_SPECIALIZED
    }
}
