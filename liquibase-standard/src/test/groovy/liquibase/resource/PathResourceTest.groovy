package liquibase.resource

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant

class PathResourceTest extends Specification {

    def "validate parent directory can be created successfully"() {
        when:
        String filePath = String.format("com/example/not/exists/%s/test.sql", Instant.now().toString())
        Path rootChangeLogPath = Paths.get(filePath)

        def pathResource =  new PathResource("test.sql", rootChangeLogPath)
        OutputStream outputStream = pathResource.openOutputStream(new OpenOptions())
        outputStream.write("test content".bytes)
        outputStream.close()

        then:
        Files.exists(rootChangeLogPath)

    }
}
