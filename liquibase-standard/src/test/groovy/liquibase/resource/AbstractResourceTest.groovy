package liquibase.resource

import spock.lang.Specification
import spock.lang.Unroll

class AbstractResourceTest extends Specification {

    /**
     * Concrete subclass for testing AbstractResource behavior.
     */
    static class TestResource extends AbstractResource {
        TestResource(String path, URI uri) {
            super(path, uri)
        }

        @Override
        InputStream openInputStream() throws IOException {
            throw new UnsupportedOperationException()
        }

        @Override
        boolean exists() {
            return true
        }

        @Override
        Resource resolve(String other) {
            return new TestResource(resolvePath(other), null)
        }

        @Override
        Resource resolveSibling(String other) {
            return new TestResource(resolveSiblingPath(other), null)
        }
    }

    @Unroll
    def "handlePathForJarfile does not throw for URI-illegal path: #description"() {
        given:
        // Simulate a jar URI with "!" separator — this triggers handlePathForJarfile
        def jarUri = new URI("jar:file:/some/lib.jar!/com/example/resource.txt")

        when:
        def resource = new TestResource(path, jarUri)

        then:
        noExceptionThrown()
        resource.getPath() != null

        where:
        path                              | description
        "com/example/resource.txt"        | "normal path"
        "path with spaces.txt"            | "spaces in path"
        "../relative/path.txt"            | "relative path"
        "/absolute/path.txt"              | "absolute path"
    }

    def "handlePathForJarfile falls back gracefully for invalid URI characters"() {
        given:
        // A jar URI that triggers handlePathForJarfile
        def jarUri = new URI("jar:file:/some/lib.jar!/com/example/resource.txt")
        // A path containing spaces — when used in new URI(path), it will throw URISyntaxException
        def pathWithSpaces = "path with spaces/file.txt"

        when:
        def resource = new TestResource(pathWithSpaces, jarUri)

        then:
        noExceptionThrown()
        // The fallback strips leading "/" and returns the path as-is
        resource.getPath() == "path with spaces/file.txt"
    }

    def "constructor handles null URI"() {
        when:
        def resource = new TestResource("some/path.txt", null)

        then:
        noExceptionThrown()
        resource.getPath() == "some/path.txt"
        resource.getUri() == null
    }

    def "constructor strips classpath prefix"() {
        when:
        def resource = new TestResource("classpath:some/path.txt", null)

        then:
        resource.getPath() == "some/path.txt"
    }

    def "constructor normalizes backslashes"() {
        when:
        def resource = new TestResource("some\\path\\file.txt", null)

        then:
        resource.getPath() == "some/path/file.txt"
    }
}
