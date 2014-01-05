package liquibase.integration.commandline;

import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Implementation of liquibase.FileOpener for the command line app.
 *
 * @see liquibase.resource.ResourceAccessor
 */
public class CommandLineResourceAccessor implements ResourceAccessor {
    private ClassLoader loader;

    public CommandLineResourceAccessor(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public InputStream getResourceAsStream(String file) throws IOException {
        URL resource = loader.getResource(file);
        if (resource == null) {
            // One more try. People are often confused about leading
            // slashes in resource paths...
            if (file.startsWith("/")) {
                resource = loader.getResource(file.substring(1));
            }
            if (resource == null) {
                return null;
            }
        }
        return resource.openStream();
    }

    @Override
    public Enumeration<URL> getResources(String packageName) throws IOException {
        return loader.getResources(packageName);
    }

    @Override
    public ClassLoader toClassLoader() {
        return loader;
    }

    @Override
    public String toString() {
        String description;
        if (loader instanceof URLClassLoader) {
            List<String> urls = new ArrayList<String>();
            for (URL url : ((URLClassLoader) loader).getURLs()) {
                urls.add(url.toExternalForm());
            }
            description = StringUtils.join(urls, ",");
        } else {
            description = loader.getClass().getName();
        }
        return getClass().getName()+"("+ description +")";
    }
}
