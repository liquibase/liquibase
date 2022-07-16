package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public class UnknownResource extends AbstractResource {

    public UnknownResource(String path, URI uri) {
        super(path, uri);
    }

    @Override
    public InputStream openInputStream() throws IOException {
        throw new IOException("TODO");
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        throw new IOException("TODO");
    }
}
