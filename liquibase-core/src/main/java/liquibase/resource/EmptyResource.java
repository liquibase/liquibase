package liquibase.resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * Used to represent an empty file
 */
public final class EmptyResource implements Resource {

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(new byte[0]);
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public Resource resolve(String other) {
        return null;
    }

    @Override
    public Resource resolveSibling(String other) {
        return null;
    }

    @Override
    public OutputStream openOutputStream(OpenOptions openOptions) throws IOException {
        return null;
    }

    @Override
    public URI getUri() {
        return null;
    }
}
