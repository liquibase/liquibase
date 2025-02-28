package liquibase.resource;

import org.apache.tools.ant.types.ResourceFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

// This accessor dynamically determines whether resource location is a classpath or a file path (or a URL).
// When a subresource is resolved with "relativeToChangelogFile: true", the subresource will inherit
// the location (classpath or file path).
class UrlDrivenResourceAccessor implements ResourceAccessor {

    private static final String CLASSPATH_URL_PREFIX = "classpath:";

    @Override
    public List<Resource> getAll(String path) throws IOException {
        return resolveUrls(path)
                .stream()
                .map(u -> new UrlDrivenResourceAccessorTest.UrlResource(path, u))
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
