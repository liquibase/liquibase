package liquibase.migrator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public interface FileOpener {
    public InputStream getResourceAsStream(String file) throws IOException;

    public Enumeration<URL> getResources(String packageName) throws IOException;
}
