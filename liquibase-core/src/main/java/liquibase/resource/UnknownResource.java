package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UnknownResource extends AbstractResource {

    public UnknownResource(String path) {
        super(path);
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
