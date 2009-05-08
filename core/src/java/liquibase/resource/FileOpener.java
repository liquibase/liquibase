package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Abstracts file access so they can be read in a variety of manners.
 */
public interface FileOpener {
    public InputStream getResourceAsStream(String file) throws IOException;

    public Enumeration<URL> getResources(String packageName) throws IOException;

    public ClassLoader toClassLoader();
}
