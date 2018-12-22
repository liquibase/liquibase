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

    @Unroll
    def "openStreams"() {
        when:
        1 * first.openStreams("file") >> firstAccessorMock
        1 * second.openStreams("file") >> secondAccessorMock
        def is = composite.openStreams("file");

        then:
        is.streams == expected;

        where:
        firstAccessorMock                                           | secondAccessorMock                                          | expected
        new InputStreamList(new URI("test://stream1"), validStream) | null                                                        | [(new URI("test://stream1")): validStream]
        null                                                        | new InputStreamList(new URI("test://stream2"), validStream) | [(new URI("test://stream2")): validStream]
        null                                                        | null                                                        | [:]
        new InputStreamList(new URI("test://stream1"), validStream) | new InputStreamList(new URI("test://stream2"), validStream) | [(new URI("test://stream1")): validStream, (new URI("test://stream2")): validStream]
    }

    @Unroll
    def "list"() {
        when:
        1 * first.list("file", true, true, true) >> firstAccessorMock
        1 * second.list("file", true, true, true) >> secondAccessorMock
        def list = composite.list("file", true, true, true);

        then:
        list == expected

        where:
        firstAccessorMock | secondAccessorMock | expected
        validResources    | [] as SortedSet    | validResources
        [] as SortedSet   | validResources     | validResources
        [] as SortedSet   | [] as SortedSet    | [] as SortedSet
        validResources    | validResources     | validResources
    }

    @Unroll
    def "getCanonicalPath"() {
        when:
        first.getCanonicalPath(null, "file") >> firstAccessorMock
        second.getCanonicalPath(null, "file") >> secondAccessorMock

        then:
        composite.getCanonicalPath(null, "file") == expected

        where:
        firstAccessorMock | secondAccessorMock | expected
        "x"               | null               | "x"
        "x"               | "y"                | "x"
        null              | "y"                | "y"
        null              | null               | null

    }
}