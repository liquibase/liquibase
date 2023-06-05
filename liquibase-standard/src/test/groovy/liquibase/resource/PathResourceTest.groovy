package liquibase.resource

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class PathResourceTest extends Specification {

    def "validate parent directory can be created successfully"() {
        when:
        String filePath = File.createTempFile("Test-", ".sql").getPath()
        Path rootChangeLogPath = Paths.get(filePath)

        def pathResource =  new PathResource("Test.sql", rootChangeLogPath)
        OutputStream outputStream = pathResource.openOutputStream(new OpenOptions())
        outputStream.write("test content".bytes)
        outputStream.close()

        then:
        Files.exists(rootChangeLogPath)

    }
}
