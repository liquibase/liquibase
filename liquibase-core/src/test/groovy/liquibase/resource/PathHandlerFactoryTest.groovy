package liquibase.resource

import liquibase.Scope
import spock.lang.Specification
import spock.lang.Unroll

class PathHandlerFactoryTest extends Specification {

    def "parse file path"() {
        expect:
        Scope.getCurrentScope().getSingleton(PathHandlerFactory).getResourceAccessor("a/path/here") instanceof DirectoryResourceAccessor
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
}
