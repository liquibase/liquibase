package liquibase.resource

import spock.lang.Specification
import spock.lang.Unroll

class AbstractResourceAccessorTest extends Specification {

    @Unroll("#featureName: #path -> #expected")
    def "convertToPath creates correct paths"() {
        when:
        def accessor = createResourceAccessor(["file:/root/path"], true)

        then:
        accessor.convertToPath(path) == expected

        where:
        path                               | expected
        "short.file"                       | "short.file"
        "/short.file"                      | "/short.file"
        "\\short.file"                     | "/short.file"
        "//short.file"                     | "/short.file"
        "some/path/Here.file"              | "some/path/Here.file"
        "some\\path\\Here.file"            | "some/path/Here.file"
        "some//path///Here.file"           | "some/path/Here.file"
        "some\\\\path\\\\\\Here.file"      | "some/path/Here.file"
        "path/./with/./dirs"               | "path/with/dirs"
        "path/./with/.dirs"                | "path/with/.dirs"
        "file:/c:/"                        | "file:/c:/"
        "http://example.local/nowhere.txt" | "http://example.local/nowhere.txt"
    }

    @Unroll("#featureName: #path -> #expected")
    def "convertToPath with windows rootUrls"() {
        when:
        def accessor = createResourceAccessor(["file:/C:/path/to/target/test-classes/",
                                               "file:/C:/path/to/target/classes/"], false);

        then:
        accessor.convertToPath(path) == expected

        where:
        path                                              | expected
        "short.file"                                      | "short.file"
        "/short.file"                                     | "/short.file"
        "file:/C:/path/to/target/test-classes/short.file" | "short.file"
        "file:/C:/path/to/target/classes/other/file.ext"  | "other/file.ext"
        "file:/C:/path/to/target/outside/short.file"      | "file:/C:/path/to/target/outside/short.file"
        "/C:/path/to/target/classes/other/file.ext"       | "other/file.ext"
        "C:/path/to/target/classes/other/file.ext"        | "other/file.ext"
        "C:\\path\\to\\target\\classes\\other\\file.ext"  | "other/file.ext"
        "c:\\path\\to\\target\\classes\\other\\file.ext"  | "other/file.ext"
    }

    @Unroll("#featureName: #path -> #expected")
    def "convertToPath with linux rootUrls"() {
        when:
        def accessor = createResourceAccessor(["file:/path/to/target/test-classes/",
                                               "file:/path/to/target/classes/"], false);

        then:
        accessor.convertToPath(path) == expected

        where:
        path                                           | expected
        "short.file"                                   | "short.file"
        "/short.file"                                  | "/short.file"
        "file:/path/to/target/test-classes/short.file" | "short.file"
        "file:/path/to/target/classes/other/file.ext"  | "other/file.ext"
        "file:/path/to/target/outside/short.file"      | "file:/path/to/target/outside/short.file"
        "/path/to/target/classes/other/file.ext"       | "other/file.ext"
        "\\path\\to\\target\\classes\\other\\file.ext" | "other/file.ext"
    }

    protected AbstractResourceAccessor createResourceAccessor(List rootUrls, boolean caseSensitive) {
        def rootUrlsSet = new ArrayList(rootUrls)
        new AbstractResourceAccessor() {
            @Override
            protected void init() {}

            @Override
            protected List<String> getRootPaths() {
                return rootUrlsSet;
            }

            @Override
            protected boolean isCaseSensitive() {
                return caseSensitive;
            }

            @Override
            Set<InputStream> getResourcesAsStream(String path) throws IOException {
                return null
            }

            @Override
            Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
                return null
            }

            @Override
            ClassLoader toClassLoader() {
                return null
            }

        }
    }
}
