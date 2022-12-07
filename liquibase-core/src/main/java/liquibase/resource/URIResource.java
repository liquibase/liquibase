package liquibase.resource;

import liquibase.exception.UnexpectedLiquibaseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class URIResource extends AbstractResource {

    public URIResource(String path, URI uri) {
        super(path, uri);
    }

    /**
     * Cannot determine if the URI exists, return true
     */
    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public Resource resolve(String other) {
        return new URIResource(resolvePath(other), URI.create(getUri().toString() + "/" + other));
    }

    @Override
    public Resource resolveSibling(String other) {
        return new URIResource(resolveSiblingPath(other), URI.create(getUri().toString().replaceFirst("/[^/]*$", "") + "/" + other));
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return getUri().toURL().openStream();
    }

    @Override
    public boolean delete() {
        throw new UnexpectedLiquibaseException("URI resources cannot be deleted.");
    }
}
