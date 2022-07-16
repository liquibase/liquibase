package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

public interface Resource extends Comparable<Resource> {
    String getPath();

    InputStream openInputStream() throws IOException;

    OutputStream openOutputStream() throws IOException;

    URI getUri();
}
