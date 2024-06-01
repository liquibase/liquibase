package liquibase.resource;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.LiquibaseException;
import org.apache.tools.ant.types.ResourceFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class UrlDrivenResourceAccessorTest {

    UrlDrivenResourceAccessor accessor;
    DatabaseChangeLog changeLog;

    @BeforeEach
    public void setUp() {
        this.accessor = new UrlDrivenResourceAccessor();
        this.changeLog = new DatabaseChangeLog();
        changeLog.setChangeLogParameters(new ChangeLogParameters());
    }

    @Test
    public void rootChangeSet() throws LiquibaseException {
        assertTrue(changeLog.include(
                "classpath:liquibase/resource/changelog1.yml",
                false,
                true,
                accessor,
                new ContextExpression(),
                new Labels(),
                false,
                DatabaseChangeLog.OnUnknownFileFormat.FAIL));

        assertEquals(1, changeLog.getChangeSets().size());
        assertEquals("cs1", changeLog.getChangeSets().get(0).getId());
    }

    @Test
    public void nestedChangeSet_RelativePath() throws LiquibaseException {
        assertTrue(changeLog.include(
                "classpath:liquibase/resource/changelog2.yml",
                false,
                true,
                accessor,
                new ContextExpression(),
                new Labels(),
                false,
                DatabaseChangeLog.OnUnknownFileFormat.FAIL));

        assertEquals(1, changeLog.getChangeSets().size());
        assertEquals("cs2", changeLog.getChangeSets().get(0).getId());
    }

    // This accessor dynamically determines whether resource location is a classpath or a file path (or a URL).
    // When a subresource is resolved with "relativeToChangelogFile: true", the subresource will inherit
    // the location (classpath or file path). 
    static class UrlDrivenResourceAccessor implements ResourceAccessor {

        private static final String CLASSPATH_URL_PREFIX = "classpath:";

        @Override
        public List<Resource> getAll(String path) throws IOException {
            return resolveUrls(path)
                    .stream()
                    .map(u -> new UrlResource(path, u))
                    .collect(Collectors.toList());
        }

        @Override
        public List<String> describeLocations() {
            return Collections.emptyList();
        }

        @Override
        public List<Resource> search(String path, boolean recursive) throws IOException {
            throw new UnsupportedOperationException("'search' operation is not defined");
        }

        @Override
        public void close() throws Exception {
        }

        private Collection<URL> resolveUrls(String resourceId) {

            // can be either a file path or a URL or a classpath: URL
            if (resourceId.startsWith(CLASSPATH_URL_PREFIX)) {

                String path = resolveAsClasspath(resourceId);

                Enumeration<URL> cpUrls;
                try {
                    cpUrls = ResourceFactory.class.getClassLoader().getResources(path);
                } catch (IOException e) {
                    throw new RuntimeException("Can't resolve resources for path: " + path, e);
                }

                if (!cpUrls.hasMoreElements()) {
                    throw new IllegalArgumentException("Classpath URL not found: " + resourceId);
                }

                List<URL> urls = new ArrayList<>(2);
                while (cpUrls.hasMoreElements()) {
                    urls.add(cpUrls.nextElement());
                }

                return urls;
            }

            return Collections.singletonList(resolveAsUri(resourceId));
        }

        private String resolveAsClasspath(String resourceId) {
            String path = resourceId.substring(CLASSPATH_URL_PREFIX.length());

            // classpath URLs must not start with a slash. This does not work with ClassLoader.
            // TODO: should we silently strip the leading path?
            if (path.length() > 0 && path.charAt(0) == '/') {
                throw new RuntimeException(CLASSPATH_URL_PREFIX + " URLs must not start with a slash: " + resourceId);
            }

            return path;
        }

        private URL resolveAsUri(String resourceId) {
            URI uri;
            try {
                uri = URI.create(resourceId);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid resource url: " + resourceId, e);
            }
            try {
                return uri.isAbsolute() ? uri.toURL() : getCanonicalFile(resourceId).toURI().toURL();
            } catch (IOException e) {
                throw new RuntimeException("Invalid resource url: " + resourceId, e);
            }
        }

        private File getCanonicalFile(String resourceId) throws IOException {
            return new File(resourceId).getCanonicalFile();
        }
    }

    static class UrlResource implements Resource {

        private final String path;
        private final URL url;

        private volatile URI uri;

        UrlResource(String path, URL url) {
            this.path = Objects.requireNonNull(path);
            this.url = Objects.requireNonNull(url);
        }

        @Override
        public boolean exists() {
            // we don't really know if the URL exists
            return true;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return url.openStream();
        }

        @Override
        public OutputStream openOutputStream(OpenOptions openOptions) {
            throw new UnsupportedOperationException("The resource is not writable: " + url);
        }

        @Override
        public boolean isWritable() {
            return false;
        }

        @Override
        public Resource resolve(String other) {
            throw new UnsupportedOperationException("The resource is not a directory: " + url);
        }

        @Override
        public Resource resolveSibling(String other) {

            String siblingPath = resolveSiblingPath(this.path, other);
            URL siblingUrl;
            try {
                siblingUrl = new URL(resolveSiblingPath(url.toString(), other));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            return new UrlResource(siblingPath, siblingUrl);
        }

        @Override
        public URI getUri() {
            if (uri == null) {
                try {
                    uri = url.toURI();
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }

            return uri;
        }

        private String resolveSiblingPath(String path, String other) {
            if (path.contains("/")) {
                return path.replaceFirst("/[^/]*$", "") + "/" + other;
            } else {
                return "/" + other;
            }
        }
    }
}
