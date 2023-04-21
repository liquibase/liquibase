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
    List<Resource> validResources;

    def setupSpec() {
        validResources = new ArrayList<>()
        def resources = this.getClass().getClassLoader().getResources("liquibase")
        while (resources.hasMoreElements()) {
            URL element = resources.nextElement()
            validResources.add(new URIResource(element.toExternalForm().replaceFirst(".*/liquibase/", "liquibase/"), element.toURI()))
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

    @Unroll
    def "search"() {
        when:
        1 * first.search("file", true) >> firstAccessorMock
        1 * second.search("file", true) >> secondAccessorMock
        def list = composite.search("file", true);

        then:
        list == expected

        where:
        firstAccessorMock | secondAccessorMock | expected
        validResources    | [] as List         | validResources
        [] as List        | validResources     | validResources
        [] as List        | [] as List         | [] as List
        validResources    | validResources     | validResources
    }
}
