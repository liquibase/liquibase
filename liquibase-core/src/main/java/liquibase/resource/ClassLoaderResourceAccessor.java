package liquibase.resource;

import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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

    @Override
    public String toString() {
        String description;
        if (classLoader instanceof URLClassLoader) {
            List<String> urls = new ArrayList<String>();
            for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                urls.add(url.toExternalForm());
            }
            description = StringUtils.join(urls, ",");
        } else {
            description = classLoader.getClass().getName();
        }
        return getClass().getName()+"("+ description +")";

    }
}
