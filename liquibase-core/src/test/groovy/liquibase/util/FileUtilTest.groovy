package liquibase.util

import liquibase.resource.DirectoryPathHandler
import spock.lang.IgnoreIf
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Unroll

class FileUtilTest extends Specification {

    @Unroll
    def "getContents"() {
        expect:
        FileUtil.getContents("pom.xml" as File).contains("<project")

        FileUtil.getContents("invalid.file" as File) == null
    }

    def "write"() {
        when:
        def tempFile = File.createTempFile("FileUtilTest-", ".txt")
        tempFile.deleteOnExit()

        FileUtil.write("test content here", tempFile)

        then:
        FileUtil.getContents(tempFile) == "test content here"
    }

    def "getFileNotFoundMessage"() {
        when:
        def message = FileUtil.getFileNotFoundMessage("path/to/file")

        then:
        message.startsWith("The file path/to/file was not found in the configured search path")
        message.endsWith("More locations can be added with the 'searchPath' parameter.")
    }

    @Requires({ System.getProperty("os.name").toLowerCase().contains("win") })
    @Unroll
    def "isAbsolute (Windows): #input"() {
        expect:
        FileUtil.isAbsolute(input) == expected

        where:
        input                       | expected
        null                        | false
        "simple"                    | false
        "with/path"                 | false
        "with\\path"                | false
        "c:\\windows\\path"         | true
        "c:/windows/path"           | true
        "/c:/windows/path"          | true
        "D:\\windows\\path"         | true
        "file:/tmp/liquibase.xml"   | false
        "file:///tmp/liquibase.xml" | false
    }

    @IgnoreIf({ System.getProperty("os.name").toLowerCase().contains("win") })
    @Unroll
    def "isAbsolute (Linux): #input"() {
        expect:
        FileUtil.isAbsolute(input) == expected

        where:
        input                       | expected
        null                        | false
        "simple"                    | false
        "with/path"                 | false
        "/etc/config"               | true
    }

}
