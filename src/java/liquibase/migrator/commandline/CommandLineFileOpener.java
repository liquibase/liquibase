package liquibase.migrator.commandline;

import liquibase.migrator.FileOpener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class CommandLineFileOpener implements FileOpener {
    private ClassLoader loader;

    public CommandLineFileOpener(ClassLoader loader) {
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
}
