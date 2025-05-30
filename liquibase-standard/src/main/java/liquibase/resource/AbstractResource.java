package liquibase.resource;

import liquibase.Scope;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractResource implements Resource {

    private final String path;

    private final String originalPath;
    private final URI uri;

    public AbstractResource(String originalPathParam, URI uri) {
        this.originalPath = originalPathParam;
        if (uri != null) {
            this.uri = handleUriSyntax(uri);
        } else {
            this.uri = null;
        }

        String calculatedPath = originalPathParam
                .replace("\\", "/")
                .replaceFirst("^classpath\\*?:", "");

        if (uri != null && uri.toString().contains("!")) {
            this.path = handlePathForJarfile(calculatedPath);
        } else {
            this.path = calculatedPath.replaceFirst("^/", "");
        }
    }

    /**
     * Handle the path for jar file resources as URI does not work as expected when you have an exclamation mark in the path.
     */
    private String handlePathForJarfile(String path) {
        String relative = this.uri.toString().replaceFirst(".*!", "");
        try {
            if (!path.startsWith("..")) {
                path = "/" + path;
            }
            return new URI(relative).resolve(new URI(path).normalize()).toString().replaceFirst("^/", "");
        } catch (URISyntaxException e) {
            Scope.getCurrentScope().getLog(AbstractResource.class).warning("Error handling URI syntax for file inside jar. Defaulting to previous behavior.", e);
            return path.replaceFirst("^/", "");
        }
    }

    private static URI handleUriSyntax(URI uri) {
        // when we have a file inside a zip/jar file (denoted by the exclamation mark)
        // URI doesn't work as expected. So we need some manual handling
        if (uri.toString().contains(("!"))) {
            String[] uriSplit = uri.toString().split(("!"));
            StringBuilder result = new StringBuilder();
            try {
                for (int i = 0; i < uriSplit.length; i++) {
                    if (i > 0) {
                        result.append(new URI(uriSplit[i]).normalize());
                    } else {
                        result.append(uriSplit[i]);
                    }
                    // append a ! if it's not the last part
                    if (i < uriSplit.length - 1) {
                        result.append("!");
                    }
                }
                return new URI(result.toString());
            } catch (URISyntaxException e) {
                return uri.normalize();
            }
        }
        return uri.normalize();
    }

    @Override
    public String getPath() {
        return path;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public OutputStream openOutputStream(OpenOptions openOptions) throws IOException {
        if (!isWritable()) {
            throw new IOException("Read only");
        }
        throw new IOException("Write not implemented");
    }

    @Override
    public String toString() {
        return getPath();
    }

    @Override
    public int hashCode() {
        return this.getUri().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Resource)) {
            return false;
        }
        return this.getUri().equals(((Resource) obj).getUri());
    }

    /**
     * Convenience method for computing the relative path in {@link #resolve(String)} implementations
     */
    protected String resolvePath(String other) {
        if (getPath().endsWith("/")) {
            return getPath() + other;
        }
        return getPath() + "/" + other;
    }

    /**
     * Convenience method for computing the relative path in {@link #resolveSibling(String)} implementations.
     */
    protected String resolveSiblingPath(String other) {
        if (getPath().contains("/")) {
            return getPath().replaceFirst("/[^/]*$", "") + "/" + other;
        } else {
            return "/" + other;
        }
    }
}
