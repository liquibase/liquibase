package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public interface Resource {
    String getPath();

    boolean exists();

    InputStream openInputStream() throws IOException;

    boolean isWritable();

    OutputStream openOutputStream() throws IOException;

    URI getUri();
}
