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
        1 * first.openStreams(null, "file") >> firstAccessorMock
        1 * second.openStreams(null, "file") >> secondAccessorMock
        def is = composite.openStreams(null, "file");

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
        1 * first.list(null, "file", true, true, true) >> firstAccessorMock
        1 * second.list(null, "file", true, true, true) >> secondAccessorMock
        def list = composite.list(null, "file", true, true, true);

        then:
        list == expected

        where:
        firstAccessorMock | secondAccessorMock | expected
        validResources    | [] as SortedSet    | validResources
        [] as SortedSet   | validResources     | validResources
        [] as SortedSet   | [] as SortedSet    | [] as SortedSet
        validResources    | validResources     | validResources
    }

    def "classLoader.getResource"() {
        when:
        CompositeResourceAccessor composite = new CompositeResourceAccessor(
                new ClassLoaderResourceAccessor(new URLClassLoader([new File("./src/main/java/liquibase/resource").toURI().toURL()] as URL[])),
                new ClassLoaderResourceAccessor(new URLClassLoader([new File("./src/main/java/liquibase/precondition").toURI().toURL()] as URL[])),
        )

        then:
        composite.toClassLoader().getResource("CompositeResourceAccessor.java").toExternalForm().endsWith("src/main/java/liquibase/resource/CompositeResourceAccessor.java")
        composite.toClassLoader().getResource("Precondition.java").toExternalForm().endsWith("src/main/java/liquibase/precondition/Precondition.java")

        //can find from context classloader which is also included
        composite.toClassLoader().getResource("liquibase/precondition/Precondition.class").toExternalForm().endsWith("target/classes/liquibase/precondition/Precondition.class")
    }


    def "classLoader.getResources"() {
        when:
        CompositeResourceAccessor composite = new CompositeResourceAccessor(
                new ClassLoaderResourceAccessor(new URLClassLoader([new File("./src/main/java/liquibase/resource").toURI().toURL()] as URL[])),
                new ClassLoaderResourceAccessor(new URLClassLoader([new File("./src/main/java/liquibase/precondition").toURI().toURL()] as URL[])),
        )

        then:
        composite.toClassLoader().getResources("CompositeResourceAccessor.java")*.toExternalForm()*.endsWith("src/main/java/liquibase/resource/CompositeResourceAccessor.java")
        composite.toClassLoader().getResources("Precondition.java")*.toExternalForm()*.endsWith("src/main/java/liquibase/precondition/Precondition.java")

        assert "Did not find resource from context classloader which is also included", composite.toClassLoader().getResources("liquibase/precondition/Precondition.class")*.toExternalForm()*.endsWith("target/classes/liquibase/precondition/Precondition.class")

        assert "Did not return resources across nested classloaders", composite.toClassLoader().getResources("META-INF/MANIFEST.MF").toList().size() > 2
    }
}
