package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Abstracts file access so they can be read in a variety of manners.
 */
public interface ResourceAccessor {

    /**
     * Return the given file path as an InputStream. Return null if the resource does not exist. Throws IOException if there is an error reading an existing file.
     */
    public InputStream getResourceAsStream(String file) throws IOException;

    public Enumeration<URL> getResources(String packageName) throws IOException;

    public ClassLoader toClassLoader();
}
