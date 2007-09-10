package liquibase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * An implementation of liquibase.FileOpener that opens file from the class loader.
 *
 * @see FileOpener
 */
public class ClassLoaderFileOpener implements FileOpener {
    public InputStream getResourceAsStream(String file) throws IOException {
        return getClass().getClassLoader().getResourceAsStream(file);
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        return getClass().getClassLoader().getResources(packageName);
    }
}
