package liquibase.util

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
        message.startsWith("The file path/to/file was not found in")
        message.endsWith("Specifying files by absolute path was removed in Liquibase 4.0. Please use a relative path or add '/' to the classpath parameter.")
    }

}
