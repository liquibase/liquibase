package liquibase.resource

import liquibase.Scope
import spock.lang.Specification
import spock.lang.Unroll

class SearchPathHandlerFactoryTest extends Specification {

    def "parse file path"() {
        expect:
        Scope.getCurrentScope().getSingleton(SearchPathHandlerFactory).getResourceAccessor("a/path/here") instanceof FileSystemResourceAccessor
    }

    @Unroll
    def "parse unparseable file path: #input"() {
        when:
        Scope.getCurrentScope().getSingleton(SearchPathHandlerFactory).getResourceAccessor(input) instanceof FileSystemResourceAccessor

        then:
        def e = thrown(IOException)
        e.message == "Cannot parse resource location: '$input'"

        where:
        input << [null, "proto:unsupported"]
    }
}
