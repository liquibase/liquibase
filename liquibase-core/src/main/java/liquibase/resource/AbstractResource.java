package liquibase.resource;

import java.net.URI;

public abstract class AbstractResource implements Resource {

    private final String path;
    private final URI uri;

    public AbstractResource(String path, URI uri) {
        this.path = path;
        this.uri = uri;
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
    public int compareTo(Resource o) {
        return this.getUri().compareTo(o.getUri());
    }
}
