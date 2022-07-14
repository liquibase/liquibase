package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Resource {
    String getPath();

    InputStream openInputStream() throws IOException;

    OutputStream openOutputStream() throws IOException;

    String getDescription();
}
