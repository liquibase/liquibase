package liquibase.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public abstract class AbstractResource implements Resource {

    private final String path;
    private final URI uri;

    public AbstractResource(String path, URI uri) {
        this.path = path
                .replace("\\", "/")
                .replaceFirst("^classpath\\*?:", "")
                .replaceFirst("^/", "");

        if (uri != null) {
            this.uri = uri.normalize();
        } else {
            this.uri = null;
        }
    }

    @Override
    public String getPath() {
        return path;
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
