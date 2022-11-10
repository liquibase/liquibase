package liquibase.resource;

import liquibase.util.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Used to represent {@link FileUtil#EMPTY_FILE}
 */
public final class EmptyResource implements Resource {

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return new ByteArrayInputStream(FileUtil.EMPTY_FILE.getBytes(StandardCharsets.UTF_8));
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
