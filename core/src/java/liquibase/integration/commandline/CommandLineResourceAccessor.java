package liquibase.integration.commandline;

import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

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

    public InputStream getResourceAsStream(String file) throws IOException {
        URL resource = loader.getResource(file);
        if (resource == null) {
            throw new IOException(file + " could not be found");
        }
        return resource.openStream();
    }

    public Enumeration<URL> getResources(String packageName) throws IOException {
        return loader.getResources(packageName);
    }

    public ClassLoader toClassLoader() {
        return loader;
    }
}
