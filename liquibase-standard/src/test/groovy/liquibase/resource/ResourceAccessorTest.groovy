package liquibase.resource

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.ui.UIService
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Tests for the ResourceAccessor interface and its default methods.
 */
class ResourceAccessorTest extends Specification {

    static final String TEST_PATH = "test/path"
    static final String DUPLICATE_FILES_MESSAGE = "Found 2 files with the path 'test/path'"

    Resource resource1
    Resource resource2
    TestResourceAccessor resourceAccessor
    UIService mockUIService

    def setup() {
        resource1 = Mock(Resource) {
            getUri() >> new URI("file:/mock1")
        }
        resource2 = Mock(Resource) {
            getUri() >> new URI("file:/mock2")
        }

        resourceAccessor = new TestResourceAccessor([resource1, resource2])
        mockUIService = Mock(UIService)
    }

    /**
     * Test that get method throws IOException for duplicate resources when DUPLICATE_FILE_MODE is ERROR.
     */
    def "test get throws IOException for duplicate resources when DUPLICATE_FILE_MODE is ERROR"() {
        when:
        Scope.child([
                (GlobalConfiguration.DUPLICATE_FILE_MODE.key): GlobalConfiguration.DuplicateFileMode.ERROR,
                (Scope.Attr.ui.name()): mockUIService
        ], new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                resourceAccessor.get(TEST_PATH)
            }
        })

        then:
        def e = thrown(IOException)
        e.message.contains(DUPLICATE_FILES_MESSAGE)

        and:
        0 * mockUIService.sendMessage(_ as String)
    }

    /**
     * Test that appropriate message is logged for duplicate resources when DUPLICATE_FILE_MODE is WARN, INFO, or DEBUG.
     */
    @Unroll
    def "test message logged for duplicate resources when DUPLICATE_FILE_MODE is #mode"() {
        when:
        Scope.child([
                (GlobalConfiguration.DUPLICATE_FILE_MODE.key): mode,
                (Scope.Attr.ui.name()): mockUIService
        ], new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                resourceAccessor.get(TEST_PATH)
            }
        })

        then:
        noExceptionThrown()

        and:
        1 * mockUIService.sendMessage(_ as String) >> { String message ->
            assert message.contains(DUPLICATE_FILES_MESSAGE)
        }

        where:
        mode << [GlobalConfiguration.DuplicateFileMode.WARN, GlobalConfiguration.DuplicateFileMode.INFO, GlobalConfiguration.DuplicateFileMode.DEBUG]
    }

    /**
     * Test that nothing is logged for duplicate resources when DUPLICATE_FILE_MODE is SILENT.
     */
    def "test nothing logged for duplicate resources when DUPLICATE_FILE_MODE is SILENT"() {
        when:
        Scope.child([
                (GlobalConfiguration.DUPLICATE_FILE_MODE.key): GlobalConfiguration.DuplicateFileMode.SILENT,
                (Scope.Attr.ui.name()): mockUIService
        ], new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                resourceAccessor.get(TEST_PATH)
            }
        })

        then:
        0 * mockUIService.sendMessage(_ as String)
    }

    def "NotFoundResource handles path with spaces"() {
        when:
        def notFound = new ResourceAccessor.NotFoundResource("some path with spaces.txt", resourceAccessor)

        then:
        noExceptionThrown()
        notFound.getPath() == "some path with spaces.txt"
        notFound.getUri() != null
    }

    def "NotFoundResource handles path with curly braces"() {
        when:
        def notFound = new ResourceAccessor.NotFoundResource("path/with{curly}braces.txt", resourceAccessor)

        then:
        noExceptionThrown()
        notFound.getPath() == "path/with{curly}braces.txt"
        notFound.getUri() != null
    }

    @Unroll
    def "NotFoundResource handles path with URI-illegal characters: #description"() {
        when:
        def notFound = new ResourceAccessor.NotFoundResource(path, resourceAccessor)

        then:
        noExceptionThrown()
        notFound.getUri() != null

        where:
        path                                  | description
        "value with spaces"                   | "spaces"
        "path/with{braces}"                   | "curly braces"
        "some [bracketed] path"               | "square brackets"
        "path with {braces} and spaces"       | "braces and spaces"
        "simple/path.txt"                     | "simple path (no special chars)"
        "path\\with\\backslashes"             | "backslashes"
    }

    /**
     * Inner class for testing purposes.
     */
    class TestResourceAccessor implements ResourceAccessor {
        private List<Resource> resources

        TestResourceAccessor(List<Resource> resources) {
            this.resources = resources
        }

        @Override
        List<Resource> search(String path, boolean recursive) throws IOException {
            return resources
        }

        @Override
        List<Resource> getAll(String path) throws IOException {
            return resources
        }

        @Override
        List<String> describeLocations() {
            return []
        }

        @Override
        void close() {}
    }
}
