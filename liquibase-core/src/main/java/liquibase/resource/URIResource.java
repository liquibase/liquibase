package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class URIResource extends AbstractResource {

    public URIResource(String path, URI uri) {
        super(path, uri);
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return getUri().toURL().openStream();
    }
}
