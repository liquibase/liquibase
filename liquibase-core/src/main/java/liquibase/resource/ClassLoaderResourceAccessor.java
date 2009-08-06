package liquibase.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * An implementation of liquibase.FileOpener that opens file from the class loader.
 *
 * @see ResourceAccessor
 */
public class ClassLoaderResourceAccessor implements ResourceAccessor {
    private ClassLoader classLoader;

    public ClassLoaderResourceAccessor() {
        this.classLoader = getClass().getClassLoader();
    }

    public ClassLoaderResourceAccessor(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public InputStream getResourceAsStream(String file) throws IOException {
        return classLoader.getResourceAsStream(file);
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        return classLoader.getResources(packageName);
    }

    public ClassLoader toClassLoader() {
        return classLoader;
    }
}
