package liquibase.resource;

import liquibase.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

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

    @Override
    public InputStream getResourceAsStream(String file) throws IOException {
        return classLoader.getResourceAsStream(file);
    }

    @Override
    public Enumeration<URL> getResources(String packageName) throws IOException {
        try {
            URL fileUrl = classLoader.getResource(packageName);
            if (fileUrl != null) {
                File file = new File(fileUrl.toURI());
                if (file.exists() && ! file.isDirectory()) {
                    return new Vector<URL>(Arrays.asList(fileUrl)).elements();
                }
            }
        } catch (Throwable e) {
            //not local file, continue on
        }

        return classLoader.getResources(packageName);
    }

    @Override
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
