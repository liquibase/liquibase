package liquibase.migrator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * An implementation of {@link FileOpener} that opens file from the class loader.
 */
public class ClassLoaderFileOpener implements FileOpener {
    public InputStream getResourceAsStream(String file) throws IOException {
        return getClass().getClassLoader().getResourceAsStream(file);
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        return getClass().getClassLoader().getResources(packageName);
    }
}
