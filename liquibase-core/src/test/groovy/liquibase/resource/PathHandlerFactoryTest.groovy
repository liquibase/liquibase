package liquibase.resource

import liquibase.Scope
import liquibase.test.JUnitResourceAccessor
import liquibase.util.StreamUtil
import spock.lang.Specification
import spock.lang.Unroll

class PathHandlerFactoryTest extends Specification {

    def "parse file path"() {
        expect:
        Scope.getCurrentScope().getSingleton(PathHandlerFactory).getResourceAccessor("src/test/groovy") instanceof DirectoryResourceAccessor
    }

    @Unroll
    def "parse unparseable file path: #input"() {
        when:
        Scope.getCurrentScope().getSingleton(PathHandlerFactory).getResourceAccessor(input) instanceof DirectoryResourceAccessor

        then:
        def e = thrown(IOException)
        e.message == "Cannot parse resource location: '$input'"

        where:
        input << [null, "proto:unsupported"]
    }

    @Unroll
    def "getResource: #path"() {
        when:
        def pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory)

        then:
        assert (pathHandlerFactory.getResource(path).exists()) == exists

        where:
        path                                                               | exists
        "src/test/groovy/liquibase/resource/PathHandlerFactoryTest.groovy" | true
        "invalid/path.txt"                                                 | false
        "liquibase/resource/PathHandlerFactoryTest.class"                  | false
        "/liquibase/resource/PathHandlerFactoryTest.class"                 | false
    }

    def openResourceOutputStream() {
        when:
        def tempFile = File.createTempFile("DirectoryPathHandlerTest", ".tmp")
        tempFile.deleteOnExit()
        tempFile.delete()
        def path = tempFile.getAbsolutePath()

        def pathHandlerFactory = Scope.currentScope.getSingleton(PathHandlerFactory)

        then:
        pathHandlerFactory.openResourceOutputStream(path, new OpenOptions().setCreateIfNeeded(false)) == null //when createIfNotExists is false

        when:
        def stream = pathHandlerFactory.openResourceOutputStream(path, new OpenOptions().setCreateIfNeeded(true)) //createIfNotExists is true
        stream.withWriter {
            it.write("test")
        }
        stream.close()

        then:
        StreamUtil.readStreamAsString(pathHandlerFactory.getResource(path).openInputStream()) == "test"

        when:
        //can update file
        stream = pathHandlerFactory.openResourceOutputStream(path, new OpenOptions())
        stream.withWriter {
            it.write("test 2")
        }
        stream.close()

        then:
        StreamUtil.readStreamAsString(pathHandlerFactory.getResource(path).openInputStream()) == "test 2"
    }
}
