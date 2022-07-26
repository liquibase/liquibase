package liquibase.resource

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class CompositeResourceAccessorTest extends Specification {

    ResourceAccessor first;
    ResourceAccessor second;
    CompositeResourceAccessor composite;
    @Shared
    InputStream validStream = this.getClass().getClassLoader().getResourceAsStream("liquibase/resource/CompositeResourceAccessorTest.class")
    InputStreamList empty = new InputStreamList()
    @Shared
    SortedSet<String> validResources;

    def setupSpec() {
        validResources = new TreeSet<>()
        def resources = this.getClass().getClassLoader().getResources("liquibase")
        while (resources.hasMoreElements()) {
            validResources.add(resources.nextElement().toExternalForm())
        }

    }

    def setup() {
        first = Mock(ResourceAccessor.class);
        second = Mock(ResourceAccessor.class);
        composite = new CompositeResourceAccessor(first, second);
    }

    def cleanup() {
        if (validStream != null) {
            validStream.close();
        }

    }
//
//    @Unroll
//    def "list"() {
//        when:
//        1 * first.list("file", true) >> firstAccessorMock
//        1 * second.list("file", true) >> secondAccessorMock
//        def list = composite.list("file", true);
//
//        then:
//        list == expected
//
//        where:
//        firstAccessorMock | secondAccessorMock | expected
//        validResources    | [] as List         | validResources
//        [] as List        | validResources     | validResources
//        [] as List        | [] as List         | [] as List
//        validResources    | validResources     | validResources
//    }
}
